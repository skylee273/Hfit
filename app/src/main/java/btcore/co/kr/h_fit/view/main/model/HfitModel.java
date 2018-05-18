package btcore.co.kr.h_fit.view.main.model;

/**
 * Created by leehaneul on 2018-02-01.
 */

public class HfitModel {

    private final String TAG = getClass().getSimpleName();
    private String Data[];
    private int DataLength;
    private int DataType;


    public void setBleData(String bleData ){
       this.Data = bleData.split("-");
       this.DataLength = Integer.parseInt(Data[2]);
       this.DataType = Integer.parseInt(Data[3]);
    }

    public int checkType(){
        return DataType;
    }

    public String splitData(){
        int startIndex = 4;
        int endIndex = 0;
        String ByteToStr = null;
       for(int i = 0; i < DataLength +2; i++){
           if(Data[i].equals("AB") && Data[i+1].equals("CD")){
               endIndex = i;
           }
       }
        ByteToStr = Data[startIndex];

        for(int i = startIndex + 1; i < endIndex; i++ ){
            ByteToStr += Data[i];
        }
        int intValue = Integer.parseInt(ByteToStr,16);
        ByteToStr = String.valueOf(intValue);
        return ByteToStr;
    }

}
