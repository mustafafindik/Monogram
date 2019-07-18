package findik.mustafa.monogram.classes;

public class User {
    private String userImage,userName,userId;

    public User() {

    }

    public User(String userImage, String userName, String userId) {
        this.userImage = userImage;
        this.userName = userName;
        this.userId = userId;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
