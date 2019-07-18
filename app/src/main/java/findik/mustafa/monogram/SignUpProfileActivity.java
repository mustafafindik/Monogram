package findik.mustafa.monogram;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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

public class SignUpProfileActivity extends AppCompatActivity {

    // Değişkenler tanımlanır.
    private EditText mUserName;
    private ProgressDialog mProgressDialog;
    private CircleImageView mProfileImage;
    private Bitmap thumb_bitmap;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_profile);

        // Status bar :: Transparent
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);

        //ınıtalize.
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        RelativeLayout mAddProfilePhoto = findViewById(R.id.sign_up_photo_layout);
        mUserName = findViewById(R.id.sign_up_userName);
        Button mFinishBtn = findViewById(R.id.sign_up2_finish_btn);
        mProfileImage = findViewById(R.id.sign_up_profile_image);



        // Profil ekle.
        mAddProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resimler Erişim izni var mı yok mU?
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //izin yoksa izin al.
                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},0);
                }else {
                    // izin varsa Resmi seç , kırp.
                    CropImage.activity()
                            .setAspectRatio(1, 1) //oran
                            .setMinCropWindowSize(500, 500) // Boyut.
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(SignUpProfileActivity.this);
                }
            }
        });


        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // izin varsa Resmi seç , kırp.
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(500, 500)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SignUpProfileActivity.this);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri(); // Resim Uri si
                File thumb_filePath = new File(resultUri.getPath()); // Resim yolu.

                mProgressDialog = new ProgressDialog(SignUpProfileActivity.this); // Popup.
                mProgressDialog.setTitle("Resim Yükleniyor..."); // başlık
                mProgressDialog.setMessage("Lütfen Bekleyiniz.."); //mesaj
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show(); // göster.

                thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this) // Resmi Copress ediyoruz.
                            .setMaxWidth(120)
                            .setMaxHeight(120)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mProgressDialog.dismiss(); // pupup kapanır.
                mProfileImage.setImageBitmap(thumb_bitmap); // layouta resim eklenir.

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // hata bildirim.
                Toast.makeText(this, "Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void Save() {
        // Layouttan bilgiler alınır.
        final String user_name = mUserName.getText().toString();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        final String current_uid = currentUser.getUid(); //user id.

        if (!TextUtils.isEmpty(mUserName.getText().toString())){ //username boş deilse.
            mProgressDialog = new ProgressDialog(SignUpProfileActivity.this); //popup açılır.
            mProgressDialog.setTitle("Kaydediliyor..."); //başlık
            mProgressDialog.setMessage("Lütfen Bekleyiniz.."); //mesaj
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show(); // göster.

            if (thumb_bitmap !=null) { // resim boş değilse.
                ByteArrayOutputStream baos = new ByteArrayOutputStream(); // resim compress edilr ve byte çevrilir.
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                final byte[] thumb_byte = baos.toByteArray();


                String imageName = "ProfilePhotos/" + current_uid + ".jpg"; // resim Uri si. User id ile.

                StorageReference reference = mStorageRef.child(imageName); // Firebase Dosya sistemine resim ekleme. ref.
                // referanse resim eklenir ve Success listener çalıştırışır.
                reference.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Resim Firebase dosya sistemndeli yolu.
                        final String download_url = Objects.requireNonNull(taskSnapshot.getDownloadUrl()).toString();
                        // Kullanıcı adı ve resim Auth sisteminde güncellenir.
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user_name)
                                .setPhotoUri(taskSnapshot.getDownloadUrl())
                                .build();

                        // Güncelleme metodu.  CompleteListener
                        currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) { // Başarılıysa.
                                    // User Firebase Veritabanına ref oluşturulur.
                                    myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                                    // bilgilere Map'a atılır.
                                    Map hashMap = new HashMap();
                                    hashMap.put("profilePhoto", download_url);
                                    hashMap.put("username", user_name);
                                    hashMap.put("fullName", "");
                                    hashMap.put("status", "Selam , Sayfama Hoşgeldiniz.");
                                    hashMap.put("phoneNumber", "");

                                    // SetValue ile Firebase Veritabanın atılır ve CompleteListener kullanılır.
                                    myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) { // Başarılıysa.
                                                mProgressDialog.dismiss(); // pupup kapanır.
                                                //Main Act. gider.
                                                Intent GoToMain = new Intent(SignUpProfileActivity.this, MainActivity.class);
                                                // Geri dönülmesini engellemek için task temizlenir.
                                                GoToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(GoToMain);
                                            } else {
                                                // Başarısızsa bildirim.
                                                Toast.makeText(SignUpProfileActivity.this, "Güncelleme Yapılırken Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                } else {
                                    // Uye güncellemesi başarısaısa bildirim.
                                    Toast.makeText(SignUpProfileActivity.this, "Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Ref başarısız olursa bildirim.
                        Toast.makeText(SignUpProfileActivity.this, "Bir Hata oluştu.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                // Aynı işlemler resim seçilmesse yapılır.
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(user_name)
                        .build();

                currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                            Map hashMap = new HashMap();
                            hashMap.put("profilePhoto", "default");
                            hashMap.put("username", user_name);
                            hashMap.put("fullName", "");
                            hashMap.put("status", "Selam , Sayfama Hoşgeldiniz.");
                            hashMap.put("phoneNumber", "");

                            myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mProgressDialog.dismiss();

                                        Intent GoToMain = new Intent(SignUpProfileActivity.this, MainActivity.class);
                                        GoToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(GoToMain);
                                    } else {
                                        Toast.makeText(SignUpProfileActivity.this, "Güncelleme Yapılırken Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }

        }else {
            // Kullanıcı adı boşşsa bildidrim.
            Toast.makeText(this, "Kullanıcı Adı Alanı Zorunludur", Toast.LENGTH_SHORT).show();
        }


    }


}
