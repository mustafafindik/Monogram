package findik.mustafa.monogram.classes;

public class Comment {
    private String userImage,userName,userComment,postDate,userId;

    public Comment() {

    }

    public Comment(String userImage, String userName, String userComment, String postDate, String userId) {
        this.userImage = userImage;
        this.userName = userName;
        this.userComment = userComment;
        this.postDate = postDate;
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

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
