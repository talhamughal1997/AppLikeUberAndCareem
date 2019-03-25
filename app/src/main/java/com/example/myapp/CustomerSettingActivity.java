package com.example.myapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CustomerSettingActivity extends AppCompatActivity implements View.OnClickListener {

    EditText txtName, txtPhone;
    Button btnConfirm, btnBack;

    FirebaseAuth mFirebaseAuth;
    DatabaseReference mCustomerDatabase;

    String userId, mName, mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_setting);
        ViewsInit();
        ViewsListeners();

        mFirebaseAuth = FirebaseAuth.getInstance();
        userId = mFirebaseAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
        userInfo();
    }

    private void ViewsListeners() {
        btnConfirm.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void ViewsInit() {
        txtName = findViewById(R.id.edttxt_Name);
        txtPhone = findViewById(R.id.edttxt_Phone);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfirm: {
                saveUserInformation();
                break;
            }
            case R.id.btnBack: {
                finish();
                break;
            }
        }
    }

    private void userInfo() {
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        txtName.setText(mName);
                    }
                    if (map.get("phone") != null) {
                        mPhone = map.get("phone").toString();
                        txtPhone.setText(mPhone);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {
        mName = txtName.getText().toString();
        mPhone = txtPhone.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name",mName);
        userInfo.put("phone",mPhone);
        mCustomerDatabase.updateChildren(userInfo);

        finish();
    }
}
