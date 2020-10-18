package com.example.e_treat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.example.e_treat.model.Patient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    TextView signUpView;
    Button signInBtn;
    EditText email, password;
    FirebaseAuth auth;
    private String TAG = "SIGNIN";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();

        signUpView = findViewById(R.id.sign_up_view);
        signInBtn = findViewById(R.id.sign_in_button);
        email = findViewById(R.id.user_email_text);
        password = findViewById(R.id.user_password_text);
        progressBar = findViewById(R.id.load_progress_bar);

        signUpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateUser(email.getText().toString(), password.getText().toString());
            }
        });

    }

    private void authenticateUser(String email, String password) {
        showDialog();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            Log.d(TAG, "signIn:: success "+ user.getUid());
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            intent.putExtra("id", user.getUid());
                            startActivity(intent);
                            finish();

                        } else {
                            Log.d(TAG, "signIn::failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }

                    }
                });

    }

    private void showDialog() {
        signInBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        signInBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }
}