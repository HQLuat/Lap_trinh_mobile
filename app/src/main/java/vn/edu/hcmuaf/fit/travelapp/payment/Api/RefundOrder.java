package vn.edu.hcmuaf.fit.travelapp.payment.Api;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import okhttp3.FormBody;

import org.json.JSONException;
import org.json.JSONObject;

import vn.edu.hcmuaf.fit.travelapp.payment.Constant.AppInfo;
import vn.edu.hcmuaf.fit.travelapp.payment.Helper.Helpers;

public class RefundOrder {
    private static final String TAG = "ZP_DEBUG(Refund)";
    private static final int MAX_DESCRIPTION = 100;

    private void logFormBody(FormBody formBody) {
        StringBuilder sb = new StringBuilder("FormBody params:\n");
        for (int i = 0; i < formBody.size(); i++) {
            sb.append(formBody.name(i)).append(" = ").append(formBody.value(i)).append("\n");
        }
        Log.d(TAG, sb.toString());
    }

    private static class RefundOrderData {
        String appId;
        String zpTransId;
        String amount;
        String description;
        String timestamp;
        String mac;
        String mRefundId;

        public RefundOrderData(String zpTransId, String amount, String description)
                throws NoSuchAlgorithmException, InvalidKeyException {
            if (zpTransId == null || zpTransId.length() > 15) {
                throw new IllegalArgumentException("zp_trans_id must be non-null and ≤15 chars");
            }
            this.appId = String.valueOf(AppInfo.APP_ID);
            this.zpTransId = zpTransId;
            this.amount = amount;

            long tsMillis = System.currentTimeMillis();
            this.timestamp = String.valueOf(tsMillis);

            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            String datePart = sdf.format(new Date());
            String randomPart = String.valueOf(new Random().nextInt(1_000_000_000));
            this.mRefundId = datePart + "_" + appId + "_" + randomPart;

            String desc = description != null ? description.trim() : "";
            if (desc.length() > MAX_DESCRIPTION) {
                desc = desc.substring(0, MAX_DESCRIPTION);
            }
            this.description = desc;

            String inputHmac = appId + "|" + zpTransId + "|" + amount + "|" + this.description + "|" + timestamp;
            this.mac = Helpers.getMac(AppInfo.MAC_KEY, inputHmac);

            Log.d(TAG, "m_refund_id = " + mRefundId);
            Log.d(TAG, "zp_trans_id = " + zpTransId);
            Log.d(TAG, "amount = " + amount);
            Log.d(TAG, "timestamp(ms) = " + timestamp);
            Log.d(TAG, "timestamp readable = " + formatTimestamp(tsMillis));
            Log.d(TAG, "description = " + this.description);
            Log.d(TAG, "mac = " + mac);
        }
    }

    public JSONObject refund(String zpTransId, String amount, String description) throws JSONException {
        try {
            RefundOrderData input = new RefundOrderData(zpTransId, amount, description);
            JSONObject refundResp = sendRefundRequest(input);

            Log.d(TAG, "Querying refund status...");
            JSONObject statusResp = queryRefundStatus(input.mRefundId);

            JSONObject result = new JSONObject();
            result.put("m_refund_id", input.mRefundId);
            result.put("refund_response", refundResp);
            result.put("status_response", statusResp);
            return result;
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Invalid param: " + iae.getMessage());
            JSONObject err = new JSONObject(); err.put("return_code", -97);
            err.put("return_message", iae.getMessage()); return err;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "Signature error: " + e.getMessage());
            JSONObject err = new JSONObject(); err.put("return_code", -99);
            err.put("return_message", "Signature error"); return err;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            JSONObject err = new JSONObject(); err.put("return_code", -98);
            err.put("return_message", "Network or unexpected error"); return err;
        }
    }

    private JSONObject sendRefundRequest(RefundOrderData input) throws Exception {
        FormBody formBody = new FormBody.Builder()
                .add("m_refund_id", input.mRefundId)
                .add("app_id", input.appId)
                .add("zp_trans_id", input.zpTransId)
                .add("amount", input.amount)
                .add("timestamp", input.timestamp)
                .add("description", input.description)
                .add("mac", input.mac)
                .build();

        logFormBody(formBody);

        Log.d(TAG, "Sending refund request to " + AppInfo.URL_REFUND);
        JSONObject resp = HttpProvider.sendPost(AppInfo.URL_REFUND, formBody);
        Log.d(TAG, "Refund response: " + resp.toString());
        return resp;
    }

    public JSONObject queryRefundStatus(String mRefundId) throws JSONException {
        try {
            String appId = String.valueOf(AppInfo.APP_ID);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String inputHmac = appId + "|" + mRefundId + "|" + timestamp;
            String mac = Helpers.getMac(AppInfo.MAC_KEY, inputHmac);

            FormBody formBody = new FormBody.Builder()
                    .add("app_id", appId)
                    .add("m_refund_id", mRefundId)
                    .add("timestamp", timestamp)
                    .add("mac", mac)
                    .build();

            logFormBody(formBody);
            Log.d(TAG, "Sending refund status query to " + AppInfo.URL_QUERY_REFUND);
            JSONObject resp = HttpProvider.sendPost(AppInfo.URL_QUERY_REFUND, formBody);
            Log.d(TAG, "Refund query response: " + resp.toString());

            return resp;
        } catch (Exception e) {
            Log.e(TAG, "Refund query error: " + e.getMessage());
            JSONObject err = new JSONObject(); err.put("return_code", -99);
            err.put("return_message", "Refund query error"); return err;
        }
    }

    private static String formatTimestamp(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date(millis));
    }
}
