package findik.mustafa.monogram;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    // Kullanılacak değişteknler
    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mNewPasswordConfirm;
    private ProgressBar mProgress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //Toolbar.
        Toolbar mToolbar = findViewById(R.id.change_password_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Şifre Değiştir"); //Toolbar title.
        // Toolbar geri dönüş Butonu. Manifeste Parent Activity Eklemeyi Unutma
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Initalize.
        mAuth = FirebaseAuth.getInstance();
        ImageView mSaveNewPassword = findViewById(R.id.change_password_save);
        mOldPassword = findViewById(R.id.change_password_oldPassword);
        mNewPassword = findViewById(R.id.change_password_NewPassword);
        mNewPasswordConfirm = findViewById(R.id.change_password_NewPasswordConfirm);
        mProgress = findViewById(R.id.change_password_progressbar);

        // Şifre degiştir Butonuna basınca
        mSaveNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = mAuth.getCurrentUser(); //Şuanki Kullanıcı

                assert user != null;
                final String userMail = Objects.requireNonNull(user.getEmail()); //User Mail
                final String userOldPassword = mOldPassword.getText().toString(); // Eski Şifre
                final String userNewPassword = mNewPassword.getText().toString(); //Yeni şifre
                final String userNewPasswordConfirm = mNewPasswordConfirm.getText().toString(); //Yeni şifre Tekra

                //Üyenin Bilgilerini değiştirmek için Credential Oluştuduk.Eski şifre dogru mu diye kontrol ediyoruz.
                AuthCredential credential = EmailAuthProvider.getCredential(userMail, userOldPassword);
                mProgress.setVisibility(View.VISIBLE); // İşlem süresince progress bar görünücek.
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if (userNewPassword.equals(userNewPasswordConfirm)){ // Yeni şifre ile confirm aynı mı
                                user.updatePassword(userNewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) { // Aynıysa şifreyi güncelle.Ve Complete Listener ekle
                                        if (task.isSuccessful()){ //İşlem başarılıysa
                                            // Bidirim.
                                            Toast.makeText(ChangePasswordActivity.this, "Şifre Başarıyla Güncellendi.", Toast.LENGTH_SHORT).show();
                                            // Profil gitmek için Intent.
                                            Intent gotoProfile = new Intent(ChangePasswordActivity.this,ProfileActivity.class);
                                            // Geri Dönüşü Engellemek için task clearr.
                                            gotoProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity( gotoProfile);
                                            finish();
                                        }else{
                                            // Eğer Başarılı olmassa bildirim.
                                            Toast.makeText(ChangePasswordActivity.this, "Şifre Güncellenirken Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                //Yeni şifre ile confirm aynı değilse.
                                Toast.makeText(ChangePasswordActivity.this, "Şifreler Eşleşmedi.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            //Credentil ada hata varsa.
                            Toast.makeText(ChangePasswordActivity.this, "Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                        }
                        mProgress.setVisibility(View.INVISIBLE); // progress barı gizle.
                    }
                });
            }
        });
    }
}
