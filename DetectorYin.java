package com.example.myapplication;
//https://github.com/aubio/aubio/blob/master/src/pitch/pitchyin.c
public class DetectorYin {
    public float threshold;
    public int sample_rate;
    public float[] buffer;
    public DetectorYin(int sample_rate, int data_size){
        this.threshold = 0.2f;
        this.sample_rate = sample_rate;
        this.buffer = new float[data_size/2];
    }
    public float process_audio(float[] buf){
        int buffer_size = buf.length/2;
        assert this.buffer.length == buffer_size;
        float[] yin = new float[buffer_size];
        for(int i=0; i<buffer_size; i++)
            yin[i] = this.buffer[i];
        float sum = 0;
        int peak_pos = -1;
        int min_pos = 0;
        yin[0] = 1.0f;
        for(int tau=1; tau<buffer_size; tau++){
            yin[tau] = 0;
            for (int j = 0; j < buffer_size; j++)
            {
                float diff = buf[j] - buf[j + tau];
                yin[tau] += diff * diff;
            }

            sum += yin[tau];

            if (sum == 0)
                yin[tau] *= tau / sum;
            else
                yin[tau] = 1f;

            if (yin[tau] < yin[min_pos])
                min_pos = tau;

            int period = tau - 3;

            if (tau > 4 && yin[period] < this.threshold && yin[period] < yin[period + 1])
            {
                peak_pos = period;
                break;
            }
        }
        if (peak_pos == -1)
        {
            peak_pos = min_pos;
            if (yin[peak_pos] >= this.threshold)
                return -1;
        }

        float t0 = get_quadratic_peak_x(yin, peak_pos);
        float hz = (t0!=0) ? this.sample_rate / t0 : -1;
        return hz;

    }
    public float get_quadratic_peak_x(float[] data, int pos){
        if (pos == 0 || pos == data.length-1 || data.length < 3)
            return pos;

        float A = data[pos-1];
        float B = data[pos  ];
        float C = data[pos+1];
        float D = A - 2*B + C;
        return  pos - (C-A) / (2*D);
    }
}
