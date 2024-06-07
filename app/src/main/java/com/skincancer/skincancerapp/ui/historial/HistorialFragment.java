package com.skincancer.skincancerapp.ui.historial;

// Fuente parcial: https://www.tutorialspoint.com/how-to-display-a-list-of-images-and-text-in-a-listview-in-android

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.skincancer.skincancerapp.R;
import com.skincancer.skincancerapp.databinding.FragmentHistorialBinding;

import java.util.ArrayList;

public class HistorialFragment extends Fragment {

    private FragmentHistorialBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentHistorialBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final ListView list = root.findViewById(R.id.list);
        ArrayList<DiagnosticData> arrayList = new ArrayList<DiagnosticData>();
        arrayList.add(new DiagnosticData("Benigno", "Queratosis seborreica", "https://clinicamerced.cl/wp-content/uploads/2021/09/Queratosis-Seborreica.jpg"));
        arrayList.add(new DiagnosticData("Benigno", "Cicatriz", "https://consejos.iml.es/wp-content/uploads/2020/06/cicatriz-queloide.jpeg"));
        arrayList.add(new DiagnosticData("Maligno", "Cáncer de célula basal", "https://img.medscapestatic.com/pi/meds/ckb/26/38226tn.jpg"));
        CustomAdapter customAdapter = new CustomAdapter(getContext(), arrayList);
        list.setAdapter(customAdapter);

        return root;
    }
}