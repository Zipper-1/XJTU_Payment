package edu.xjtu.zipper.xjtupayment.tool;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import edu.xjtu.zipper.xjtupayment.data.TokenHolder;
import edu.xjtu.zipper.xjtupayment.data.XJTUUser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TempTokenManager{
    private MutableLiveData<TokenHolder> tempIdCode = new MutableLiveData<>();
    private MutableLiveData<TokenHolder> tempPaymentSign = new MutableLiveData<>();
    private final String secretKey = "18a9d512c03745a791d92630bc0888f6";
    public void refreshCode(String personalToken)
    {
        OkHttpClient client = new OkHttpClient();
        Request.Builder getCode = new Request.Builder().url("http://org.xjtu.edu.cn/openplatform/toon/auth/getCode?personToken="+personalToken);
        getCode.addHeader("Authorization",personalToken);
        getCode.addHeader("secretkey",secretKey);
        client.newCall(getCode.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                tempIdCode.postValue(new TokenHolder(TokenHolder.ErrorType.failed));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                assert response.body() != null;
                String resStr = response.body().string();
                Log.d("Temp code refresh",resStr);
                try {
                    JSONObject resJS = new JSONObject(resStr);

                    if(resJS.has("code")&&(resJS.getInt("code")==600061||resJS.getInt("code")==600062))
                    {
                        tempIdCode.postValue(new TokenHolder(TokenHolder.ErrorType.expired));
                    }
                    else
                        tempIdCode.postValue(new TokenHolder(resJS.getString("data")));
                } catch (JSONException e) {
                    tempIdCode.postValue(new TokenHolder(TokenHolder.ErrorType.failed));
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void refreshSign() throws JSONException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder getSign = new Request.Builder().url("https://org.xjtu.edu.cn/openplatformtoon/ecard/getPayCodeParam");
        getSign.addHeader("code",this.tempIdCode.getValue().getContent());
        JSONObject datajs = new JSONObject();
        datajs.put("sno",XJTUUser.getActiveUser().getValue().getSno());
        RequestBody reqBody = RequestBody.create(datajs.toString(), MediaType.parse("application/json"));
        client.newCall(getSign.post(reqBody).build()).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    assert response.body() != null;
                    JSONObject resJs = new JSONObject(response.body().string());
                    if(resJs.getInt("code") == 0) {
                        tempPaymentSign.postValue(new TokenHolder(resJs.getJSONObject("data").getString("sign")));
                    }
                    else {
                        tempPaymentSign.postValue(new TokenHolder(TokenHolder.ErrorType.expired));
                    }
                } catch (JSONException e) {
                    tempPaymentSign.postValue(new TokenHolder(TokenHolder.ErrorType.failed));
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }

    public MutableLiveData<TokenHolder> getCode()
    {
        return tempIdCode;
    }

    public MutableLiveData<TokenHolder> getSign() {
        return tempPaymentSign;
    }
}
