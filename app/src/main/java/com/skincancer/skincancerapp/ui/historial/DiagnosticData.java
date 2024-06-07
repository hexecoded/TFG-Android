package com.skincancer.skincancerapp.ui.historial;

public class DiagnosticData {
    public String result;
    public String type;
    public String imageDir;

    public DiagnosticData(String result, String type, String image) {
        this.result = result;
        this.type = type;
        this.imageDir = image;
    }
}
