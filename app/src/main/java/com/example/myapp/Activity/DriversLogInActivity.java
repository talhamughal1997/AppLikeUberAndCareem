package com.example.myapp.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp.ChangeActivities;
import com.example.myapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriversLogInActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editText_Email, editText_Password;
    Button btnLogIn;
    TextView txtSignUp;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener firebaseAuthListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_log_in);
        this.getSupportActionBar().hide();
        ViewsInitialization();
        Listeners();

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListeners = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    new ChangeActivities().ChangeActivity(DriversLogInActivity.this, DriverMapActivity.class);
                    finish();
                    return;
                }
            }
        };

    }

    void Listeners() {
        btnLogIn.setOnClickListener(this);
        txtSignUp.setOnClickListener(this);
    }

    void ViewsInitialization() {
        editText_Email = findViewById(R.id.email);
        editText_Password = findViewById(R.id.password);
        btnLogIn = findViewById(R.id.btnLogIn);
        txtSignUp = findViewById(R.id.txt_SignUp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogIn: {
                funLogIn();
                break;
            }
            case R.id.txt_SignUp: {
                funSignUp();
                break;
            }
        }
    }

    private void funLogIn() {
        final String email = editText_Email.getText().toString();
        final String pswd = editText_Email.getText().toString();

        mAuth.signInWithEmailAndPassword(email, pswd).addOnCompleteListener(DriversLogInActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(DriversLogInActivity.this, "Sign In Error ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void funSignUp() {
        final String email = editText_Email.getText().toString();
        final String pswd = editText_Email.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener(DriversLogInActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(DriversLogInActivity.this, "Sign Up Error ", Toast.LENGTH_SHORT).show();

                } else {
                    Log.d("success", "onComplete: ");
                    String user_id = mAuth.getCurrentUser().getUid();
                    DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id).child("name");
                    users.setValue(email);
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListeners);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListeners);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*mAuth.signOut();*/
    }
}
