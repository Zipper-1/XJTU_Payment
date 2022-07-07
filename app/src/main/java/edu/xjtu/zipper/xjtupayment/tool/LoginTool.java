package edu.xjtu.zipper.xjtupayment.tool;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import edu.xjtu.zipper.xjtupayment.data.XJTUUser;
import edu.xjtu.zipper.xjtupayment.ui.login.LoginResult;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginTool {

    private boolean loginActDone = false;
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    public LoginTool() {
        loginActDone = false;
    }

    public void login(String username, String password) {

        Log.d("login debug","loginacted");
        OkHttpClient client = new OkHttpClient();
        RequestBody reqBody = RequestBody.create("{\"acount\":\"" + username + "\",\"pwd\":\"" + password + "\"}", MediaType.parse("application/json"));
        Request.Builder getPersonToken = new Request.Builder().url("http://org.xjtu.edu.cn/openplatform//toon/auth/loginByPwd");
        getPersonToken.addHeader("secretkey", "18a9d512c03745a791d92630bc0888f6");
        client.newCall(getPersonToken.post(reqBody).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                loginResult.setValue(new LoginResult(null,false,"网络请求失败"));
                loginActDone = true;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {

                try {
                    assert response.body() != null;
                    JSONObject resJs = new JSONObject(response.body().string());
                    Log.d("login debug",resJs.toString());
                    if(resJs.getJSONObject("meta").getInt("code") == 0) {
                        XJTUUser user = new XJTUUser();
                        user.setPersonToken(resJs.getJSONObject("data").get("personToken").toString());
                        user.setSno(username);
                        user.setName(resJs.getJSONObject("data").getString("nickName"));
                        user.setPhoneNumber(resJs.getJSONObject("data").getString("mobile"));
                        user.setUserId(resJs.getJSONObject("data").getInt("memberId"));
                        user.setAvatarUrl(resJs.getJSONObject("data").getString("avatar"));
                        loginResult.postValue(new LoginResult(user,true,"登陆成功"));
                    }
                    else {
                        loginResult.postValue(new LoginResult(null,false,(resJs.getJSONObject("meta")).get("message").toString()));
                    }
                    loginActDone = true;
                } catch (JSONException | IOException e) {
                    loginResult.postValue(new LoginResult(null,false,"数据解析失败"));
                    loginActDone = true;
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public boolean isLoginActDone() {
        return loginActDone;
    }

    public MutableLiveData<LoginResult> getLoginResult() {
        return loginResult;
    }
}
