package vn.edu.hcmuaf.fit.travelapp.payment.Api;

import android.util.Log;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.json.JSONObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import vn.edu.hcmuaf.fit.travelapp.payment.Constant.AppInfo;
import vn.edu.hcmuaf.fit.travelapp.payment.Helper.Helpers;

public class RefundOrder {

    /**
     * Data for refund request
     */
    private static class RefundOrderData {
        String appId;
        String mRefundId;
        String zpTransId;
        String amount;
        String refundFeeAmount; // optional
        String timestamp;
        String description;
        String mac;

        /**
         * @param zpTransId Zalopay transaction ID
         * @param amount refund amount
         * @param refundFeeAmount optional fee (pass null if none)
         * @param description refund reason
         */
        public RefundOrderData(String zpTransId, String amount, String refundFeeAmount, String description) throws NoSuchAlgorithmException, InvalidKeyException {
            this.appId = String.valueOf(AppInfo.APP_ID);
            this.zpTransId = zpTransId;
            this.amount = amount;
            this.refundFeeAmount = refundFeeAmount;
            this.mRefundId = Helpers.getAppTransId();
            // Timestamp in seconds
            long tsSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            this.timestamp = String.valueOf(tsSeconds);
            this.description = description;

            // Build HMAC input string
            StringBuilder sb = new StringBuilder();
            sb.append(appId)
                    .append('|').append(mRefundId)
                    .append('|').append(zpTransId)
                    .append('|').append(amount);
            if (refundFeeAmount != null && !refundFeeAmount.isEmpty()) {
                sb.append('|').append(refundFeeAmount);
            }
            sb.append('|').append(description)
                    .append('|').append(timestamp);

            String inputHmac = sb.toString();
            this.mac = Helpers.getMac(AppInfo.MAC_KEY, inputHmac);

            Log.d("ZP_DEBUG(Refund)", "refund_id = " + mRefundId);
            Log.d("ZP_DEBUG(Refund)", "zp_trans_id = " + zpTransId);
            Log.d("ZP_DEBUG(Refund)", "amount = " + amount);
            Log.d("ZP_DEBUG(Refund)", "timestamp = " + timestamp);
            Log.d("ZP_DEBUG(Refund)", "description = " + description);
            Log.d("ZP_DEBUG(Refund)", "mac = " + mac);

        }
    }

    /**
     * Data for querying refund status
     */
    private static class QueryRefundData {
        String appId;
        String mRefundId;
        String timestamp;
        String mac;

        public QueryRefundData(String mRefundId) throws NoSuchAlgorithmException, InvalidKeyException {
            this.appId = String.valueOf(AppInfo.APP_ID);
            this.mRefundId = mRefundId;
            long tsSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            this.timestamp = String.valueOf(tsSeconds);

            String inputHmac = appId + '|' + mRefundId + '|' + timestamp;
            this.mac = Helpers.getMac(AppInfo.MAC_KEY, inputHmac);
        }
    }

    /**
     * Send refund request
     * @param zpTransId Zalopay transaction ID to refund
     * @param amount amount to refund
     * @param refundFeeAmount optional refund fee (pass null if none)
     * @param description refund reason
     */
    public JSONObject refund(String zpTransId, String amount, String refundFeeAmount, String description) throws Exception {
        RefundOrderData input = new RefundOrderData(zpTransId, amount, refundFeeAmount, description);
        FormBody.Builder builder = new FormBody.Builder()
                .add("m_refund_id", input.mRefundId)
                .add("app_id", input.appId)
                .add("zp_trans_id", input.zpTransId)
                .add("amount", input.amount);
        if (refundFeeAmount != null && !refundFeeAmount.isEmpty()) {
            builder.add("refund_fee_amount", refundFeeAmount);
        }
        builder.add("timestamp", input.timestamp)
                .add("description", input.description)
                .add("mac", input.mac);

        RequestBody formBody = builder.build();
        return HttpProvider.sendPost(AppInfo.URL_REFUND, formBody);
    }

    /**
     * Query refund status
     * @param mRefundId the refund transaction ID
     */
    public JSONObject queryRefundStatus(String mRefundId) throws Exception {
        QueryRefundData input = new QueryRefundData(mRefundId);
        RequestBody formBody = new FormBody.Builder()
                .add("m_refund_id", input.mRefundId)
                .add("app_id", input.appId)
                .add("timestamp", input.timestamp)
                .add("mac", input.mac)
                .build();
        return HttpProvider.sendPost(AppInfo.URL_REFUND_STATUS, formBody);
    }
}
