package org.plantainreader;

import android.nfc.tech.MifareClassic;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.TimeZone;

class Dump implements Serializable {
    static final int SECTOR_SIZE = 4;
    static final int BLOCK_SIZE = MifareClassic.BLOCK_SIZE;

    private String tagName;

    String getTagName() {
        return tagName;
    }

    byte[][] sector4;
    byte[][] sector5;
    byte[][] sector9;


    Dump(byte[] tagName) {
        this.tagName = Base64.encodeToString(tagName, Base64.URL_SAFE);
        this.sector4 = new byte[SECTOR_SIZE][BLOCK_SIZE];
        this.sector5 = new byte[SECTOR_SIZE][BLOCK_SIZE];
        this.sector9 = new byte[SECTOR_SIZE][BLOCK_SIZE];
    }

    public Dump(byte[] tagName, byte[][] sector4, byte[][] sector5) {
        this.tagName = Base64.encodeToString(tagName, Base64.URL_SAFE);
        this.sector4 = sector4;
        this.sector5 = sector5;
    }

    String serializeToString() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            byte[] result = bos.toByteArray();
            return Base64.encodeToString(result, Base64.URL_SAFE);
        } finally {
            try {
                bos.close();
            } catch (IOException ignored) {
            }
        }
    }

    static Dump deserializeFromString(String input) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(input, Base64.URL_SAFE));
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return (Dump) o;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public String getBalance() {
        byte[] block = sector4[0];
        return DumpUtil.getRubles(block[0], block[1]);
    }

    public String getSubwayTravelCount() {
        byte[] block = sector5[1];
        int travelCount = DumpUtil.convertBytes(block[0]);
        return Integer.toString(travelCount);
    }

    public String getOnGroundTravelCount() {
        byte[] block = sector5[1];
        int travelCount = DumpUtil.convertBytes(block[1]);
        return Integer.toString(travelCount);
    }

    public String getLastDate() {
        byte[] block = sector5[0];
        int timeDiff = DumpUtil.convertBytes(block[0], block[1], block[2], block[3]);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"));
        c.set(2010, 0, 1, 0, 0, 0);
        c.add(Calendar.MINUTE, timeDiff);
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(c.getTime());
    }

    public String getLastTravelCost() {
        byte[] block = sector5[0];
        return DumpUtil.getRubles(block[6], block[7]);
    }

    public String getLastValidator() {
        byte[] block = sector5[0];
        int validatorID = DumpUtil.convertBytes(block[4], block[5]);
        return Integer.toString(validatorID);
    }

}
