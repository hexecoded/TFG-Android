package com.skincancer.skincancerapp.ui.historial;

// Fuente parcial: https://www.tutorialspoint.com/how-to-display-a-list-of-images-and-text-in-a-listview-in-android

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.skincancer.skincancerapp.R;
import com.skincancer.skincancerapp.databinding.FragmentHistorialBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HistorialFragment extends Fragment {

    private FragmentHistorialBinding binding;
    private static final String RESULTS_FILE = "historial.txt";
    private static final String[] CLASSES = new String[]{"Benigno", "Maligno"};


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = FragmentHistorialBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ArrayList<String> infoDiagnostics = new ArrayList<>();

//
//        StringBuilder content = new StringBuilder();
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(RESULTS_FILE));
//            String line;
//            while ((line = br.readLine()) != null) {
//                infoDiagnostics.add(line);
//            }
//            br.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        File file = new File(getContext().getFilesDir(), RESULTS_FILE);

        File gpxfile = new File(file, "historial_paciente");
        FileReader reader;
        BufferedReader buf_reader = null;
        try {
            reader = new FileReader(gpxfile);
            buf_reader = new BufferedReader(reader);

            String line = "";
            while (true) {
                try {
                    if ((line = buf_reader.readLine()) == null) break;
                } catch (IOException e) {
                    System.out.println("No hay fichero");
                }
                infoDiagnostics.add(line);
            }

        } catch (FileNotFoundException e) {
            System.out.println("No hay fichero");
        }


        //FileInputStream is;
        //BufferedReader reader;

//        if (file.exists()) {
//            try {
//                is = new FileInputStream(file);
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//            reader = new BufferedReader(new InputStreamReader(is));
//            String line = null;
//            try {
//                line = reader.readLine();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            while (line != null) {
//
//                try {
//                    line = reader.readLine();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                infoDiagnostics.add(line);
//
//            }
//        }

        final ListView list = root.findViewById(R.id.list);
        ArrayList<DiagnosticData> arrayList = new ArrayList<DiagnosticData>();

        for (int i = 0; i < infoDiagnostics.size(); i++) {
            ArrayList<String> params = new ArrayList<>();
            String[] fields = infoDiagnostics.get(i).split(":::");
            for (String a : fields) {
                params.add(a);
                System.out.println(a);

            }
            System.out.println("se acabo");


            arrayList.add(new DiagnosticData(CLASSES[Integer.parseInt(params.get(1))], "Queratosis seborreica", "https://cdn3.iconfinder.com/data/icons/design-n-code/100/272127c4-8d19-4bd3-bd22-2b75ce94ccb4-512.png"));
        }

        CustomAdapter customAdapter = new CustomAdapter(getContext(), arrayList);
        if (arrayList.isEmpty()) {
            arrayList.add(new DiagnosticData("", "Vacío", "https://cdn3.iconfinder.com/data/icons/design-n-code/100/272127c4-8d19-4bd3-bd22-2b75ce94ccb4-512.png"));

        }
        list.setAdapter(customAdapter);

        return root;
    }
}