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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    //Değişkenler tanımlamlanır.
    private EditText mUserMail;
    private EditText mUserPassword;
    private EditText mUserPasswordConfirm;
    private Button mContiunebtn;
    private LinearLayout mGOOGLESign;
    private LinearLayout mSignIn;
    private ProgressBar mProgress;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Status bar :: Transparent
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);


        //initalize.
        mAuth = FirebaseAuth.getInstance();

        mUserMail = findViewById(R.id.sign_up_mailtext);
        mUserPassword = findViewById(R.id.sign_up_password);
        mUserPasswordConfirm = findViewById(R.id.sign_up_password_confirm);
        mContiunebtn = findViewById(R.id.sign_up_btn);
        mGOOGLESign = findViewById(R.id.sign_up_google_liner);
        mSignIn = findViewById(R.id.sign_up_liner);
        mProgress = (ProgressBar) findViewById(R.id.sign_up_progress);


        //İlerle Butonu basınca
        mContiunebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //layouttan veriler alınır.
                String email = mUserMail.getText().toString();
                String password = mUserPassword.getText().toString();
                String passwordConfirm = mUserPasswordConfirm.getText().toString();

                // veriler boş değilse.
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConfirm)){
                    if (password.equals(passwordConfirm)){ //şifre ile şifre confirm aynıysa.
                        mProgress.setVisibility(View.VISIBLE); // progress bar görünür olur.
                        // Firebase Kayıt metodu SuccessListener ile çağrılır.
                        mAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //başarılıysa
                                mProgress.setVisibility(View.INVISIBLE); // Progress bar gizlenir.
                                //bildirim.
                                Toast.makeText(SignUpActivity.this, "Kayıt Başarıyle Gerçekleştirildi.", Toast.LENGTH_SHORT).show();
                                //İkinci Kısma git.
                                Intent goToSecond = new Intent(SignUpActivity.this,SignUpProfileActivity.class);
                                startActivity(goToSecond);
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Kayıt başarısız olursa.
                                mProgress.setVisibility(View.INVISIBLE);
                                Toast.makeText(SignUpActivity.this, "Bir Hata Oluştu. Hata" +e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        //şifreler eşleşmesse.
                        Toast.makeText(SignUpActivity.this, "Şifreler Eşleşmiyor.", Toast.LENGTH_SHORT).show();
                    }

                }else  {
                    //Alanlardan en az biri boşşsa
                    Toast.makeText(SignUpActivity.this, "Alanlardan En Az Bir Tanesi Boş.", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // Giriş yap butonuna basılınca SignIn Act. gider.
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GoToSignIn = new Intent(SignUpActivity.this,SignInActivity.class);
                startActivity(GoToSignIn);
                finish();
            }
        });



    }

    protected void onStart() {
        // Eğer üye giriş yapmıssa otomatik Main Act. gider.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            Intent GoToMain = new Intent(SignUpActivity.this,MainActivity.class);
            // Taskı temizler.
            GoToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(GoToMain);
            finish();
        }
        super.onStart();
    }
}
