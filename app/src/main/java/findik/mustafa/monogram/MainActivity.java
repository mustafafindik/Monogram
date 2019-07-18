package findik.mustafa.monogram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

import findik.mustafa.monogram.adapters.PostAdapter;
import findik.mustafa.monogram.classes.Post;

import static java.util.Objects.requireNonNull;

public class MainActivity extends AppCompatActivity {

    private static int TOTAL_ITEM_EACH_LOAD = 2;
    private int currentPage = 0;

    // Değişkenler tanımladık
    private FirebaseAuth mAuth;
    private List<Post> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PostAdapter mAdapter;
    private DatabaseReference mPostsDatabase;
    private DatabaseReference mUserDatabase;
    Query query;
    private ImageView mChatListBtn;

    private AppCompatImageView btnHome,btnDisc,btnAdd,btnNof,btnProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnHome=findViewById(R.id.m_home);
        btnDisc=findViewById(R.id.m_disc);
        btnAdd=findViewById(R.id.m_add);
        btnNof=findViewById(R.id.m_nof);
        btnProfile=findViewById(R.id.m_profile);


        // İnitalize.
        mAuth = FirebaseAuth.getInstance();
        mChatListBtn = findViewById(R.id.main_chat_list_btn);
        //Post Ref .
        mPostsDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        query = mPostsDatabase.orderByChild("NegavivePostDate");
        query.keepSynced(true);
        // Burada gelen verileri NegavivePostDate ile sıralıyoruz. En son Eklenen En üste geçsin diye.


        // İnitalize.
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Post listesini Eklemek için adapter oluşturup Postlistesini Ekliyoruz.
        mAdapter = new PostAdapter(postList, this);
        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter); // Adapteri Ekliyoruz. recyclerView'e


        mChatListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               
                Intent gotoChatList = new Intent(MainActivity.this, ChatListActivity.class);

                startActivity(gotoChatList);


            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToHome = new Intent(MainActivity.this, MainActivity.class);
                goToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToHome);
                finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewPost();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(goToProfile);


            }
        });

        // Yukarıda aşagıya dogru kaydırıldığında reload.
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                getPosts(); // Veriler tekrar çekilio yüklenir.
            }
        });


        // ilk sayfa açılığında veriler yüklenir.
        getPosts();

    }

    private void getPosts() {
        postList.clear(); //Postlist temizlenir. Çünkü Her Swipe da üstüne eklemesin diye.
        query.addValueEventListener(new ValueEventListener() { // query ref. Valuelistener eklenir.Firebaseden verileri çekmek için.
            @Override
            public void onDataChange(DataSnapshot postSnapshot) {

                for (final DataSnapshot postsingle : postSnapshot.getChildren()) { // Gelen veriler liste oldugundan dolayı for.

                    final Post post = new Post();
                    // Post Modulu oluşturduk. Çünkü gelen verilerin hepsi tek teK Postlist e eklenecek.

                    String postid = postsingle.getKey().toString(); //Post'un Unique İd si.

                    //ref den gelen veriler post'a eklenir.
                    post.setUserLocation(requireNonNull(postsingle.child("userLocation").getValue()).toString());
                    post.setUserComment(requireNonNull(postsingle.child("userComment").getValue()).toString());
                    post.setPostImage(requireNonNull(postsingle.child("postImage").getValue()).toString());
                    post.setPostDate(requireNonNull(postsingle.child("PostDate").getValue()).toString());
                    post.setPostId(postid);

                    String userid = requireNonNull(postsingle.child("userId").getValue()).toString();
                    post.setUserId(userid);
                    // postu ekleye kullanıcıya ait verileri çekmek için 2.bir ref kullanılır.
                    mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
                    mUserDatabase.keepSynced(true);


                    // Kullanıcı verilerini çekmek için single value Listener.
                    mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            //kulanıcı adı ve resmide post a eklenir.
                            post.setUserName(userSnapshot.child("username").getValue().toString());
                            post.setUserImage(userSnapshot.child("profilePhoto").getValue().toString());
                            postList.add(post); // Posttali tüm bildiler postliste eklenir.

                            mAdapter.notifyDataSetChanged(); //değişiklik oldugunda adapteri güncelle.
                            mSwipeRefreshLayout.setRefreshing(false); // Swipe devre dışı bırakılır.
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //User den veri çekemessek bildirim
                            Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Post ref den veri çekemessek bildirim.
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }




    @Override
    protected void onStart() {
        // Başlangıçta User giriş yapmamışşsa Giriş yapma sayfasına gönderilir.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent GoToSignIn = new Intent(MainActivity.this, SignInActivity.class);
            // Task temizlenir.Geri dönememesi için.
            GoToSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(GoToSignIn);
            finish();
        }
        super.onStart();
    }


    private void AddNewPost() {
        // İzin var mı Yok mu ? Resim eklemek için.
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //İzin Yoksa izin Al.
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            // İzin varsa resim seç,Kırp ve Result'a gönder.
            CropImage.activity()
                    .setAspectRatio(1, 1)//oran
                    .setMinCropWindowSize(600, 600) // YÜKSEKLİK VE GENİŞLİK
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildikten sonra. resim seç,Kırp ve Result'a gönder.
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(600, 600)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            // Resmi seçip kırtıkten sonra.
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                // resim Uri.
                Uri resultUri = result.getUri();

                // Bu Uri AddAct. gider.
                Intent gotoAdd = new Intent(this, AddActivity.class);
                gotoAdd.setData(resultUri); // İntent ile Resim Uri yollama.
                startActivity(gotoAdd);
                finish();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Result Ok değilse.
                Toast.makeText(this, "Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
