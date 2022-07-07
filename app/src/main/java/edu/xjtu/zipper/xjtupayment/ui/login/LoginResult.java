package edu.xjtu.zipper.xjtupayment.ui.login;

import edu.xjtu.zipper.xjtupayment.data.XJTUUser;

public class LoginResult {
    private XJTUUser user;
    private boolean loginSuccess;
    private String loginMessage;

    public LoginResult(XJTUUser user, boolean loginSuccess, String loginMessage) {
        this.user = user;
        this.loginSuccess = loginSuccess;
        this.loginMessage = loginMessage;
    }

    public XJTUUser getUser() {
        return user;
    }

    public String getUsername() {
        return user.getName();
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public String getLoginMessage() {
        return loginMessage;
    }
}
