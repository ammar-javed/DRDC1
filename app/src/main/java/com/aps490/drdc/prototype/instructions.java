package com.aps490.drdc.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import java.io.IOException;

public class instructions extends AppCompatActivity {
    Assembly assembly;
    Instruction currentInstr;int task;
    private ImageView mDialog;


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
            assembly = new Assembly( getAssets().open(CourseModules.map.get(assemblyName) ) );
            System.out.println("Initialized assembly: " + assemblyName);
            assembly.selectModule(task);
            System.out.println("Module selected: " + assembly.getModules().get(task));

            Instruction instruction = assembly.getInstr();
            if(instruction==null)
                System.out.println("ERROR: First instruction was null!");

            currentInstr = instruction;

            ((TextView) findViewById(R.id.textViewInst)).setText(currentInstr.getText());
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
    public void seeFigures(View view) {
        ArrayList<String> figures = assembly.getFigures();
        System.out.println(figures.get(0));
        if( figures.isEmpty() ) {
            System.out.println("No figures for this module");
            //Add a popup saying no figure available for module
        }
        else {
            System.out.println("Figure name for this app is: " + figures.get(0));
            //To load the image, use ...something...( getAssets().open(figures[0])   );
        }
    }
}
