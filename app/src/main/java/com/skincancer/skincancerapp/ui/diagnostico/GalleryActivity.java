package com.skincancer.skincancerapp.ui.diagnostico;

import static com.skincancer.skincancerapp.ui.diagnostico.DiagnosticoFragment.assetFilePath;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import org.pytorch.MemoryFormat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skincancer.skincancerapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class GalleryActivity extends AppCompatActivity {

    // Button that allow to change image using gallery images
    FloatingActionButton galleryButton;

    // View for loading the image
    ImageView imageView;

    // View for writing the image classification text
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This already was here with the empty activity:
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Set toolbar

        Toolbar toolbar = findViewById(R.id.gallery_toolbar);
        toolbar.setTitle("Resultado del diagnóstico");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        float[] scores = getIntent().getFloatArrayExtra("scores");
        int maxScoreIdx = getIntent().getIntExtra("maxScoreIdx", 0); // 0 = default value
        String picturePath = getIntent().getStringExtra("picturePath");
        boolean fromcamera = getIntent().getBooleanExtra("fromcamera", false);
        Bitmap image;
        if (fromcamera) {
            try {
                image = BitmapFactory.decodeStream(openFileInput(picturePath));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else
            image = BitmapFactory.decodeFile(picturePath);

        // Associate elements from layout with their respective handler class
        imageView = findViewById(R.id.foto);
        textView = findViewById(R.id.dianostico);
        String res;
        // Show initial text with instructions
        if (maxScoreIdx == 0)
            textView.setText(String.format("Es una lesión benigna al %.2f %% de confianza", scores[0] * 100));
        else
            textView.setText(String.format("Es una lesión maligna al %.2f %% de confianza", scores[1] * 100));

        // Colocamos imagen
        imageView.setImageBitmap(image);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}