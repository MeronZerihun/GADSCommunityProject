package com.example.e_treat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_treat.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText firstName, lastName, email, password, confirmPassword;
    Button signUpButton;
    TextView signInView;

    String TAG = "SIGNUP";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firstName = findViewById(R.id.first_name_text);
        lastName = findViewById(R.id.last_name_text);
        email = findViewById(R.id.email_text);
        password = findViewById(R.id.password_text);
        confirmPassword = findViewById(R.id.confirm_password_text);

        signUpButton = findViewById(R.id.sign_up_button);
        signInView = findViewById(R.id.sign_in_view);

        progressBar = findViewById(R.id.progress_bar);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !isEmpty(firstName.getText().toString())  && !isEmpty(lastName.getText().toString())
                        && !isEmpty(email.getText().toString())
                        && !isEmpty(password.getText().toString())
                        && !isEmpty(confirmPassword.getText().toString()
                        )){

                    if(doStringsMatch(password.getText().toString(), confirmPassword.getText().toString())){

                        //Initiate registration task
                        registerNewEmail(firstName.getText().toString(), lastName.getText().toString(), email.getText().toString(), password.getText().toString());

                    }else{
                        Toast.makeText(SignUpActivity.this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                        password.setText("");
                        confirmPassword.setText("");
                    }
                }
            }
        });

        signInView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginScreen();
            }
        });

    }

    private boolean isEmpty(String string){
        return string.equals("");
    }

    private void showDialog(){
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private boolean doStringsMatch(String s1, String s2){
        return s1.equals(s2);
    }

    private void openLoginScreen(){
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    /*private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Sent Verification Email", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(SignUpActivity.this, "Couldn't Verification Send Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }*/

    public void registerNewEmail(final String firstName, final String lastName, final String email, String password){
        showDialog();
        hideSignUpBtn();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        Log.d(TAG, firstName+"|"+lastName+"|"+email+"|"+password);

                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                            String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            User user = new User(firstName, lastName, email, uId);

                            FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.user_node))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseAuth.getInstance().signOut();

                                            //redirect the user to the login screen
                                            openLoginScreen();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignUpActivity.this, "Something went wrong. Try again!", Toast.LENGTH_SHORT).show();
                                    if(FirebaseAuth.getInstance() != null){
                                        FirebaseAuth.getInstance().signOut();
                                    }
                                    clearFields();
                                }
                            });

                        }
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Unable to Register", Toast.LENGTH_SHORT).show();
                        }
                        showSignUpBtn();
                        hideDialog();
                    }
                });
    }

    private void clearFields() {
        firstName.setText("");
        lastName.setText("");
        email.setText("");
        password.setText("");
        confirmPassword.setText("");
    }

    private void hideSignUpBtn() {
        signUpButton.setVisibility(View.INVISIBLE);
    }
    private void showSignUpBtn() {
        signUpButton.setVisibility(View.VISIBLE);
    }
}