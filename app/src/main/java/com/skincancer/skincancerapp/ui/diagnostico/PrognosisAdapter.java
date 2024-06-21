package com.skincancer.skincancerapp.ui.diagnostico;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class PrognosisAdapter extends FragmentStateAdapter {
    private boolean isMalignant;
    private int[] bestScoresPos;
    private float[] bestScores;

    private static int ITEMCOUNT = 3;
    private static final String[] CLASSESBENIGN = new String[]{"Acrocordón", "Queratosis actínica", "Proliferación melatocínica atípica", "Angioma", "AIMP", "Dermatofibroma", "Lentigo", "Lentigo", "Querastosis liquenoide", "Cicatriz", "Lunar común", "Queratosis benigna", "Neurofibroma", "Queratosis seborreica", "Lentigo solar", "Lesión vascular", "Verruga"};
    private static final String[] CLASSESMALIGNANT = new String[]{"Carcinoma de célula basal", "Melanoma", "Carcinoma escamoso"};

    public PrognosisAdapter(FragmentManager fragmentManager, Lifecycle lifecycle, boolean isMalignant, float[] scores) {
        super(fragmentManager, lifecycle);
        this.isMalignant = isMalignant;
        bestScoresPos = new int[3];
        bestScores = new float[3];

        getTopKValues(scores);
    }

    private void getTopKValues(float[] scores) {
        ArrayList<Float> tmpScores = new ArrayList<>();

        for (float valor : scores)
            tmpScores.add(valor);


        for (int i = 0; i < ITEMCOUNT; i++) {
            int maxPosition = tmpScores.indexOf(Collections.max(tmpScores));
            bestScoresPos[i] = maxPosition;
            bestScores[i] = tmpScores.get(maxPosition);
            tmpScores.set(maxPosition, 0.0F);
        }
    }

    @Override
    public int getItemCount() {
        return ITEMCOUNT;
    }

    @Override
    public Fragment createFragment(int position) {
        String diag;
        String prob;
        switch (position) {
            case 0:
                prob = String.format("%.2f", bestScores[0] * 100.0);

                // Tipo de enfermedad
                if (isMalignant) diag = CLASSESMALIGNANT[bestScoresPos[0]];
                else diag = CLASSESBENIGN[bestScoresPos[0]];

                return PrognosisFragment.newInstance(diag, prob);
            case 1:
                prob = String.format("%.2f", bestScores[1] * 100.0);

                // Tipo de enfermedad
                if (isMalignant) diag = CLASSESMALIGNANT[bestScoresPos[1]];
                else diag = CLASSESBENIGN[bestScoresPos[1]];

                return PrognosisFragment.newInstance(diag, prob);

            case 2:
                prob = String.format("%.2f", bestScores[2] * 100.0);

                // Tipo de enfermedad
                if (isMalignant) diag = CLASSESMALIGNANT[bestScoresPos[2]];
                else diag = CLASSESBENIGN[bestScoresPos[2]];

                return PrognosisFragment.newInstance(diag, prob);

            default:
                return null;

        }

    }
}

