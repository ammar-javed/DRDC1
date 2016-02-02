package com.aps490.drdc.prototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class ModuleSelection extends AppCompatActivity implements Communications {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_selection);


        Intent intent = getIntent();
        String message_username = intent.getStringExtra(LoginActivity.USER_NAME);
        String message_password = intent.getStringExtra(LoginActivity.PASSWORD);
    }


}
