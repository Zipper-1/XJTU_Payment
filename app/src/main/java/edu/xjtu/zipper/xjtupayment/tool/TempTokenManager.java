package edu.xjtu.zipper.xjtupayment.tool;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class TempTokenManager{
    private MutableLiveData<String> tempIdCode = new MutableLiveData<>();
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
                tempIdCode.setValue("failed");
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
                        tempIdCode.postValue("loginexpired");
                    }
                    else
                        tempIdCode.postValue(resJS.get("data").toString());
                } catch (JSONException e) {
                    tempIdCode.postValue("failed");
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public LiveData<String> getCode()
    {
        return tempIdCode;
    }

}
