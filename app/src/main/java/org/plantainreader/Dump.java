package org.plantainreader;

import android.nfc.tech.MifareClassic;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.IOException;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Pattern;

class Dump implements Parcelable{
    private String tagName;
    private SparseArray<Sector> sectors;

    private Dump(String tagName, Sector sector4, Sector sector5) {
        this.tagName = tagName;
        sectors = new SparseArray<>();
        sectors.put(4, sector4);
        sectors.put(5, sector5);
    }

    Dump(String tagName) {
        this(tagName, new Sector(), new Sector());
    }

    Dump(Parcel p) {
        this(p.readString());
        deserialize(p.readString());
    }

    public static final Parcelable.Creator<Dump> CREATOR
              = new Parcelable.Creator<Dump>() {
          public Dump createFromParcel(Parcel in) {
              return new Dump(in);
          }
          public Dump[] newArray(int size) {
              return new Dump[size];
          }
      };

    String getTagName() {
        return tagName;
    }

    public Sector sector(int sector) {
        return sectors.get(sector);
    }

    public String getBalance() {
        // sector 4, block 0, bytes 0, 1, 2, 3
        return DumpUtil.getRubles(sector(4).bytes(0, 0, 4));
    }

    public String getSubwayTravelCount() {
        // sector 5, block 1, byte 0
        int travelCount = DumpUtil.convertBytes(sector(5).bytes(1, 0, 1));
        return Integer.toString(travelCount);
    }

    public String getOnGroundTravelCount() {
        // sector 5, block 1, byte 1
        int travelCount = DumpUtil.convertBytes(sector(5).bytes(1, 1, 1));
        return Integer.toString(travelCount);
    }

    public Calendar getLastDate() {
        // sector 5, block 0, bytes 0, 1, 2
        return DumpUtil.getDate(sector(5).bytes(0, 0, 3));
    }

    public String getLastTravelCost() {
        // sector 5, block 0, bytes 6, 7
        return DumpUtil.getRubles(sector(5).bytes(0, 6, 2));
    }

    public String getLastValidator() {
        // sector 5, block 0, bytes 4, 5
        int validatorID = DumpUtil.convertBytes(sector(5).bytes(0, 4, 2));
        return Integer.toString(validatorID);
    }

    public Calendar getLastPaymentDate() {
        // sector 4, block 2, bytes 2, 3, 4
        Calendar c = DumpUtil.getDate(sector(4).bytes(2, 2, 3));
        return c;
    }

    public String getLastPaymentValue() {
        // sector 4, block 2, bytes 8, 9, 10
        return DumpUtil.getRubles(sector(4).bytes(2, 8, 3));
    }

    public void readSector(MifareClassic tech, int sectorId) throws IOException {
        Sector sector = this.sector(sectorId);
        int block = tech.sectorToBlock(sectorId);
        sector.read(tech, block);
    }

    public String serialize() {

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < sectors.size(); i++){
            int n = sectors.keyAt(i);
            Sector s = sectors.get(n);
            sb.append("+Sector: ");
            sb.append(n);
            sb.append("\n");
            sb.append(s.serialize());
        }
        return sb.toString();
    }

    public void deserialize(String s) {
        Pattern sectorStart = Pattern.compile("\\+Sector:");
        Scanner scanner = new Scanner(s);
        while(scanner.hasNextLine()){
            if(scanner.hasNext(sectorStart)) {
                scanner.skip(sectorStart);
                int n = scanner.nextInt();
                scanner.skip("\n");
                StringBuilder sectorData = new StringBuilder();
                while(scanner.hasNextLine() && !scanner.hasNext(sectorStart)){
                    sectorData.append(scanner.nextLine());
                    sectorData.append("\n");
                }
                Sector sector = new Sector();
                sector.deserialize(sectorData.toString());
                sectors.put(n, sector);
            }
            else {
                break;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tagName);
        dest.writeString(this.serialize());
    }
}
