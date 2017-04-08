package org.plantainreader;

import android.nfc.tech.MifareClassic;

import java.io.IOException;
import java.util.Arrays;

public class Sector {
    static final int SECTOR_SIZE = 4;
    static final int BLOCK_SIZE = MifareClassic.BLOCK_SIZE;

    private byte[][] data;

    public Sector() {
        this.data = new byte[SECTOR_SIZE][BLOCK_SIZE];
    }

    public byte[] getBlock(int blockIndex) {
        return data[blockIndex];
    }

    public  byte[] bytes(int blockIndex, int startByte, int count) {
        byte[] block = this.getBlock(blockIndex);
        return Arrays.copyOfRange(block, startByte, startByte+count);
    }

    public void read(MifareClassic tech, int block) throws IOException {
        for (int i = 0; i < SECTOR_SIZE; i++) {
            data[i] = tech.readBlock(block + i);
        }
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for(byte[] block: data){
            sb.append(DumpUtil.bytesToHex(block));
            sb.append("\n");
        }
        return sb.toString();
    }

    public void deserialize(String s) {
        String[] stringBlocks = s.split("\n");
        for (int i = 0; i < SECTOR_SIZE; i++) {
            data[i] = DumpUtil.hexToBytes(stringBlocks[i]);
        }
    }
}
