package org.plantainreader;

import android.util.Log;

public class DumpUtil {
    public static final String LOG = "DumpUtil";

    public static Integer convertBytes(byte... b) {
        int result = 0;
        for (int i = 0; i < b.length; i++) {
            int shift = (i == 0) ? 0 : (2 << (i + 1));
            Log.d(LOG, (b[i] & 0xFF) + " << " + shift + " = " + ((b[i] & 0xFF) << shift));
            result += (b[i] & 0xFF) << shift;
        }
        Log.d(LOG, "Result = " + result);
        return result;
    }

    public static String getHexString(byte[] b, String separator) {
        StringBuilder result = new StringBuilder();
        for (byte b2 : b) {
            result.append(Integer.toString((b2 & 255) + 256, 16).substring(1).toUpperCase());
            result.append(separator);
        }
        return result.substring(0, result.length() - separator.length());
    }
}
