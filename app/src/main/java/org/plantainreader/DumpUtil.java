package org.plantainreader;

import android.util.Log;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

public class DumpUtil {
    public static final String LOG = "DumpUtil";
    public static final Locale locale = new Locale("ru", "RU");
    public static final TimeZone tz = TimeZone.getTimeZone("GMT+3");
    public static final DateFormat FULL_DT_FMT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

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

    public static String getRubles(byte... b) {
        int value = DumpUtil.convertBytes(b);
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance();
        formatter.setMaximumFractionDigits(0);
        formatter.setCurrency(Currency.getInstance("RUR"));
        formatter.setNegativePrefix(formatter.getCurrency().getSymbol() + "-");
        formatter.setNegativeSuffix("");
        return formatter.format(((double) value) / 100.0d);
    }

    public static Calendar getDate(byte... b) {
        int timeDiff = DumpUtil.convertBytes(b);
        Calendar c = Calendar.getInstance(tz);
        c.set(2010, 0, 1, 0, 0, 0);
        c.add(Calendar.MINUTE, timeDiff);
        return c;
    }

    public static String formatDateFull(Calendar c) {
        return DumpUtil.FULL_DT_FMT.format(c.getTime());
    }

    public static String formatDateMY(Calendar c) {
        Calendar now = Calendar.getInstance(tz);
        String format = "LLLL";
        if(now.get(Calendar.YEAR) != c.get(Calendar.YEAR)) {
            format += " yyyy";
        }
        SimpleDateFormat fmt = new SimpleDateFormat(format, locale);
        return fmt.format(c.getTime()).toLowerCase();
    }
}
