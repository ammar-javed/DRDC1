package com.aps490.drdc.prototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class ModuleSelection extends AppCompatActivity implements Communications {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_selection);


        Intent intent = getIntent();
        String message_username = intent.getStringExtra(LoginActivity.USER_NAME);
        String message_password = intent.getStringExtra(LoginActivity.PASSWORD);
    }

    public void openMainPage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openSubAssemblyPage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, sub_assembly.class);
        startActivity(intent);
    }


}
