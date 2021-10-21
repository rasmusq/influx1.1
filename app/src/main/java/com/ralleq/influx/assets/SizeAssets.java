package com.ralleq.influx.assets;

public class SizeAssets {

    float strokeSizeInInches = 0.02f,
    strokeSizeThin = 0.01f,
    strokeSizeThick = 0.03f,

    minimumPianoKeyWidth = 0.3f, maximumPianoKeyWidth = 0.7f,

    maximumButtonSize = 0.5f;

    public static float dpi = 1;
    //A library of general sizes for things drawn on the screen

    public static void updateDPI(float newDPI) {

        dpi = newDPI;
    }
    public static float convertInchesToPixels(float inches) {
        return (inches * dpi);
    }
    public static float convertPixelsToInches(float pixels) {
        return (pixels / dpi);
    }
}
