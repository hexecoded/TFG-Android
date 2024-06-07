package com.skincancer.skincancerapp.ui.diagnostico;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.button.MaterialButton;
import com.skincancer.skincancerapp.R;
import com.skincancer.skincancerapp.databinding.FragmentDiagnosticoBinding;

import org.pytorch.IValue;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DiagnosticoFragment extends Fragment {
    private FragmentDiagnosticoBinding binding;
    private MaterialButton fromGallery;
    private MaterialButton fromCamera;

    private static String RESULTS_FILE = "historial.txt";

    // Pytorch model
    Module module;

    final String[] GALLERY_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CAMERA_REQUEST = 1888;
    // Flag for enable/disable running validation when launching the app
    final boolean RUN_VALIDATION = false;
    // Validation percentage of image used (for >10 the process is very time consuming)
    final int RUN_VALIDATION_PCT = 1;

    // Internal request codes that are lunched by the app
    // Request for storage permissions
    final int REQUEST_GALLERY_PERMISSIONS = 1;
    // Request for image on gallery selection (external content)
    final int REQUEST_ADD_FILE = 2;

    ActivityResultLauncher<CropImageContractOptions> cropImageAR = registerForActivityResult(new CropImageContract(), result -> {
        System.out.println("dentro");

        if (result.isSuccessful()) {
            Bitmap bitmap = BitmapFactory.decodeFile(result.getUriFilePath(requireContext(), true));
            final float[] scores = predictBinary(bitmap, true);
            int maxScoreIdx = argMax(scores);
            System.out.println("maxScoreIdx: " + maxScoreIdx);

            String[] CLASSES = new String[]{"benigno", "maligno"};
            System.out.println("Predicted class: " + CLASSES[maxScoreIdx]);

            Log.d(String.valueOf(this.getClass()), String.format("Prediction: %s (%.2f %% beningno | %.2f %% maligno)", CLASSES[maxScoreIdx], scores[0] * 100, scores[1] * 100));

            // Guardamos la imagen

            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
            Date date = new Date();
            String filename = "img_" + sdf.format(date);
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                FileOutputStream fo = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                fo.write(bytes.toByteArray());
                // remember close file output
                fo.close();
            } catch (Exception e) {
                e.printStackTrace();
                filename = null;
            }

            // La guardamos haciendo referencia


            Bitmap x = bitmap;
            ContentValues values = new ContentValues();

            filename = "img_" + sdf.format(date);

            values.put(MediaStore.Images.Media.TITLE, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream outstream;
            try {
                outstream = getActivity().getContentResolver().openOutputStream(uri);
                x.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                outstream.close();

                // Guardamos URI en fichero
                //                FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), "tours"));
                //                FileOutputStream historial = new FileOutputStream(RESULTS_FILE, true);
                //                OutputStreamWriter historialWriter = new OutputStreamWriter(historial);
                //
                //                historialWriter.append(uri.toString() + ";" + maxScoreIdx);
                //
                //                historialWriter.close();
                //                historial.close();


                File file = new File(getContext().getFilesDir(), RESULTS_FILE);
                if (!file.exists())
                    file.mkdir();

                File gpxfile = new File(file, "historial_paciente");
                FileWriter writer = new FileWriter(gpxfile, true);
                writer.append(String.format("%s:::%s\n", uri, maxScoreIdx));
                writer.flush();
                writer.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Mostramos los resultados


            Intent intent = new Intent(getActivity(), GalleryActivity.class);
            intent.putExtra("scores", scores);
            intent.putExtra("maxScoreIdx", maxScoreIdx);
            intent.putExtra("picturePath", filename);
            intent.putExtra("fromcamera", true);

            getActivity().startActivity(intent);

        }
    });

    private void cropImage(Uri imageUri) {
        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.imageSourceIncludeGallery = true;
        cropImageOptions.guidelines = CropImageView.Guidelines.ON;
        cropImageOptions.imageSourceIncludeCamera = true;
        cropImageOptions.fixAspectRatio = true;
        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(imageUri, cropImageOptions);
        cropImageAR.launch(cropImageContractOptions);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        binding = FragmentDiagnosticoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fromGallery = root.findViewById(R.id.gallerybutton);
        fromCamera = root.findViewById(R.id.camera);

        fromGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_ADD_FILE);
            }
        });

        fromCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        //mText.setText("Diagnostico");

        // Here we load the assets: the only one is the pytorch model.
        try {
            module = Module.load(assetFilePath(getContext(), "skin-rn50android512.ptl"));

            // Code below is for checking that load + tensor convert to float + normalization is the same here and in Android
            Bitmap assetImage = BitmapFactory.decodeFile(assetFilePath(getContext(), "ASAN_0.png"));
            final Tensor tensor = TensorImageUtils.bitmapToFloat32Tensor(assetImage, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
            //System.out.println("inputTensor[100,200,0] = " + tensor.getDataAsFloatArray()[0 + 100 * 3 + 200 * 3 * 720]);
            //System.out.println("inputTensor[44,123,2] = " + tensor.getDataAsFloatArray()[2 + 44 * 3 + 123 * 3 * 720]);

            if (RUN_VALIDATION) {
                String[] validation_fns = getContext().getAssets().list("validation/");
                System.out.println("Running validation set from: " + validation_fns.length);
                int processedImages = 0, processedCatImages = 0, processedDogImages = 0, isCorrectPrediction = 0;
                for (int i = 0; i < validation_fns.length; ++i)
                    if (i % 100 < RUN_VALIDATION_PCT) {
                       /* String assetFn = "validation/" + validation_fns[i];
                        InputStream istr = getContext().getAssets().open(assetFn);
                        Bitmap bitmap = BitmapFactory.decodeStream(istr);
                        istr.close();
                        if (bitmap != null) {
                            float[] scores = predictBinary(bitmap, false);
                            int predictedClass = argMax(scores);

                            //if (i%100==0)
                            System.out.println(validation_fns[i]);
                            System.out.println(Character.isUpperCase(validation_fns[i].charAt(0)) ? "cat" : "dog");
                            int realClass = Character.isUpperCase(validation_fns[i].charAt(0)) ? 0 : 1;
                            System.out.println(predictedClass + " " + realClass);
                            if (++processedImages % 100 == 0) System.out.println(processedImages);
                            if (realClass == 0) ++processedDogImages;
                            else ++processedCatImages;
                            isCorrectPrediction += (predictedClass == realClass) ? 1 : 0;
                        }*/
                    }

                //System.out.println(String.format("Cats: %d (%.2f) Dogs: %d (%.2f) Acc: %.8f", processedCatImages, ((float) processedCatImages) / processedImages, processedDogImages, ((float) processedDogImages) / processedImages, ((float) isCorrectPrediction) / processedImages));
            }

        } catch (IOException e) {
            Log.e("SKINCancerAPP", "Error reading assets", e);
        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_FILE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            //ImageView imageView = (ImageView) findViewById(R.id.imageView);
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            //imageView.setImageBitmap(bitmap);

            cropImage(selectedImage);

            final float[] scores = predictBinary(bitmap, true);
            int maxScoreIdx = argMax(scores);
            System.out.println("maxScoreIdx: " + maxScoreIdx);

            String[] CLASSES = new String[]{"benigno", "maligno"};
            System.out.println("Predicted class: " + CLASSES[maxScoreIdx]);

            Log.d(String.valueOf(this.getClass()), String.format("Prediction: %s (%.2f %% beningno | %.2f %% maligno)", CLASSES[maxScoreIdx], scores[0] * 100, scores[1] * 100));
        }


        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && null != data) {
            Bitmap x = (Bitmap) data.getExtras().get("data");
            ContentValues values = new ContentValues();

            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
            Date date = new Date();
            String filename = "img_" + sdf.format(date);

            values.put(MediaStore.Images.Media.TITLE, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream outstream;
            try {
                outstream = getActivity().getContentResolver().openOutputStream(uri);
                x.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                outstream.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
            System.out.print("URI:");
            System.out.println(uri.toString());

            cropImage(uri);
        }
    }


    // Here we do all the pytorch stuff (except with loading the model, done in onCreate) and return the predicted class probabilities
    private float[] predictBinary(Bitmap bitmap, boolean verbose) {
        // Convert bitmap to Float32 tensor is equivalent to:
        // - Load the image (pixels as 0 to 255 bytes).
        // - Apply torchvision.transforms.ToTensor, scaleing values from 0 to 1 (dividing by 255).
        // - Apply transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))
        // You don't need the resize because ResNet use AdaptiveAvgPool2d
        bitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, false);
        //Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
        //        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
        //final Tensor inputTensor = bitmap.toTensor();
        if (verbose) System.out.println("Shape: " + Arrays.toString(inputTensor.shape()));

        // Forward pass, run the model
        // We do not resize to 224 x 224 thanks to AdaptiveAvgPool2d (but it could be a good idea to speed up inference process)
        // In production this SHOULD NOT be done in the main thread because is a lot of work and will block the app
        if (verbose) System.out.println("Forward begin");
        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        if (verbose) System.out.println("Forward ends");

        // Getting tensor content as java array of floats
        final float[] scores = outputTensor.getDataAsFloatArray();
        if (verbose) System.out.println("scores: " + Arrays.toString(scores));
        return scores;
    }

    private int argMax(float[] scores) {
        // Searching for the index with maximum score
        int maxScoreIdx = 0;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > scores[maxScoreIdx]) {
                maxScoreIdx = i;
            }
        }
        return maxScoreIdx;
    }

    // This function was copied from HelloWorldApp (android-demo-app from pytorch's repo)
    // It returns the path of the asset on the file dir
    // The first time is called for an asset it copies the asset from the asset location to file dir
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

}