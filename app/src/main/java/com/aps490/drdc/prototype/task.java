package com.aps490.drdc.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;

public class task extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView l ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String[] values;
        Intent intent = getIntent();
        String moduleName = intent.getStringExtra("name");
        System.out.println(moduleName);

        try{
            Assembly assembly = new Assembly( getAssets().open(CourseModules.map.get(moduleName) ) );
            values = assembly.getModules().toArray(new String[0]);
            System.out.println(values);
            l = (ListView) findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_view_layout,R.id.list_content,values);
            l.setAdapter(adapter);
            l.setOnItemClickListener(this);

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, instructions.class);
        intent.putExtra("name",intent.getStringExtra("name"));
        intent.putExtra("task",position);
        startActivity(intent);

    }
}
