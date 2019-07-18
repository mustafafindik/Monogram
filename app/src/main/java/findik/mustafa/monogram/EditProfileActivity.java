package findik.mustafa.monogram;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity {


    //Kullanılacak değişkenler tanımlanır.

    private DatabaseReference mUsersDatabase;

    private ImageView mSaveProfileChanges;
    private RelativeLayout mChangeProfilePhoto;
    private EditText mUserFullname;
    private EditText mUserName;
    private EditText mUserStatus;
    private EditText mUserPhone;
    private ProgressBar mEditProgress;
    private CircleImageView mUserPhoto;
    private Bitmap thumb_bitmap;
    private FirebaseUser currentUser;
    private ProgressDialog mProgressDialog;

    private StorageReference mStorageRef;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Toolbar.
        Toolbar mToolbar = findViewById(R.id.profile_Edit_toolbar);
        mToolbar.setTitle("Profili Düzenle"); // Toolbar title.
        setSupportActionBar(mToolbar);
        // Toolbar geri butonu. Manifeste Parent Activitiy eklemeyi unutma
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        // Kullanıcı Id si.
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String user_id = currentUser.getUid();


        // Firebase veritabanında Bu kullanıcıya ait bilgileri çekmek için oluşturdugumuz ref. User -> userıd altındaki bilgileri
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUsersDatabase.keepSynced(true); //Önemli. Gelen verileri Cache da tutar.


        // Initalize.
        mSaveProfileChanges = findViewById(R.id.edit_profile_save);
        mChangeProfilePhoto = findViewById(R.id.profile_user_liner);
        mUserFullname = findViewById(R.id.edit_profile_fullname);
        mUserName = findViewById(R.id.edit_profile_username);
        mUserStatus = findViewById(R.id.edit_profile_status);
        mUserPhone = findViewById(R.id.edit_profile_Phonenumber);
        mEditProgress = findViewById(R.id.edit_profile_progressbar);
        mUserPhoto = findViewById(R.id.profile_user_image);

        // Firebase Dosya sistemi Referansı
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mUsersDatabase.addValueEventListener(new ValueEventListener() { // Bu Referanstaki verileri çekmek için ValueEventListener kullanılır.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // çekilen veriler String degişkenlere atanır. Tek Parça veri geldiği için . Aksi halde for kullanılırç
                final String Full_name = dataSnapshot.child("fullName").getValue().toString();
                final String user_name = dataSnapshot.child("username").getValue().toString();
                final String user_phonenumber = dataSnapshot.child("phoneNumber").getValue().toString();
                final String status = dataSnapshot.child("status").getValue().toString();
                final String Profile_image = dataSnapshot.child("profilePhoto").getValue().toString();

                // Gelen veriler layouta set edilir.
                mUserName.setText(user_name);
                mUserFullname.setText(Full_name);
                mUserPhone.setText(user_phonenumber);
                mUserStatus.setText(status);

                // Resimler Uri şeklinde geldiği için Picasso yardımı ile set edilir. NetworkPolicy Ofline demek cache tut demek.
                Picasso.with(EditProfileActivity.this).load(Profile_image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.color.defaultImage).into(mUserPhoto, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(EditProfileActivity.this).load(Profile_image)
                                .placeholder(R.color.defaultImage).into(mUserPhoto);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Profil resmi değiştirmek için.
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resimleri Erişim izni var mı kontrol edilir.
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},0); // Yoksa izin Alınır.
                }else {
                    //izin varsa cropImage ile resim seçilir ve kesilir.
                    CropImage.activity()
                            .setAspectRatio(1, 1) //ORAN
                            .setMinCropWindowSize(500, 500) //Boyut.
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(EditProfileActivity.this);
                }
            }
        });

        mSaveProfileChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveChanges(); //Değişikleri kaydet butonuna basınca SaveChanges metodu çalışır.
            }
        });



    }

    private void SaveChanges() {
        // Layouta yazılan bilgiler alınır.
        final String userName = mUserName.getText().toString();
        final String userFullname = mUserFullname.getText().toString();
        final String userStatus = mUserStatus.getText().toString();
        final String userPhoneNumber = mUserPhone.getText().toString();

        final String current_uid = currentUser.getUid(); //kullanıcı id.

        if (!TextUtils.isEmpty(userName)){ //Username alanı zorunlu. boş değilse.
            mEditProgress.setVisibility(View.VISIBLE); //progress bar çalışır.

            if (thumb_bitmap !=null) { //resim varsa
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                final byte[] thumb_byte = baos.toByteArray(); //resim Byte çevrilir.

                String imageName = "ProfilePhotos/" + current_uid + ".jpg"; //Profil resmi olduqundan User id kullanılarak Dosya sistemine atılırç

                // Resmi Firebase Dosya sistemine eklitoruz.
                StorageReference reference = mStorageRef.child(imageName);
                //Resim Başarıyla eklenmişmi kontrol etmek için  SuccessListener kullanıyoruzç.
                reference.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Eklenen resmin yolu firebase'de
                        final String download_url = Objects.requireNonNull(taskSnapshot.getDownloadUrl()).toString();
                        // Kullanıcı adını ve Üye resmini Auth sistemindede güneclliyoruz.
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .setPhotoUri(taskSnapshot.getDownloadUrl())
                                .build();

                        // Update ettikten sonra CompleteListener ile ile tamamlanma durumuna bakıyoruz.
                        currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) { // Başarıyla güncellenmişse.
                                    // Firebase Veritabındali Users -> userid ref alıp günceelliyeceqz.
                                    myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                                    // Verileri Map a ekliyoruz.
                                    Map hashMap = new HashMap();
                                    hashMap.put("profilePhoto", download_url);
                                    hashMap.put("username", userName);
                                    hashMap.put("fullName", userFullname);
                                    hashMap.put("status", userStatus);
                                    hashMap.put("phoneNumber", userPhoneNumber);

                                    //referensa Set verile Update ile Complete ediyoruz.
                                    myRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) { // Başasıylısa
                                                mEditProgress.setVisibility(View.INVISIBLE); //progressbarı gizle

                                                //profile git
                                                Intent GoToProfile = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                                //geri gönüşü engellemek için Taskı siliyoruz.
                                                GoToProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(GoToProfile);
                                            } else {
                                                // başarısızsa bildirim.
                                                Toast.makeText(EditProfileActivity.this, "Güncelleme Yapılırken Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                } else {
                                    //Profil güncellenirken hata olursa bildirim.
                                    Toast.makeText(EditProfileActivity.this, "Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { // Dosta Sistemine resim eklenirken fal olmussa hata
                        Toast.makeText(EditProfileActivity.this, "Bir Hata oluştu.", Toast.LENGTH_SHORT).show();
                    }
                });

            }else{ // Eğer resim değiştirilmesse aynı işlemler resimsiz devam eder.
                {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(userName)
                            .build();

                    currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                                Map hashMap = new HashMap();
                                hashMap.put("username", userName);
                                hashMap.put("fullName", userFullname);
                                hashMap.put("status", userStatus);
                                hashMap.put("phoneNumber", userPhoneNumber);

                                myRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mEditProgress.setVisibility(View.INVISIBLE);

                                            Intent GoToProfile = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                            GoToProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(GoToProfile);
                                        } else {
                                            Toast.makeText(EditProfileActivity.this, "Güncelleme Yapılırken Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }


        }
        else {
            //Kullanıcı adı boşsa bildirim.
            Toast.makeText(this, "Kullanıcı Adı Alanı Boş Olamaz.", Toast.LENGTH_SHORT).show();
        }

    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(500, 500)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(EditProfileActivity.this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());

                mProgressDialog = new ProgressDialog(EditProfileActivity.this);
                mProgressDialog.setTitle("Resim Yükleniyor...");
                mProgressDialog.setMessage("Lütfen Bekleyiniz..");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(120)
                            .setMaxHeight(120)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mProgressDialog.dismiss();
                mUserPhoto.setImageBitmap(thumb_bitmap);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
