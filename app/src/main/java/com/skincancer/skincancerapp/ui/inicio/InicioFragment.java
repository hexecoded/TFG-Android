package com.skincancer.skincancerapp.ui.inicio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.skincancer.skincancerapp.R;
import com.skincancer.skincancerapp.databinding.FragmentInicioBinding;


public class InicioFragment extends Fragment {
    private FragmentInicioBinding binding;
    private TextView mText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mText = root.findViewById(R.id.text_home);
        mText.setText("Inicio");

        return root;
    }
}