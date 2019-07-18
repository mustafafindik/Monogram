package findik.mustafa.monogram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import findik.mustafa.monogram.adapters.CommentAdapter;
import findik.mustafa.monogram.classes.Comment;

import static java.util.Objects.requireNonNull;

public class CommentActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private CircleImageView muUserPhoto;
    private FirebaseUser currentUser;
    private EditText mUserComment;
    private TextView mSendBtn;
    String postid;
    String user_id;

    private List<Comment> commentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommentAdapter mAdapter;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mCommentDatabase;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        postid = intent.getStringExtra("postId");

        muUserPhoto =findViewById(R.id.profile_user_image);
        mUserComment =findViewById(R.id.post_comment);
        mSendBtn = findViewById(R.id.comment_send_btn);




        Toolbar mToolbar = findViewById(R.id.comment_toolbar);
        mToolbar.setTitle("Yorum Ekle"); // Toolbar title.
        setSupportActionBar(mToolbar);
        // Toolbar geri butonu. Manifeste Parent Activitiy eklemeyi unutma
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Kullanıcı Id si.
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        user_id = currentUser.getUid();

        // İnitalize.
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Post listesini Eklemek için adapter oluşturup Postlistesini Ekliyoruz.
        mAdapter = new CommentAdapter(commentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter); // Adapteri Ekliyoruz. recyclerView'e

        mCommentDatabase = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);
        query = mCommentDatabase.orderByChild("NegativeDate");
        query.keepSynced(true); // Verileri Cache de tutmak için

        // Yukarıda aşagıya dogru kaydırıldığında reload.
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                PrepareComment();

            }
        });



        Picasso.with(CommentActivity.this).load(currentUser.getPhotoUrl()).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.color.defaultImage).into(muUserPhoto, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(CommentActivity.this).load(currentUser.getPhotoUrl())
                        .placeholder(R.color.defaultImage).into(muUserPhoto);
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendComment();
            }
        });

        PrepareComment();

    }

    private void PrepareComment() {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot commentSnapshot) {
                commentList.clear();
                for (final DataSnapshot commentsingle : commentSnapshot.getChildren()){
                    final Comment comment  = new Comment();
                    comment.setUserComment(requireNonNull(commentsingle.child("userComment").getValue()).toString());
                    comment.setPostDate(requireNonNull(commentsingle.child("Date").getValue()).toString());

                    String userId = commentsingle.child("UserId").getValue().toString();
                    comment.setUserId(userId);

                    mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    mUserDatabase.keepSynced(true);

                    mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            comment.setUserName(userSnapshot.child("username").getValue().toString());
                            comment.setUserImage(userSnapshot.child("profilePhoto").getValue().toString());
                            commentList.add(comment); // Posttali tüm bildiler postliste eklenir.

                            mSwipeRefreshLayout.setRefreshing(false); // Swipe devre dışı bırakılır.
                            mAdapter.notifyDataSetChanged(); //değişiklik oldugunda adapteri güncelle.

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(CommentActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                mSwipeRefreshLayout.setRefreshing(false); // Swipe devre dışı bırakılır.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CommentActivity.this, "Error", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false); // Swipe devre dışı bırakılır.
            }

        });
    }


    private void SendComment() {
        mSendBtn.setClickable(false);
        Long tsLong = System.currentTimeMillis();
        long negativeTS = -1 * tsLong; // Eklenen Postları Son Eklenen En ÜSTTE çıksın diye eklediğimiz Negatif TIME.

        UUID uuıd = UUID.randomUUID(); // Yeni bir Uniqie Id. Post id si
        String uuidString = uuıd.toString();

        String userComment =  mUserComment.getText().toString();
        if(!TextUtils.isEmpty(userComment)){
            if (userComment.length()<= 100){
                Map hashMap = new HashMap();
                hashMap.put("userComment", userComment);
                hashMap.put("Date", ServerValue.TIMESTAMP);
                hashMap.put("NegativeDate", negativeTS);
                hashMap.put("UserId", user_id);

                mCommentDatabase.child(uuidString).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        PrepareComment();
                        hideSoftKeyboard(CommentActivity.this);
                        mUserComment.setText(null);
                        Toast.makeText(CommentActivity.this, "Yorum Gönderildi", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CommentActivity.this, "Yorum Gönderilmedi.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(this, "Yorumunuz 100 Karekterden Büyük Olamaz.", Toast.LENGTH_SHORT).show();
            }

        }else {

            Toast.makeText(this, "Yorum Alanı Boş", Toast.LENGTH_SHORT).show();
        }
        mSendBtn.setClickable(true);
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
