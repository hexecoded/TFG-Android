package com.skincancer.skincancerapp.ui.diagnostico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.skincancer.skincancerapp.R;
import com.skincancer.skincancerapp.databinding.FragmentDiagnosticoBinding;

public class DiagnosticoFragment extends Fragment {
    private FragmentDiagnosticoBinding binding;
    private TextView mText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentDiagnosticoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mText = root.findViewById(R.id.text_gallery);
        mText.setText("Diagnostico");

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}