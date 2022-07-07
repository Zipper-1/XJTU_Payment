package edu.xjtu.zipper.xjtupayment.ui.login;

import android.app.Activity;
import android.util.Log;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import edu.xjtu.zipper.xjtupayment.data.XJTUUser;
import edu.xjtu.zipper.xjtupayment.databinding.ActivityLoginBinding;
import edu.xjtu.zipper.xjtupayment.tool.LoginTool;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private LoginTool loginTool;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("activity","login activity created");
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loginTool = new LoginTool();
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginTool.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.isLoginSuccess()) {
                    Log.d("Login listener","success");
                    XJTUUser.getActiveUser().setValue(loginResult.getUser());
                    try {

                        JSONObject loginInfoJson = loginResult.getUser().exportJson();

                        FileOutputStream loginInfoStream = getApplicationContext().openFileOutput("info.json",MODE_PRIVATE);
                        loginInfoStream.write(loginInfoJson.toString().getBytes(StandardCharsets.UTF_8));
                        loginInfoStream.close();
                        Log.d("Login info changed","write&save:"+loginInfoJson.toString());
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    setResult(Activity.RESULT_OK);
                    Log.i("LoginActivity","login success and quit");
                    finish();
                }
                else {
                    Log.d("Login listener","failed");
                    Toast.makeText(getApplicationContext(), loginResult.getLoginMessage(), Toast.LENGTH_LONG).show();
                }
                //Complete and destroy login activity once successful
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginTool.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }




}