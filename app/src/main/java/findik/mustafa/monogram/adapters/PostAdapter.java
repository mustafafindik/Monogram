package findik.mustafa.monogram.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import findik.mustafa.monogram.CommentActivity;
import findik.mustafa.monogram.ProfileActivity;
import findik.mustafa.monogram.R;
import findik.mustafa.monogram.classes.GetTimeAgo;
import findik.mustafa.monogram.classes.Post;

// Postları RecyclerView e eklemek için Adapter.
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    //Postları lsitede tutmak için List<Post>
    private List<Post> postList;
    private Context mContext;


    // initalzie.
    private FirebaseAuth mAuth;
    private DatabaseReference mLikeDatabase;
    private DatabaseReference mCommentDatabase;
    private Boolean LikeStatus = false; // tekrar Liste basmasını engellemek için.

    public class MyViewHolder extends RecyclerView.ViewHolder {

        // View içindeki Değişkenleri tanımladık.
        public TextView usernameTop,UserNameBottom, userLocation, userComment, postTime, likeBtn, commentBtn, likeCount, comment_count;
        public CircleImageView userPhoto;
        public ImageView postImage, mMenu;
        public LinearLayout comment_liner, location_liner;

        MyViewHolder(View view) {
            super(view);
            // View İnitalize.
            userPhoto = view.findViewById(R.id.profile_user_image);
            postImage = view.findViewById(R.id.postImage);
            usernameTop = view.findViewById(R.id.user_Name);
            userLocation = view.findViewById(R.id.user_Location);
            userComment = view.findViewById(R.id.user_Comment);
            postTime = view.findViewById(R.id.post_time);
            likeBtn = view.findViewById(R.id.post_like);
            commentBtn = view.findViewById(R.id.post_comment_ic);
            comment_liner = view.findViewById(R.id.comment_liner);
            likeCount = view.findViewById(R.id.likes_count);
            comment_count = view.findViewById(R.id.comment_count);
            location_liner = view.findViewById(R.id.location_liner);
            mMenu = view.findViewById(R.id.item_menu);
            UserNameBottom = view.findViewById(R.id.User_NameOnComment);



        }

        // Likebtn Set Durumu. ( Like mı değil mi ilk başta)
        public void likeBtn(final String post_id) {
            //mLikeDatabase (altta) ValueEventListener ile Likes verilerini çekiyoruz.
            mLikeDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> likelist = new ArrayList<>(); //İlerde kullanmak için ve like sayısı için Like listeye atıyorz.
                    for (final DataSnapshot like : dataSnapshot.child(post_id).getChildren()) { //liste oldugundan dolayı for.
                        likelist.add((Objects.requireNonNull(like.getValue()).toString())); //likeliste atıyoruz.
                    }
                    int count = likelist.size(); // like sayısı.
                    likeCount.setText(String.valueOf(count) + " "); // layoutta like sayısına set.et.

                    // Giriş yapan kullanıcı id si.
                    final String currentuserid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                    if (dataSnapshot.child(post_id).hasChild(currentuserid)) {
                        // Eğer bu posttu kullanıcı beğenmesse. Yanı Kullanıcı id ye ait chiled varsa
                        likeBtn.setBackgroundResource(R.drawable.ic_action_like_full); // Kırmızı kalpli resim set.
                    } else {
                        // Yoksa Boş Kalpli resim set.
                        likeBtn.setBackgroundResource(R.drawable.ic_action_like_empty);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void comment_count(String post_id) {
            ValueEventListener valueEventListener = mCommentDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> commentList = new ArrayList<>();

                    for (final DataSnapshot comment : dataSnapshot.getChildren()) { //liste oldugundan dolayı for.
                        commentList.add((Objects.requireNonNull(comment.getValue()).toString())); //likeliste atıyoruz.
                    }

                    int count = commentList.size(); // like sayısı.
                    comment_count.setText(String.valueOf(count) + " "); // layoutta like sayısına set.et.
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    public PostAdapter(List<Post> postList, Context mContext) {
        this.postList = postList; // PostListesi.
        this.mContext = mContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                // Oluşturduğumuz post_list_row layoutunu set infilate ediyoruz
                .inflate(R.layout.post_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final Post post = postList.get(position); // Postu Çektik.
        final Context context = holder.userPhoto.getContext(); // Context.
        final String post_id = postList.get(position).getPostId(); //Postid.


        //Üstte Kullandığımız Like verileri için Db. ref.
        mLikeDatabase = FirebaseDatabase.getInstance().getReference().child("Likes");
        mLikeDatabase.keepSynced(true); // Cache.

        mCommentDatabase = FirebaseDatabase.getInstance().getReference().child("Comments").child(post_id);
        mLikeDatabase.keepSynced(true); // Cache.

        mAuth = FirebaseAuth.getInstance();

        // Resmi TimeStrmp olarak kaydettik  GetTimeAgo classı ile ne kadar zaman önce olduğunu alabliyoruz.
        Long lastTime = Long.parseLong(post.getPostDate());
        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, context); // Ne kadar zaman önce.

        final Picasso picassoUserImage = Picasso.with(context);
        picassoUserImage.setIndicatorsEnabled(false); // Sol Üsteki Renkli üçgenlerden kurtulmak için.

        //  Resimler Uri olduğundan Picasso yardımıysa eklenitoz resimleri
        // NetworkPolicy OFFLINE ile resimleri hafızada cache de tutuyoruz.
        picassoUserImage.load(post.getUserImage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.userPhoto, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                picassoUserImage.load(post.getUserImage()).into(holder.userPhoto);
            }
        });


        final Picasso picassoPostImage = Picasso.with(context);
        picassoUserImage.setIndicatorsEnabled(false);
        //  Resimler Uri olduğundan Picasso yardımıysa eklenitoz resimleri
        // NetworkPolicy OFFLINE ile resimleri hafızada cache de tutuyoruz.
        picassoPostImage.load(post.getPostImage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.postImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                picassoPostImage.with(context).load(post.getPostImage()).into(holder.postImage);
            }
        });




        // Kullanıcı ve Post bildilerini Set ediyoruz.
        holder.usernameTop.setText(post.getUserName());
       holder.UserNameBottom.setText("- " + post.getUserName());
        holder.userComment.setText("\""+post.getUserComment()+"\"");
        holder.postTime.setText(lastSeenTime);
        holder.likeBtn(post_id); // Bu Metod ile Üstteki durumun gerçekleşmesini set ediyoruz. Kırmız kalp yada boş kalp
        holder.comment_count(post_id);


        // Lokasyon boş değilse görünür olsun.
        if (!TextUtils.isEmpty(post.getUserLocation())) {
            holder.userLocation.setText(post.getUserLocation());
            holder.location_liner.setVisibility(View.VISIBLE);
        }


        // Üstteki Kullanıcı adına tıklanıldığında.
        holder.usernameTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goProfile(position, context);
            }
        });

        // Üstteki Kullanıcı Resmine tıklanıldığında.
        holder.userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goProfile(position, context);
            }
        });


        // Begeni resime tıklanıldığında.
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikesProcess(position);
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToComment(v.getContext(), post_id);
            }
        });

        holder.comment_liner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToComment(v.getContext(), post_id);
            }
        });

        holder.mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postUserId = postList.get(position).getUserId();
                if (postUserId.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())) {
                    PopupMenu popupMenu = new PopupMenu(mContext, holder.mMenu);
                    popupMenu.inflate(R.menu.post_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.edit_post:

                                    Toast.makeText(mContext, "Düzenleme İşlemi", Toast.LENGTH_LONG).show();
                                    break;
                                case R.id.delete_post:
                                    Toast.makeText(mContext, "Silme İşlemi", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            }

        });


    }


    private void GoToComment(Context context, String postID) {
        Intent gotoComment = new Intent(context, CommentActivity.class);
        gotoComment.putExtra("postId", postID);
        context.startActivity(gotoComment);
    }

    private void goProfile(int position, Context context) {
        // O Kullancıya ait İd ile Profile Act. git.
        String postUserId = postList.get(position).getUserId();
        Intent gotoProfile = new Intent(context, ProfileActivity.class);
        gotoProfile.putExtra("user_id", postUserId);
        context.startActivity(gotoProfile);
    }

    private void LikesProcess(int position) {

        LikeStatus = true; // Begeni durumu true.
        final String post_id = postList.get(position).getPostId(); // Postid.
        final String currentuserid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid(); // Begenen Kullanıcı id.


        // Bu Database Ref ile ValueEventListener verilerni çekiyoruz.
        mLikeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (LikeStatus) { // Like Status true ise
                    // Likes altındaki Post id ye ait begenen Kullancılar id listesinde bu kullanıcı varsa.
                    if (dataSnapshot.child(post_id).hasChild(currentuserid)) {
                        // Eğer bu kullanıcı bu postu daha önce begendiyse begeni silinsin.
                        mLikeDatabase.child(post_id).child(currentuserid).removeValue();
                        LikeStatus = false; // Begeni durumu false..
                    } else {
                        // Eğer Daha önce begenmediyse Yeni value olarak eklensin.
                        mLikeDatabase.child(post_id).child(currentuserid).setValue(ServerValue.TIMESTAMP);
                        LikeStatus = false; // Begeni durumu false..
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    // item sayısı.
    @Override
    public int getItemCount() {
        return postList.size();
    }
}
