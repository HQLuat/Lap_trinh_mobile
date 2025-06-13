package vn.edu.hcmuaf.fit.travelapp.payment.Api;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.json.JSONObject;
import java.util.Date;
import vn.edu.hcmuaf.fit.travelapp.payment.Constant.AppInfo;
import vn.edu.hcmuaf.fit.travelapp.payment.Helper.Helpers;

public class RefundOrder {

    /**
     * Data for refund request
     */
    private class RefundOrderData {
        String appId;
        String mRefundId;
        String zpTransId;
        String amount;
        String timestamp;
        String description;
        String mac;

        public RefundOrderData(String zpTransId, String amount, String description) throws Exception {
            this.appId = String.valueOf(AppInfo.APP_ID);
            this.zpTransId = zpTransId;
            this.amount = amount;
            this.mRefundId = Helpers.getAppTransId();
            this.timestamp = String.valueOf(new Date().getTime());
            this.description = description;
            String inputHmac = String.format(
                    "%s|%s|%s|%s|%s",
                    this.appId,
                    this.mRefundId,
                    this.zpTransId,
                    this.amount,
                    this.timestamp
            );
            this.mac = Helpers.getMac(AppInfo.MAC_KEY, inputHmac);
        }
    }

    /**
     * Data for querying refund status
     */
    private class QueryRefundData {
        String appId;
        String mRefundId;
        String timestamp;
        String mac;

        public QueryRefundData(String mRefundId) throws Exception {
            this.appId = String.valueOf(AppInfo.APP_ID);
            this.mRefundId = mRefundId;
            this.timestamp = String.valueOf(new Date().getTime());
            String inputHmac = String.format(
                    "%s|%s|%s",
                    this.appId,
                    this.mRefundId,
                    this.timestamp
            );
            this.mac = Helpers.getMac(AppInfo.MAC_KEY, inputHmac);
        }
    }

    /**
     * Send refund request to ZaloPay sandbox
     * @param zpTransId Zalopay transaction ID to refund
     * @param amount Amount to refund
     * @param description Reason for refund
     * @return JSONObject response from ZaloPay
     * @throws Exception
     */
    public JSONObject refund(String zpTransId, String amount, String description) throws Exception {
        RefundOrderData input = new RefundOrderData(zpTransId, amount, description);
        RequestBody formBody = new FormBody.Builder()
                .add("m_refund_id", input.mRefundId)
                .add("app_id", input.appId)
                .add("zp_trans_id", input.zpTransId)
                .add("amount", input.amount)
                .add("timestamp", input.timestamp)
                .add("description", input.description)
                .add("mac", input.mac)
                .build();
        return HttpProvider.sendPost(AppInfo.URL_REFUND, formBody);
    }

    /**
     * Query the status of a refund
     * @param mRefundId The refund transaction ID generated earlier
     * @return JSONObject response with refund status
     * @throws Exception
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
