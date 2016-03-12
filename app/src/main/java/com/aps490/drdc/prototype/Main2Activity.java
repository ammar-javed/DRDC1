package com.aps490.drdc.prototype;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    public void showInstruction(View view) {
        // Do something in response to button
        Assembly assembly;
        Instruction instruction;


        try {
            assembly = new Assembly(getAssets().open("mainLandingGearWheel.xml"));
            instruction = assembly.getInstr();
            ((TextView) findViewById(R.id.textView8)).setText(instruction.getText());
        }
        catch(IOException e){
            e.printStackTrace();
        }



    }

}


