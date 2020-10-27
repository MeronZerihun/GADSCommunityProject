package com.example.e_treat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.e_treat.model.Doctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class DoctorInfoActivity extends AppCompatActivity {

    private static final int UPLOAD_CV = 1;
    private static final int UPLOAD_DOCS = 2;
    EditText department, qualification, specialization, education, cV, nationalId, academicDocs;
    Button createAccountBtn;
    ProgressBar progressBar;

    final HashMap<String, String> urls = new HashMap<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_info);
        
        
        department = findViewById(R.id.dept_text);
        qualification = findViewById(R.id.qualification_text);
        specialization = findViewById(R.id.specialization_text);
        education = findViewById(R.id.education_text);
        cV = findViewById(R.id.cv_file);
        nationalId = findViewById(R.id.national_id_text);
        academicDocs = findViewById(R.id.academic_file);
        
        createAccountBtn = findViewById(R.id.create_account_button);
        
        progressBar = findViewById(R.id.account_progress);


        cV.setOnTouchListener((view, motionEvent) -> {
                openFileChooser("Upload CV (Format in PDF)", UPLOAD_CV);
                return true;
        });

        academicDocs.setOnTouchListener((view, motionEvent) -> {
            openFileChooser("Upload Academic Document (Format in PDF)", UPLOAD_DOCS);
            return true;
        });



        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !isEmpty(department.getText().toString())  && !isEmpty(qualification.getText().toString())
                        && !isEmpty(specialization.getText().toString())
                        && !isEmpty(cV.getText().toString())
                        && !isEmpty(education.getText().toString())
                        && !isEmpty(nationalId.getText().toString())
                        && !isEmpty(academicDocs.getText().toString())
                ){
                    showDialog();
                    createDoctorAccount();

                }
                else{
                    Toast.makeText(DoctorInfoActivity.this, "Please make sure that you have filled all the fields correctly!", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

    private void showDialog() {
        progressBar.setVisibility(View.VISIBLE);
        createAccountBtn.setVisibility(View.INVISIBLE);
    }
    private void hideDialog() {
        progressBar.setVisibility(View.INVISIBLE);
        createAccountBtn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == UPLOAD_CV && resultCode == RESULT_OK && data.getData() != null){
            uploadPDF("CV", data.getData());
        }
        else if(requestCode == UPLOAD_DOCS && resultCode == RESULT_OK && data.getData() != null){
            uploadPDF("DOCS", data.getData());
        }
    }

    private void uploadPDF(String type, Uri data) {

        final ProgressDialog dialog =  new ProgressDialog(this);
        dialog.setTitle("File Uploading");
        dialog.show();

        String userId = FirebaseAuth.getInstance().getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference("uploads");
        reference.child(type+"_"+userId+"_"+System.currentTimeMillis()+".pdf")
                .putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                        while(!task.isComplete());
                        Uri uri = task.getResult();

                        urls.put(type, uri.toString());
                        dialog.dismiss();
                        if(type.equals("CV")){
                            cV.setText(type + " document has been uploaded");
                        }
                        else if(type.equals("DOCS")){
                            academicDocs.setText(type + " document has been uploaded");
                        }


                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                double progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                dialog.setMessage("File uploading..."+(int) progress+"%");
            }
        });


    }


    private void openFileChooser(String message, int requestCode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent = Intent.createChooser(intent, message);
        startActivityForResult(intent, requestCode);


    }

    private void createDoctorAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getUid() == null){
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Doctor doctor = new Doctor(department.getText().toString(), qualification.getText().toString(), specialization.getText().toString(), education.getText().toString(), urls.get("CV"), nationalId.getText().toString(), urls.get("DOCS"));
            FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.doctor_node))
                    .child(FirebaseAuth.getInstance().getUid())
                    .setValue(doctor)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(DoctorInfoActivity.this, "Account was successfully created!", Toast.LENGTH_SHORT).show();
                            hideDialog();
                            clearFields();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DoctorInfoActivity.this, "Something went wrong. Try again!", Toast.LENGTH_SHORT).show();
                    hideDialog();
                }
            });
        }
    }

    private boolean isEmpty(String string){
        return string.equals("");
    }

    private void clearFields(){
        department.setText("");
        qualification.setText("");
        specialization.setText("");
        cV.setText("");
        education.setText("");
        nationalId.setText("");
        academicDocs.setText("");
    }
}