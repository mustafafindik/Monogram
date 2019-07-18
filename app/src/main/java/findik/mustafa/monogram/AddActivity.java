package findik.mustafa.monogram;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class AddActivity extends AppCompatActivity {

    // Kullanacağımız View'daki toolları tanımlıyoruz.
    private Uri uri; // Main yada Profile Activity'den CropImage ile geliyor.Her yerde kullanmak için En üstte.
    private Bitmap thumb_bitmap;
    private ImageView postImage;
    private ImageView sharePost;
    private EditText postComment;
    private TextView postLocation;
    private Switch postCommentSwitch;
    private ProgressDialog mProgressDialog;

    private FirebaseUser currentUser;
    private StorageReference mStorageRef;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);


        Toolbar mToolbar = findViewById(R.id.add_toolbar); // Toolbar Tanımlıyoruz.
        mToolbar.setTitle("Şurada Paylaş"); // Toolbardaki Yazı
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //Geri dönüş Butonu

        // layout'tan initalize ediyoruz.
        uri = getIntent().getData(); // Main yada Profile Activity'den CropImage Dan Kesilmiş gelen Uri.
        postImage = findViewById(R.id.post_image);
        sharePost = findViewById(R.id.post_share);
        postComment = findViewById(R.id.post_comment);
        postLocation = findViewById(R.id.post_location);
        postCommentSwitch = findViewById(R.id.comment_false_switch);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String user_id = currentUser.getUid();

        File thumb_filePath = new File(uri.getPath()); //Uri Yolu.
        thumb_bitmap = null;
        try {
            thumb_bitmap = new Compressor(this) //Compress ediyoruz. yüzde 50 kalite ile .
                    .setQuality(50)
                    .compressToBitmap(thumb_filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        postImage.setImageBitmap(thumb_bitmap); //Compress edilmiş Resmi AddActivitydeki resin alanına attık

        sharePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharePost();
            }
        });  // Paylaş Butonu basıldıktan Sonra SharePost() Metodu Çalışşsın

        postLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoSelectLocation = new Intent(AddActivity.this,SelectLocationActivity.class);
                startActivityForResult(gotoSelectLocation,3);
                //Konum Almak için SelectLocationActivity'e gidiyoruz. Seçim yapıldıktan sonra Add Activity'e otomatik Dönecek.
                // Veri kaybı yaşamamak (Main'den yada Profile'den gelen Üri) için startActivityForResult kullanıryoz.
                // onActivityResult metodu ile Locastonu alıryoz. (Intent Put Extra Metodu)

             }
        });

    }

    @Override
    public void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        if (requestCode == 3) {
            if(resultCode == Activity.RESULT_OK){ // Seçin yapılmışşsa Lokaston Layouta ekleniyor.
                String result=data.getStringExtra("Location");
                postLocation.setText(result);
            }
            if(resultCode == Activity.RESULT_CANCELED){


            }
        }
    }

    private void SharePost() {
        final String userComment = postComment.getText().toString(); // Kullanıcı Yorumu
        String Location ="";
        if ( !postLocation.getText().toString().equals("Konum Ekle")){ // Lokasyon "Konum Ekle" olan default text den farklıysa Konumuda alıyoruz.
                Location = postLocation.getText().toString();
        }
        final String userLocation = Location;
        final boolean commentStatus = postCommentSwitch.isChecked(); // Şuan Kullanılmıyor ama Yorumlar açık mı kapalı mı.
        final String current_uid = currentUser.getUid(); //Userid

        if (thumb_bitmap !=null) { // Resim varsa
            mProgressDialog = new ProgressDialog(AddActivity.this); //Dialog , Popup açar.
            mProgressDialog.setTitle("Gönderi Paylaşılıyor...");
            mProgressDialog.setMessage("Lütfen Bekleyiniz..");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show(); //Önemli

            ByteArrayOutputStream baos = new ByteArrayOutputStream(); // Resmi Byte'a çeviriyoruz.
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            final byte[] thumb_byte = baos.toByteArray();


            UUID uuıdimage = UUID.randomUUID(); // Resim İçin Bir Unique İd
            String imageName = "FeedImages/" +uuıdimage+".jpg"; // Resim yolu

            StorageReference reference = mStorageRef.child(imageName); // Dosya Ekleme referansı.

            //Resmi Firebase Dosya sistemine bu yolla ekliyoruz.Ve Başarılı oldu mu diye Listener Ekliyorz.
            reference.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //Eğer Başarılıysa Olacaklar
                    final String download_url = Objects.requireNonNull(taskSnapshot.getDownloadUrl()).toString(); //Resmin Firebasedeki URL i
                    UUID uuıd = UUID.randomUUID(); // Yeni bir Uniqie Id. Post id si
                    String uuidString = uuıd.toString();
                    // Database JSON tipi
                    myRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(uuidString); //Post -> UUID -> pOSTBİLGİLERİ
                    Long tsLong = System.currentTimeMillis();
                    long negativeTS = -1 * tsLong; // Eklenen Postları Son Eklenen En ÜSTTE çıksın diye eklediğimiz Negatif TIME.
                    //SADECE SIRALAMADA KULLANILACAK.

                    Map hashMap = new HashMap(); // Hasmhmap a bilgileri ekliyoruz.
                    hashMap.put("postImage", download_url);
                    hashMap.put("userComment", userComment);
                    hashMap.put("userId", current_uid);
                    hashMap.put("userLocation", userLocation);
                    hashMap.put("userCommentStatus", commentStatus);
                    hashMap.put("PostDate", ServerValue.TIMESTAMP);
                    hashMap.put("NegavivePostDate", negativeTS);


                    // Veritabanıne Bu Metodlar Hashmap ı Ekliyoruz. Ve CompleteListener ekliyoruz.( Success Listenerde olabilir)
                    myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){ // Tamamlandıktan Sonra Başarılıysa
                                Toast.makeText(AddActivity.this, "Gönderi Paylaşıldı", Toast.LENGTH_SHORT).show(); // Toast eklitozu.
                                mProgressDialog.dismiss(); // Açılana Popup kapatılır.
                                Intent GoToMain = new Intent(AddActivity.this, MainActivity.class); //Main Act. Intent Oluşturduk.
                                // Geri Dönülmesini Engellemek için Task temizlenir. Önemli
                                GoToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(GoToMain); // Git
                                finish(); // Bitir.
                            }else {
                                Toast.makeText(AddActivity.this, "Gönderi Paylaşılırken Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
                                //Eger Gönderi başarılı olmassa Uyarı bildirimi
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddActivity.this, "Gönderi Paylaşılırken Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
                    // Eğer Sistemi Dosyayı atarken hata olursa Uyarı bildirimi
                }
            });
        }else{
            Toast.makeText(this, "Resim Bulunamadı", Toast.LENGTH_SHORT).show();
            //Resim yoksa Uyarı bildirimi.
        }

    }


}
