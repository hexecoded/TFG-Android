package com.skincancer.skincancerapp.ui.diagnostico;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skincancer.skincancerapp.R;

import java.io.FileNotFoundException;


public class GalleryActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;


    // View for loading the image
    ImageView imageView;

    // View for writing the image classification text
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This already was here with the empty activity:
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        viewPager2 = findViewById(R.id.pager);

        // Set toolbar

        Toolbar toolbar = findViewById(R.id.gallery_toolbar);
        toolbar.setTitle("Resultado del diagnóstico");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        float[] scores = getIntent().getFloatArrayExtra("scores");
        int maxScoreIdx = getIntent().getIntExtra("maxScoreIdx", 0); // 0 = default value
        String picturePath = getIntent().getStringExtra("picturePath");
        boolean fromcamera = getIntent().getBooleanExtra("fromcamera", false);
        boolean ismalignant = getIntent().getBooleanExtra("ismalignant", false);
        float[] scoresSubtype = getIntent().getFloatArrayExtra("scoresSubtype");


        viewPager2.setAdapter(new PrognosisAdapter(getSupportFragmentManager(), getLifecycle(), ismalignant, scoresSubtype));

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
            textView.setText(String.format("Es una lesión benigna (%.2f %%)", scores[0] * 100));
        else
            textView.setText(String.format("Es una lesión maligna (%.2f %%)", scores[1] * 100));

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