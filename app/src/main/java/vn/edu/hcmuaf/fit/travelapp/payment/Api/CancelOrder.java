package vn.edu.hcmuaf.fit.travelapp.payment.Api;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import vn.edu.hcmuaf.fit.travelapp.payment.Constant.AppInfo;
import vn.edu.hcmuaf.fit.travelapp.payment.Helper.Helpers;

public class CancelOrder {

    private class CancelOrderData {
        String appId;
        String appTransId;
        String mac;

        public CancelOrderData(String appTransId) throws NoSuchAlgorithmException, InvalidKeyException {
            this.appId = String.valueOf(AppInfo.APP_ID);
            this.appTransId = appTransId;
            String input = String.format("%s|%s|%s", this.appId, this.appTransId, AppInfo.MAC_KEY);
            // Helpers.getMac dùng HMAC SHA256 và trả về hex string
            this.mac = Helpers.getMac(AppInfo.MAC_KEY, this.appId + "|" + this.appTransId);
        }
    }

    /**
     * Hủy order (chỉ khi chưa thanh toán)
     * @param appTransId ID khi tạo order
     * @return JSONObject kết quả { return_code, return_message, ... }
     * @throws Exception
     */
    public JSONObject cancel(String appTransId) throws Exception {
        CancelOrderData d = new CancelOrderData(appTransId);

        RequestBody body = new FormBody.Builder()
                .add("app_id", d.appId)
                .add("app_trans_id", d.appTransId)
                .add("mac", d.mac)
                .build();

        return HttpProvider.sendPost(AppInfo.URL_CANCEL_ORDER, body);
    }
}
