package com.example.mysaved;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterUserActivity extends AppCompatActivity {
    private static final String TAG = "RegisterUserActivity";

    private EditText regName, regEmail, regPhone, regPassword;
    private Spinner districtSpinner, genderSpinner;
    private Button accountCreate;

    private FirebaseAuth fAuth;
    private DatabaseReference usersRef;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z0-9]+\\.+[a-z]+";
    private String phonePattern = "[0-9]{10}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_user);

        regName = findViewById(R.id.et_name_reg);
        regEmail = findViewById(R.id.et_email_reg2);
        regPhone = findViewById(R.id.et_mobile);
        regPassword = findViewById(R.id.et_password);
        districtSpinner = findViewById(R.id.district_spinner);
        genderSpinner = findViewById(R.id.gender_spinner);
        accountCreate = findViewById(R.id.btn_acCeate);

        fAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("user");

        accountCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = regName.getText().toString().trim();
        String email = regEmail.getText().toString().trim();
        String phone = regPhone.getText().toString().trim();
        String password = regPassword.getText().toString().trim();
        String district = districtSpinner.getSelectedItem().toString();
        String gender = genderSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(email)) {
            regEmail.setError("Email is required");
            return;
        } else if (!email.matches(emailPattern)) {
            regEmail.setError("Invalid Email Address");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            regPassword.setError("Password is required");
            return;
        } else if (password.length() < 6) {
            regPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (TextUtils.isEmpty(name)) {
            regName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            regPhone.setError("Phone number is required");
            return;
        } else if (!phone.matches(phonePattern)) {
            regPhone.setError("Invalid Phone Number");
            return;
        }

        if (district.equals("Select District")) {
            Toast.makeText(RegisterUserActivity.this, "Select a District", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender.equals("Select Gender")) {
            Toast.makeText(RegisterUserActivity.this, "Select Gender", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = fAuth.getCurrentUser();
                            if (currentUser != null) {
                                String userId = currentUser.getUid();

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("Email", email);
                                userMap.put("Name", name);
                                userMap.put("Phone", phone);
                                userMap.put("District", district);
                                userMap.put("Gender", gender);
                                userMap.put("JobId", 0);
                                userMap.put("ReqId", 0);

                                usersRef.child(userId).setValue(userMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RegisterUserActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "User profile is created for " + userId);

                                                startActivity(new Intent(getApplicationContext(), UserloginActivity.class));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegisterUserActivity.this, "Error: is" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(RegisterUserActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
