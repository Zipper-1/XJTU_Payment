package edu.xjtu.zipper.xjtupayment.data;

import androidx.lifecycle.MutableLiveData;
import org.json.JSONException;
import org.json.JSONObject;

public class XJTUUser {
    private String sno;
    private String PersonToken;
    private String name;
    //maybe useful in the future
    private String phoneNumber;
    private String avatarUrl;
    private int userId;
    //multiuser in future?
    private static final MutableLiveData<XJTUUser> activeUser = new MutableLiveData<>();

    public JSONObject exportJson() throws JSONException {
        JSONObject infoJs = new JSONObject();
        infoJs.put("token",PersonToken);
        infoJs.put("name",name);
        infoJs.put("phone",phoneNumber);
        infoJs.put("sno",sno);
        infoJs.put("user_id",userId);
        infoJs.put("avatar",avatarUrl);
        JSONObject returnJs = new JSONObject();
        returnJs.put("data_version",1);
        returnJs.put("data",infoJs);
        return returnJs;
    }

    public static XJTUUser importJson(JSONObject js,boolean setActive) throws XjtuUserImportException {
        if(!js.has("data_version"))
            throw new XjtuUserImportException("No data version given");
        XJTUUser returnUser = new XJTUUser();
        try {
            int version = js.getInt("data_version");
            switch (version) {
                case 1:
                    JSONObject dataJs = js.getJSONObject("data");
                    if(!(dataJs.has("token") && dataJs.has("name") && dataJs.has("phone")
                            && dataJs.has("sno") && dataJs.has("user_id") && dataJs.has("avatar"))) {
                        throw new XjtuUserImportException("data corrupted");
                    }
                    returnUser.setUserId(dataJs.getInt("user_id"));
                    returnUser.setSno(dataJs.getString("sno"));
                    returnUser.setPhoneNumber(dataJs.getString("phone"));
                    returnUser.setName(dataJs.getString("name"));
                    returnUser.setAvatarUrl(dataJs.getString("avatar"));
                    returnUser.setPersonToken(dataJs.getString("token"));
                    break;
                default:
                    throw new XjtuUserImportException("Data version " + version + " not compatible");

            }
        } catch (JSONException e) {
            throw new XjtuUserImportException(e);
        }
        if(setActive)
            XJTUUser.activeUser.postValue(returnUser);
        return returnUser;
    }
    public static MutableLiveData<XJTUUser> getActiveUser() {
        return activeUser;
    }

    public XJTUUser() {

    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getPersonToken() {
        return PersonToken;
    }

    public void setPersonToken(String personToken) {
        PersonToken = personToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
