package com.example.photorecogtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Add People Page
        Button addPeopleButton = (Button)findViewById(R.id.addPeopleButton);

        addPeopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, NameInsertion.class));

            }
        });
    }


    //Override Keys
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            Toast.makeText(this, "Volume Up", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this, Record.class));
            return true;
        }
        return true;
    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event){
//        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
//            Toast.makeText(this, "Volume Up", Toast.LENGTH_LONG).show();
//            return true;
//        }
//        return true;
//    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}