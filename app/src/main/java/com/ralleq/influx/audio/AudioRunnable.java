package com.ralleq.influx.audio;

public interface AudioRunnable {
    public void run(int[] leftInputSamples, int[] rightInputSamples, int[] leftOutputSamples, int[] rightOutputSamples, int correntSampleIndex);
}
