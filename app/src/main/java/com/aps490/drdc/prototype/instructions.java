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
    Assembly assembly;
    Instruction currentInstr;int task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        System.out.println("Creating instructions activity.");
        String assemblyName = intent.getStringExtra("name");
        task = intent.getIntExtra("task", 0);
        System.out.println("Acquired intent values.");

        setContentView(R.layout.activity_instructions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            System.out.println("Attempting to load assembly " + assemblyName);
            Assembly assembly = new Assembly( getAssets().open(CourseModules.map.get(assemblyName) ) );
            System.out.println("Initialized assembly: " + assemblyName);
            assembly.selectModule(task);
            System.out.println("Module selected: " + assembly.getModules().get(task));
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public void getPreInstruction(View view) {
        Instruction instruction = assembly.getPreviousInstr();
        if(instruction!=null)
            currentInstr = instruction;

        ((TextView) findViewById(R.id.textViewInst)).setText(currentInstr.getText());

    }



    public void getNextInstruction(View view) {
        Instruction instruction = assembly.getInstr();
        if(instruction!=null)
            currentInstr = instruction;

        ((TextView) findViewById(R.id.textViewInst)).setText(currentInstr.getText());

    }
}
