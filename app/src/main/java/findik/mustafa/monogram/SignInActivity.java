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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {


    // Değişkenler tanımlanır.
    private EditText mUserMail;
    private EditText mUserPassword;
    private Button mLoginbtn;
    private TextView mForgetPassword;
    private LinearLayout mGOOGLELogin;
    private LinearLayout mSignUp;
    private ProgressBar mProgress;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Status bar :: Transparent
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);


        //Initalize.
        mAuth = FirebaseAuth.getInstance();

        mUserMail = findViewById(R.id.sign_in_mailtext);
        mUserPassword = findViewById(R.id.sign_in_password);
        mLoginbtn= findViewById(R.id.sign_in_login_btn);
        mForgetPassword = findViewById(R.id.sign_in_forget_password);
        mGOOGLELogin = findViewById(R.id.sign_in_google_liner);
        mSignUp = findViewById(R.id.sign_up_liner);

        mProgress = (ProgressBar) findViewById(R.id.sign_in_progress);

        //Girişe basılınca
        mLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Mail = mUserMail.getText().toString();
                String Password = mUserPassword.getText().toString();

                if (!TextUtils.isEmpty(Mail) && !TextUtils.isEmpty(Password)) {
                    mProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(Mail, Password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(SignInActivity.this, "Başarıyla Giriş Yaptınız.", Toast.LENGTH_SHORT).show();
                            mProgress.setVisibility(View.INVISIBLE);
                            goMain();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignInActivity.this, "Giriş Yapılırken Hata Oluştu.", Toast.LENGTH_SHORT).show();
                            mProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }else {
                    Toast.makeText(SignInActivity.this, "Alanlardan En Az Bir Tanesi Boş.", Toast.LENGTH_SHORT).show();
                }
                
              
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Kayıt ola basılınca SignUpAct. gider.
                Intent GoToSignUp = new Intent(SignInActivity.this,SignUpActivity.class);
                startActivity(GoToSignUp);

            }
        });

        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Şifremi unuttuma basılınca ForgetPasswordAct. gider.
                Intent GoToForgetPassword = new Intent(SignInActivity.this,ForgetPasswordActivity.class);
                startActivity(GoToForgetPassword);

            }
        });
    }

    @Override
    protected void onStart() { // Eğer Kullanıcı giril yapmıssa Main Activity ' e gider.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            goMain();
        }
        super.onStart();
    }

    public void goMain (){ //Main Act. intetn.
        Intent GoToMain = new Intent(SignInActivity.this,MainActivity.class);
        //geri gönüş engellemek için task temizlenir.
        GoToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(GoToMain);
        finish();
    }
}
