package com.skincancer.skincancerapp.ui.diagnostico;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.skincancer.skincancerapp.R;
import com.skincancer.skincancerapp.databinding.FragmentDiagnosticoBinding;

public class DiagnosticoFragment extends Fragment {
    private FragmentDiagnosticoBinding binding;
    private MaterialButton fromGallery;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentDiagnosticoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fromGallery = root.findViewById(R.id.gallerybutton);
        fromGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(), GalleryActivity.class);
                getActivity().startActivity(intent);

            }
        });
        //mText.setText("Diagnostico");

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}