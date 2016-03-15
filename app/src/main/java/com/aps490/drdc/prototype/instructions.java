package com.aps490.drdc.prototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Dialog;
import java.util.ArrayList;

import java.io.IOException;

public class instructions extends AppCompatActivity {
    Assembly assembly;
    Instruction currentInstr;int task;
    private ImageView mDialog;
    ArrayList<String> figures;


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

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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

            figures = assembly.getFigures();

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
        if( figures.isEmpty() ) {
            System.out.println("No figures for this module");
            //Add a popup saying no figure available for module
        }
        else {
            System.out.println("Figure name for this app is: " + figures.get(0));
            String figureName;
            if( currentInstr.hasFigure() )
              figureName = currentInstr.getFigure();
            else
              figureName = figures.get(0);

            showImage(figureName);
            //To load the image, use ...something...( getAssets().open(figures[0])   );
        }
    }

    public void showImage(String fileName ) {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);
        try {
            imageView.setImageBitmap(BitmapFactory.decodeStream(getAssets().open(fileName)));
        }catch(IOException e) {
            e.printStackTrace();
        }

        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, task.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }

}
