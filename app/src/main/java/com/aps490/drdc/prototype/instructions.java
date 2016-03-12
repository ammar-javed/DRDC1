package com.aps490.drdc.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class instructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

   /* public void getNextInstruction(View view) {

        Assembly assembly;
        Instruction instruction;

        try {
            assembly = new Assembly(getAssets().open("mainLandingGearWheel.xml"));
            instruction = assembly.getInstr();
            ((TextView) findViewById(R.id.textViewInst)).setText(instruction.getText());
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
   /* Intent intent = getIntent();
    int position = intent.getIntExtra("position", 0);
    int task = intent.getIntExtra("task", 0);
    Assembly assembly;
    Instruction instruction;

    public void getNextInstruction(View view) {


        try {
            assembly = new Assembly(getAssets().open("mainLandingGearWheel.xml"));
            instruction = assembly.getInstr();
            ((TextView) findViewById(R.id.textViewInst)).setText(instruction.getText());
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }*/
}
