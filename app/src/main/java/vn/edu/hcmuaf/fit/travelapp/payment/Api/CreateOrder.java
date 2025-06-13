package vn.edu.hcmuaf.fit.travelapp.payment.Api;

import android.util.Log;

import okhttp3.RequestBody;
import vn.edu.hcmuaf.fit.travelapp.payment.Constant.AppInfo;
import vn.edu.hcmuaf.fit.travelapp.payment.Helper.Helpers;

import org.json.JSONObject;

import java.util.Date;

import okhttp3.FormBody;


public class CreateOrder {
    private static class CreateOrderData {
        String AppId;
        String AppUser;
        String AppTime;
        String Amount;
        String AppTransId;
        String EmbedData;
        String Items;
        String BankCode;
        String Description;
        String Mac;

    private CreateOrderData(String amount, String appTransId) throws Exception {
        long appTime = new Date().getTime();
        AppId = String.valueOf(AppInfo.APP_ID);
        AppUser = "Android_Demo";
        AppTime = String.valueOf(appTime);
        Amount = amount;
        AppTransId = appTransId;
        EmbedData = "{}";
        Items = "[]";
        BankCode = "zalopayapp";
        Description = "Merchant pay for order #" + appTransId;
        String inputHMac = String.format("%s|%s|%s|%s|%s|%s|%s",
                this.AppId,
                this.AppTransId,
                this.AppUser,
                this.Amount,
                this.AppTime,
                this.EmbedData,
                this.Items);

        Mac = Helpers.getMac(AppInfo.MAC_KEY, inputHMac);

        // === Chèn log ở đây ===
        Log.d("ZP_DEBUG", "CreateOrderData:");
        Log.d("ZP_DEBUG", "  inputHMac = " + inputHMac);
        Log.d("ZP_DEBUG", "  Mac       = " + Mac);
    }
}

        /**
     * Gọi tạo order ZaloPay với appTransId đã cho.
     * @param amount số tiền (chuỗi)
     * @param appTransId phải trùng với orderId Firestore
     */
    public JSONObject createOrder(String amount, String appTransId) throws Exception {
        CreateOrderData input = new CreateOrderData(amount, appTransId);
        RequestBody formBody = new FormBody.Builder()
                .add("app_id", input.AppId)
                .add("app_user", input.AppUser)
                .add("app_time", input.AppTime)
                .add("amount", input.Amount)
                .add("app_trans_id", appTransId)
                .add("embed_data", input.EmbedData)
                .add("item", input.Items)
                .add("bank_code", input.BankCode)
                .add("description", input.Description)
                .add("mac", input.Mac)
                .build();

        JSONObject data = HttpProvider.sendPost(AppInfo.URL_CREATE_ORDER, formBody);

        // === Chèn log ở đây ===
        Log.d("ZP_DEBUG", "createOrder request to " + AppInfo.URL_CREATE_ORDER);
        Log.d("ZP_DEBUG", "  amount       = " + amount);
        Log.d("ZP_DEBUG", "  app_trans_id = " + appTransId);
        Log.d("ZP_DEBUG", "Response JSON = " + data.toString());

        return data;
    }
}

