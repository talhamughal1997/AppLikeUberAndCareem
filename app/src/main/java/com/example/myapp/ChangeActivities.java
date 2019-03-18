package com.example.myapp;

import android.content.Context;
import android.content.Intent;

public class ChangeActivities {

    public void ChangeActivity(Context context, Class activityClass) {
        Intent intent = new Intent(context, activityClass);
        context.startActivity(intent);

    }

}
