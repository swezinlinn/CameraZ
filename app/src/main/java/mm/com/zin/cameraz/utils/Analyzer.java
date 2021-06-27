package mm.com.zin.cameraz.utils;

import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class Analyzer implements ImageAnalysis.Analyzer {
    private long lastAnalyzedTimestamp = 0L;

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - lastAnalyzedTimestamp >=
                TimeUnit.SECONDS.toMillis(1)) {
            // Since format in ImageAnalysis is YUV, image.planes[0]
            // contains the Y (luminance) plane
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            // Extract image data from callback object
            byte[] data = toByteArray(buffer);

            // Convert the data into an array of pixel values
            int sum = 0;
            for (byte val : data) {
                // Add pixel value
                sum += (((int) val) & 0xFF);
            }

            // Compute average luminance for the image
            double luma = sum / ((double) data.length);
            Log.d(TAG, "Average Luminosity " + luma);
            // Update timestamp of last analyzed frame
            lastAnalyzedTimestamp = currentTimestamp;
        }
    }

    private static byte[] toByteArray(ByteBuffer buffer) {
        buffer.rewind();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }
}
