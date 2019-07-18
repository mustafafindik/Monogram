package findik.mustafa.monogram.classes;

public class Post {
    private String userImage,userName,userLocation,postImage,userComment,postDate,postId,userId;

    public Post(){

}




    public Post(String userImage, String userName, String userLocation, String postImage, String userComment, String postDate, String postId,String userId) {
        this.userImage = userImage;
        this.userName = userName;
        this.userLocation = userLocation;
        this.postImage = postImage;
        this.userComment = userComment;
        this.postDate=postDate;
        this.postId=postId;
        this.userId = userId;


    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
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

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
