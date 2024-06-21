package com.skincancer.skincancerapp.ui.diagnostico;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skincancer.skincancerapp.MainActivity;
import com.skincancer.skincancerapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PrognosisFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrognosisFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String prognosis;
    private String score;

    private TextView prognosisLabel;
    private TextView diagDescription;

    public PrognosisFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PrognosisFragment.
     */
    public static PrognosisFragment newInstance(String param1, String param2) {
        PrognosisFragment fragment = new PrognosisFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            prognosis = getArguments().getString(ARG_PARAM1);
            score = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.prognosis, container, false);

        // Ajustamos los valores de la enfermedad y su probabilidad
        prognosisLabel = view.findViewById(R.id.progLabel);
        prognosisLabel.setText(prognosis + " (" + score + "%)");

        diagDescription = view.findViewById(R.id.descPatologia);
        try {
            diagDescription.setText(getDescripcionPatologia(prognosis));
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    private static Fragment newInstance(float[] scores) {
        PrognosisFragment fragment = new PrognosisFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private String getDescripcionPatologia(String enfermedad) throws IOException, JSONException {

        // Cargamos archivo de patologías
        InputStream inputStream = getContext().getAssets().open("infopatologia.json");


        // Leemos su contenido
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        // Convertimos a JSON
        JSONObject dictEnfermedades = new JSONObject(builder.toString());

        return dictEnfermedades.get(enfermedad).toString();
    }
}

