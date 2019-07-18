package findik.mustafa.monogram;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    // Değişkenler tanımlanır.
    private TextView mProfileName;
    private TextView mProfileStatus;
    private TextView mUserPhone;
    private CircleImageView mProfilePhoto;
    private TextView mPostCount;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mFollowerCount;
    private TextView mFollowingCount;
    private Button mEditProfile;
    private GridView androidGridView;
    private Toolbar mToolbar;

    String user_name;
    private AppCompatImageView btnHome,btnDisc,btnAdd,btnNof,btnProfile;

    ArrayList<String> arrayList = new ArrayList<>();
    int width;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnHome=findViewById(R.id.m_home);
        btnDisc=findViewById(R.id.m_disc);
        btnAdd=findViewById(R.id.m_add);
        btnNof=findViewById(R.id.m_nof);
        btnProfile=findViewById(R.id.m_profile);

        // Şuanki Kullanıcı.
        final FirebaseUser currenuser = FirebaseAuth.getInstance().getCurrentUser();

        // Intent ile gelen User id. ( Eger basşa birinin profiline girilirse)
        String user_id = getIntent().getStringExtra("user_id");
        if (user_id == null) { // İntentden gelen Userid boşşsa
            if (currenuser == null) {
                throw new AssertionError();
            }
            user_id = currenuser.getUid(); // userid , Şuanki kullanıcı id sine eşit olsun.
        }

        // seçilen user'a ait bilgileri çekmek için oluşturulan Database Ref.
        DatabaseReference mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUsersDatabase.keepSynced(true); // Cache için.

        // Porfil kısmındaki Resimleri eklemek için oluşturulan Database Ref.
        DatabaseReference mPostDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        // Resimleri son eklenen en başta şeklinde göstermek için NegavivePostDate e göre sıraladık.
        Query query = mPostDatabase.orderByChild("NegavivePostDate");
        query.keepSynced(true); // Cache için.

        // İnitalize.
        androidGridView = findViewById(R.id.user_post_images);
        mEditProfile = findViewById(R.id.profile_edit_btn);
        mProfileName = findViewById(R.id.profile_fullname);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfilePhoto = findViewById(R.id.profile_user_image);
        mUserPhone = findViewById(R.id.profile_phone);
        mPostCount = findViewById(R.id.profile_user_post_count);
        mFollowerCount = findViewById(R.id.profile_user_follower_count);
        mFollowingCount = findViewById(R.id.profile_user_following_count);


        // Toolbar.
        mToolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Toolbar  geri butonu.
        mToolbar.inflateMenu(R.menu.navigation);

        // Burada resimleri her telefona göre 3'er tane sıgdırmak için telefonun genişliğini alıyoruz.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x; // Telefonun genişliği


        if (!user_id.equals(currenuser.getUid())) {
            mEditProfile.setText("Mesaj Gönder");
        }

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToHome = new Intent(ProfileActivity.this, MainActivity.class);
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
                Intent goToProfile = new Intent(ProfileActivity.this, ProfileActivity.class);
                startActivity(goToProfile);


            }
        });

        //Eğer kullanıcı kendi profilindeyse. basıldığında profilini düzenleyebicek.
        final String finalUser_id1 = user_id;
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finalUser_id1.equals(currenuser.getUid())) {
                    Intent gotoChat = new Intent(ProfileActivity.this, ChatActivity.class);
                    gotoChat.putExtra("Userid",finalUser_id1);
                    gotoChat.putExtra("userName",user_name);
                    startActivity(gotoChat);
                }else{
                    // Profil düzenlemek için EditProfile Act. intent.
                    Intent goToEditProfile = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    startActivity(goToEditProfile);
                }
            }
        });


        // Kullanıcı bildilerini Çekmek için oluşturulan Database ref. e ValueEventListener ekleyirız.
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Kullanıcı bildilerini String değişenlere aldık.
                 String Full_name = dataSnapshot.child("fullName").getValue().toString()==null?"":dataSnapshot.child("fullName").getValue().toString();
                user_name = dataSnapshot.child("username").getValue().toString();
                final String user_phonenumber = dataSnapshot.child("phoneNumber").getValue().toString();
                final String status = dataSnapshot.child("status").getValue().toString();
                final String Profile_image = dataSnapshot.child("profilePhoto").getValue().toString();

                Objects.requireNonNull(getSupportActionBar()).setTitle(user_name);

                // eğer Tam adı Soyadı yoksa onun yerine kullanıcı adı kullan.
                if (TextUtils.isEmpty(Full_name)) {
                    mProfileName.setText(user_name);
                } else {
                    mProfileName.setText(Full_name);
                }
                // Telefon numarasnı set .
                mUserPhone.setText(user_phonenumber);
                // Durumunu set .
                mProfileStatus.setText(status);

                //  Resimler Uri olduğundan Picasso yardımıysa eklenitoz resimleri
                // NetworkPolicy OFFLINE ile resimleri hafızada cache de tutuyoruz.
                Picasso.with(ProfileActivity.this).load(Profile_image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.color.defaultImage).into(mProfilePhoto, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ProfileActivity.this).load(Profile_image)
                                .placeholder(R.color.defaultImage).into(mProfilePhoto);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Kullanıcıya ait bilgiler çekilmesse.
                Toast.makeText(ProfileActivity.this, "Kullanıcı Bilgileri Çekilemedi.", Toast.LENGTH_SHORT).show();
            }
        });


        // Resimleri çekmek için.
        final String finalUser_id = user_id;
        // query'e ValueEventListener Ekliyoruz.
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot postSnapshot) {
                for (final DataSnapshot postsingle : postSnapshot.getChildren()) { // Gelen resimler liste olduqundan dolayı For.

                    //userid ye ait olan resimleri alıoruoz sadece
                    if (Objects.equals(postsingle.child("userId").getValue(), finalUser_id))
                        // Resimleri arrayliste ekliyoruz.
                        arrayList.add((Objects.requireNonNull(postsingle.child("postImage").getValue()).toString()));

                }
                int count = arrayList.size(); // Resim sayısı için.
                mPostCount.setText(String.valueOf(count)); //layouta resim sayısınu ekliyoruz.

                // Image adapter ile resimleri gridView ekledik.
                androidGridView.setAdapter(new ImageAdapter(ProfileActivity.this));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Resimler alınmassa bildirim.
                Toast.makeText(ProfileActivity.this, "Kullanıcı Resimleri Alınırken Hata Oluştu.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Resimleri Gridview 'e eklemek için Adapter.
    public class ImageAdapter extends BaseAdapter {
        private Context mContext; //Context.


        public ImageAdapter(Context c) {
            mContext = c;
        }

        //Resim sayısı.
        public int getCount() {
            return arrayList.size();
        }

        // İtem çekmek için.
        @Override
        public String getItem(int position) {
            return arrayList.get(position);
        }

        //itemid. Sıfır gönder.
        public long getItemId(int position) {
            return 0;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext); // Yeni İmageview.
                // Boyutları. Telefon boyutlarının 3 e bölümü genişlik ve yükseklik.
                imageView.setLayoutParams(new GridView.LayoutParams(width / 3, width / 3));
                imageView.setPadding(1, 1, 1, 1); //padding.
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); //Scaletype.

            } else {
                imageView = (ImageView) convertView;
            }
            final String url = getItem(position); // Resimin Url si.

            //  Resimler Uri olduğundan Picasso yardımıysa eklenitoz resimleri
            // NetworkPolicy OFFLINE ile resimleri hafızada cache de tutuyoruz.
            Picasso picasso = Picasso.with(ProfileActivity.this);
            picasso.setIndicatorsEnabled(false); // Solüstteki Üçgen rekleri kapatmak için.
            picasso.load(url).placeholder(R.color.defaultImage).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ProfileActivity.this).load(url)
                            .placeholder(R.color.defaultImage).into(imageView);
                }
            });
            return imageView;
        }
    }


    // Toolbardaki Menüyü infilate ediyoruz.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    // Menüdeki itemlere tıklanıldığında.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            FirebaseAuth.getInstance().signOut(); // çıkış yap.
            // SignIn Act. git.
            Intent goToSignIn = new Intent(ProfileActivity.this, SignInActivity.class);
            // Task'ı temizle geri dönemesin.
            goToSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(goToSignIn);
            finish();
        }
        if (item.getItemId() == R.id.change_password) { //şifre depiştirme İntentine git.
            Intent goToChangePass = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(goToChangePass);
            finish();
        }

        if (item.getItemId() == R.id.close_account) {
            // Üyeliğimi Sile başınca Popup açılır.
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setTitle("Üyeliğimi Sil");  // popup başlık
            builder.setMessage("Emin Misiniz ? "); // popup Soru.
            builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() { //Negatif buton tanımı ve Text'i
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Negatif Butona basınca olacaklar.
                }
            });
            // Pozitigf Buton tanımı ve Text'i
            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Evet'e basınca olacaklar.
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //Şuanku Kullanıcı
                    user.delete() // Üyeyi sil.
                            .addOnCompleteListener(new OnCompleteListener<Void>() { // Complete lister ile bak.
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) { // Başarıyla silinirse.
                                        //bildirim.
                                        Toast.makeText(ProfileActivity.this, "Üyeliğiniz Başarıyla Silindi.", Toast.LENGTH_SHORT).show();
                                        //SignIn Activity e git.
                                        Intent GotoSignIn = new Intent(ProfileActivity.this, SignInActivity.class);
                                        //Task'i sil geri dönemesin.
                                        GotoSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(GotoSignIn);
                                        finish();
                                    } else {
                                        // Başarısız olursa uyarı bildirimi.
                                        Toast.makeText(ProfileActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
            builder.show(); // Önemli. Alerti göster.

        }

        return super.onOptionsItemSelected(item);
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
