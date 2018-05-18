package btcore.co.kr.h_fit.view.cal.model;

/**
 * Created by leehaneul on 2018-02-01.
 */

public class KalModel {

    private String mBleData[];
    private int length;
    private int type;


    public void setKaltData(String bleData) {
        this.mBleData = bleData.split("-");
        this.length = Integer.parseInt(mBleData[2]);
        this.type = Integer.parseInt(mBleData[3]);
    }

    public int checkType(){
        return type;
    }


    public String splitData() {
        int startIndex = 4;
        int endIndex = 0;
        String temp = null;

        for (int i = 0; i < length + 2; i++) {
            if (mBleData[i].equals("AB") && mBleData[i + 1].equals("CD")) {
                endIndex = i;
            }
        }
        temp = mBleData[startIndex];

        for (int i = startIndex + 1; i < endIndex; i++) {
            temp += mBleData[i];
        }
        int intValue = Integer.parseInt(temp, 16);
        temp = String.valueOf(intValue);


        return temp;
    }


}
