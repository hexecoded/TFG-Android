package com.skincancer.skincancerapp.ui.diagnostico;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class DiagnosticoFragment extends Fragment {
    private FragmentDiagnosticoBinding binding;
    private MaterialButton fromGallery;
    private MaterialButton fromCamera;

    private static final String[] CLASSES = new String[]{"Benigno", "Maligno"};
    private static final String[] CLASSESBENIGN = new String[]{"Acrocordón", "Queratosis actínica", "Proliferación melatocínica atípica", "Angioma", "AIMP", "Dermatofibroma", "Lentigo", "Lentigo", "Querastosis liquenoide", "Cicatriz", "Lunar común", "Queratosis benigna", "Neurofibroma", "Queratosis seborreica", "Lentigo solar", "Lesión vascular", "Verruga"};
    private static final String[] CLASSESMALIGNANT = new String[]{"Carcinoma de célula basal", "Melanoma", "Carcinoma de célula escamosa"};

    private static String RESULTS_FILE = "historial.txt";

    // Pytorch model
    Module module;
    Module moduleBenign;
    Module moduleMalignant;

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

        if (result.isSuccessful()) {
            Bitmap bitmap = BitmapFactory.decodeFile(result.getUriFilePath(requireContext(), true));
            final float[] scores = predict(bitmap, true, module);
            final float[] scoresBenign;
            final float[] scoresMalignant;
            int maxScoreIdx = argMax(scores);
            System.out.println("maxScoreIdx: " + maxScoreIdx);
            System.out.println("Predicted class: " + CLASSES[maxScoreIdx]);

            // predecimos subtipo

            Toast.makeText(getContext(), "Cargando...", Toast.LENGTH_LONG).show();

            boolean ismalignant = false;
            int maxIdx = 0;
            if (maxScoreIdx == 0) {
                scoresBenign = predict(bitmap, true, moduleBenign);
                maxIdx = argMax(scoresBenign);
                System.out.println("maxScoreIdx: " + maxIdx);
                System.out.println("Predicted class: " + CLASSESBENIGN[maxIdx]);

            } else {
                scoresMalignant = predict(bitmap, true, moduleMalignant);
                maxIdx = argMax(scoresMalignant);
                System.out.println("maxScoreIdx: " + maxIdx);
                System.out.println("Predicted class: " + CLASSESMALIGNANT[maxIdx]);
                ismalignant = true;
            }

            //Log.d(String.valueOf(this.getClass()), String.format("Prediction: %s (%.2f %% beningno | %.2f %% maligno)", CLASSES[maxScoreIdx], scores[0] * 100, scores[1] * 100));

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

                File file = new File(getContext().getFilesDir(), RESULTS_FILE);
                if (!file.exists()) file.mkdir();

                File gpxfile = new File(file, "historial_paciente");
                FileWriter writer = new FileWriter(gpxfile, true);
                if (ismalignant){
                    writer.append(String.format("%s:::%s:::%s\n", uri, maxScoreIdx,CLASSESMALIGNANT[maxIdx]));
                }
                else{
                    writer.append(String.format("%s:::%s:::%s\n", uri, maxScoreIdx,CLASSESBENIGN[maxIdx]));
                }
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
            intent.putExtra("ismalignant", ismalignant);
            intent.putExtra("maxIDX", maxIdx);


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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDiagnosticoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fromGallery = root.findViewById(R.id.gallerybutton);
        fromCamera = root.findViewById(R.id.camera);

        fromGallery.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_ADD_FILE);
        });

        fromCamera.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        });


        // Cargamos el modelo de pytorch
        try {
            Toast.makeText(getContext(), "Cargando modelos...", Toast.LENGTH_SHORT).show();

            // Loading the 3 models
            module = Module.load(assetFilePath(getContext(), "skin-rn50android512.ptl"));
            moduleBenign = Module.load(assetFilePath(getContext(), "bestISICbenign512_android.ptl"));
            moduleMalignant = Module.load(assetFilePath(getContext(), "bestISICmalignant512_android.ptl"));

            // Code below is for checking that load + tensor convert to float + normalization is the same here and in Android
            Bitmap assetImage = BitmapFactory.decodeFile(assetFilePath(getContext(), "ASAN_0.png"));
            final Tensor tensor = TensorImageUtils.bitmapToFloat32Tensor(assetImage, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);

            if (RUN_VALIDATION) {
                String[] validation_fns = getContext().getAssets().list("validation/");
                System.out.println("Running validation set from: " + validation_fns.length);
                long startTime = System.currentTimeMillis();

                try {

                    File file = new File(getContext().getFilesDir(), "validacion.txt");
                    if (!file.exists()) file.mkdir();

                    File gpxfile = new File(file, "resultadosvalid");
                    FileWriter writer = new FileWriter(gpxfile, true);


                    for (int i = 0; i < 200; ++i) {
                        String assetFn = "validation/" + validation_fns[i];
                        InputStream istr = getContext().getAssets().open(assetFn);
                        Bitmap bitmap = BitmapFactory.decodeStream(istr);
                        istr.close();
                        if (bitmap != null) {
                            float[] scores = predict(bitmap, false, module);
                            int predictedClass = argMax(scores);

                            //System.out.println(validation_fns[i] + ", " + CLASSES[predictedClass]);
                            //System.out.println(CLASSES[predictedClass]);
                            writer.append(validation_fns[i] + ", " + predictedClass + "\n");

                        }
                    }

                    writer.flush();
                    writer.close();


                    long difference = System.currentTimeMillis() - startTime;
                    System.out.print("Tiempo de inferencia: ");
                    System.out.print(difference);
                    System.out.println("ms");

                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

            }
        } catch (IOException e) {
            Log.e("SKINCancerAPP", "Error reading assets", e);
        }

        Toast.makeText(getContext(), "Modelos cargados exitosamente", Toast.LENGTH_SHORT).show();

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
            cursor.close();

            cropImage(selectedImage);
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
            cropImage(uri);
        }
    }


    // Here we do all the pytorch stuff (except with loading the model, done in onCreate) and return the predicted class probabilities
    private float[] predict(Bitmap bitmap, boolean verbose, Module model) {
        // Convert bitmap to Float32 tensor is equivalent to:
        // - Load the image (pixels as 0 to 255 bytes).
        // - Apply torchvision.transforms.ToTensor, scaleing values from 0 to 1 (dividing by 255).
        // - Apply transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))
        // You don't need the resize because ResNet use AdaptiveAvgPool2d
        bitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, false);
        //Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
        //        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        //final Tensor inputTensor = bitmap.toTensor();
        if (verbose) System.out.println("Shape: " + Arrays.toString(inputTensor.shape()));

        // Forward pass, run the model
        // We do not resize to 224 x 224 thanks to AdaptiveAvgPool2d (but it could be a good idea to speed up inference process)
        // In production this SHOULD NOT be done in the main thread because is a lot of work and will block the app
        if (verbose) System.out.println("Forward begin");
        Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();
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