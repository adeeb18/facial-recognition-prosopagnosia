package com.example.photorecogtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Element;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.photorecogtest.ml.MobilenetModelMetadata3;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class Record extends Activity
{
    private static final int CAMERA_REQUEST = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private ImageView imageView;
    private TextToSpeech t1;
    private Button speakButton;
    private Button photoButton;
    private Bitmap Img;
    private Bitmap resizedImg;
    private Button callModelButton;

    private int currentClass;
    private Map<Integer, String> classIdentifier = new HashMap<Integer, String>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        this.imageView = (ImageView)this.findViewById(R.id.photoView);
        photoButton = (Button) this.findViewById(R.id.takePhoto);
        speakButton = (Button)findViewById(R.id.speakButton);
        callModelButton = (Button)findViewById(R.id.callModel);

        classIdentifier.put(0, "Jeffrey Chen");
        classIdentifier.put(1, "Patrick Kennedy");
        classIdentifier.put(-1, "Unknown");

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int result = t1.setLanguage(Locale.US);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    }
                    else{
                        Log.e("TTS", "Language is supported");
                    }
                }
                else{
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        speakButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                speak();
            }
        });

        callModelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                callModel();
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra("android.intent.extra.quickCapture", true);
        //Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        if (cameraIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }


    }

    private void speak(){
        String text = classIdentifier.get(currentClass);
        float pitch = (float)1.0;
        float speed = (float)1.0;
        t1.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        startActivity(new Intent(Record.this, MainActivity.class));
    }

    @Override
    protected void onDestroy(){
        if(t1 != null){
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //requestCode
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            //grantResults
            //grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (true)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != RESULT_CANCELED) {

            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);
                Img = photo;
                resizedImg = Bitmap.createScaledBitmap(photo, 640, 640, true);
                Log.d("model call", "calling model");
                callModel();
            }
        }
    }
    private void callModel(){
        try {
            MobilenetModelMetadata3 model = MobilenetModelMetadata3.newInstance(this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(Img);

            // Runs model inference and gets result.
            MobilenetModelMetadata3.Outputs outputs = model.process(image);
            MobilenetModelMetadata3.DetectionResult detectionResult = outputs.getDetectionResultList().get(0);

            // Gets result from DetectionResult.
            float accuracy = detectionResult.getScoreAsFloat();
            RectF category = detectionResult.getLocationAsRectF();
            String score = detectionResult.getCategoryAsString();

            // Releases model resources if no longer used.
            model.close();
            Log.d("MODEL OUTPUT", "accuracy: " + accuracy);
            Log.d("MODEL OUTPUT", "class: " + score);
            if(accuracy < 0.95){
                currentClass = -1;
            }
            else{
                if (score.indexOf('{') >= 0){
                    currentClass = 0;
                    Log.d("MODEL OUTPUT", "JEFF");
                }
                else{
                    currentClass = 1;
                    Log.d("MODEL OUTPUT", "PK");
                }
            }

            speakButton.performClick();
        } catch (IOException e) {
            // TODO Handle the exception
            Toast.makeText(this, "model IO Exception", Toast.LENGTH_LONG).show();
            Log.d("MODEL OUTPUT", "Model IO Exception");
        }

    }

}