package findik.mustafa.monogram;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    //Degişkenleri tanımladık
    private EditText mUserMail;
    private Button mSendBtn;
    private LinearLayout mSignUp;
    private ProgressBar mProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Status bar :: Transparent
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);

        //Inıtalize
        mAuth = FirebaseAuth.getInstance();
        mUserMail = findViewById(R.id.forget_mailtext);
        mSendBtn = findViewById(R.id.forget_send_btn);
        mProgress = (ProgressBar) findViewById(R.id.forget_progress);
        mSignUp = findViewById(R.id.sign_up_liner);

        // Göndere basına RestartPassword metodu çalışsak
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestartPassword();
            }
        });

        // Kayıt ol butonuna basınca kayıt olma sayfasına gidicek.
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoSignUp = new Intent(ForgetPasswordActivity.this,SignUpActivity.class);
                startActivity(gotoSignUp);
                finish();
            }
        });
    }

    private void RestartPassword() {
        final String emailAddress = mUserMail.getText().toString(); // Mail adresi alnır.
        if (!TextUtils.isEmpty(emailAddress)) {
            mProgress.setVisibility(View.VISIBLE); //progress bar görünür.
            mAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) { //şifre resetleme metodu ile CompleteListener kullanılırç
                    if (task.isSuccessful()) { // Başarılıysa
                        //bildirim.
                        Toast.makeText(ForgetPasswordActivity.this, "Şifre Sıfırlama Maili Gönderildi.", Toast.LENGTH_SHORT).show();
                        //Giriş sayfasına gidilir.
                        Intent gotoSignIn = new Intent(ForgetPasswordActivity.this,SignInActivity.class);
                        // Task temizlenir.
                        gotoSignIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(gotoSignIn);
                        finish();
                    } else {
                        //Mail gitmesse bildirim
                        Toast.makeText(ForgetPasswordActivity.this, "Mail Gönderilirken Bir Hata Oluştu.", Toast.LENGTH_SHORT).show();
                    }
                    //progress bar görünmez olur.
                    mProgress.setVisibility(View.INVISIBLE);
                }
            });

        } else {
            //Mail alanı Boşşsa bildirim.
            Toast.makeText(this, "Mail Adresi Alanı Boş Olamaz.", Toast.LENGTH_SHORT).show();
        }
    }
}
