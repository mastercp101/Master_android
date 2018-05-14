package nom.cp101.master.master.Message.CLASS;

/**
 * Created by chunyili on 2018/5/6.
 */

public class BaseMessage {
    private String message;
    private String userName;

    public BaseMessage(String message, String userName) {
        this.message = message;
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}