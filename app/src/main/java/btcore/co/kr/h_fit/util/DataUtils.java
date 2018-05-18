package btcore.co.kr.h_fit.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by leehaneul on 2018-02-05.
 */

public class DataUtils {
    private final String TAG = getClass().getSimpleName();

    private byte HfitStartPacket = 0x7f;
    private byte HfitNotiPacket = 0x0F;
    private byte HfitSmsPacket = 0x03;
    private byte HfitKakaoPacket = 0x04;
    private byte HFitCallPacket = 0x00;
    private byte HfitEndPacket = (byte) 0xff;

    public String requsetData() {
        byte[] bytes = new byte[6];
        bytes[0] = 0x7f;      //Start ID
        bytes[1] = 0x03;      //Length
        bytes[2] = 0x06;      //Type
        bytes[3] = 0x01;
        bytes[4] = (byte) 0xff;
        String byteToStr;
        byteToStr = new String(bytes);
        return byteToStr;
    }

    public String kakaoGet(int KakaoMissMessage, String KakaoName, String KakaoMessage) {
        ArrayList<Byte> Data = new ArrayList<>();
        byte[] HfitKakaoNamePacket = null;
        byte[] HfitKakaoMessagePacket = null;
        int HfitPacketLength = 0;

        HfitKakaoNamePacket = KakaoName.getBytes();
        HfitKakaoMessagePacket = KakaoMessage.getBytes();
        int nameLength = KakaoName.length();
        Data.add(HfitStartPacket);
        Data.add(HfitStartPacket);
        Data.add(HfitNotiPacket);
        Data.add(HfitKakaoPacket);
        Data.add((byte) KakaoMissMessage);
        Data.add((byte) nameLength);
        for (byte HfitKakaoNameToByte : HfitKakaoNamePacket) {
            Data.add(HfitKakaoNameToByte);
        }
        for (byte HfitKakaoMsgToByte : HfitKakaoMessagePacket) {
            Data.add(HfitKakaoMsgToByte);
        }
        if (Data.size() > 20) {
            HfitPacketLength = 20;
            Data.set(1, (byte) 18);
            Data.set(19, HfitEndPacket);
        } else {
            Data.add(HfitEndPacket);
            HfitPacketLength = Data.size();
            Data.set(1, (byte) Data.size());
        }

        byte[] KakaoByte = new byte[HfitPacketLength];

        for (int i = 0; i < HfitPacketLength - 1; i++) {
            KakaoByte[i] = Data.get(i);
        }

        String ByteToStr;
        ByteToStr = new String(KakaoByte);
        return ByteToStr;

    }

    public String smsGet(int HfitSmsMiss, String HfitSmsName, String HfitSmsMsg) {

        ArrayList<Byte> Data = new ArrayList<>();

        int SmsMsgLength = 0;
        int SmsNameLength = 0;
        byte[] SmsNameVal = null;
        byte[] SmsMsgVal = null;

        SmsMsgLength = HfitSmsMsg.length();
        SmsNameLength = HfitSmsName.length();

        if (SmsMsgLength > 3) {
            HfitSmsMsg = HfitSmsMsg.substring(0, 2);
        }

        SmsNameVal = HfitSmsName.getBytes();
        SmsMsgVal = HfitSmsMsg.getBytes();

        Data.add(HfitStartPacket);
        Data.add(HfitStartPacket);
        Data.add(HfitNotiPacket);
        Data.add(HfitSmsPacket);
        Data.add((byte) HfitSmsMiss);
        Data.add((byte) SmsNameLength);
        for (byte _nameValue : SmsNameVal) {
            Data.add(_nameValue);
        }
        for (byte _messageValue : SmsMsgVal) {
            Data.add(_messageValue);
        }
        Data.add(HfitEndPacket);
        Data.set(1, (byte) Data.size());

        byte[] SmsByte = new byte[Data.size()];

        for (int i = 0; i < Data.size() - 1; i++) {
            SmsByte[i] = Data.get(i);
        }

        String ByteToStr;
        ByteToStr = new String(SmsByte);
        return ByteToStr;
    }

    public String callGetByte(String HfitCall) {

        ArrayList<Byte> Data = new ArrayList<>();

        byte[] nameValue = HfitCall.getBytes();

        Data.add(HfitStartPacket);
        Data.add(HfitStartPacket);
        Data.add(HfitNotiPacket);
        Data.add(HFitCallPacket);
        Data.add((byte) 0x01);
        Data.add((byte) (nameValue.length));
        for (byte _nameValue : nameValue) {
            Data.add(_nameValue);
        }
        Data.add(HfitEndPacket);
        Data.set(1, (byte) (Data.size() - 2));
        byte[] CallByte = new byte[Data.size()];

        for (int i = 0; i < Data.size() - 1; i++) {
            CallByte[i] = Data.get(i);
        }

        String ByteToStr;
        ByteToStr = new String(CallByte);
        return ByteToStr;
    }

    public String endCallGetByte(String HfitCallEndName) {

        ArrayList<Byte> data = new ArrayList<>();

        byte[] CallNameByte = HfitCallEndName.getBytes();

        data.add(HfitStartPacket);
        data.add(HfitStartPacket);
        data.add(HfitNotiPacket);
        data.add(HFitCallPacket);
        data.add((byte) 0x00);
        data.add((byte) (CallNameByte.length));
        for (byte _nameValue : CallNameByte) {
            data.add(_nameValue);
        }
        data.add(HfitEndPacket);
        data.set(1, (byte) (data.size() - 2));
        byte[] CallByte = new byte[data.size()];

        for (int i = 0; i < data.size() - 1; i++) {
            CallByte[i] = data.get(i);
        }

        String ByteToStr;
        ByteToStr = new String(CallByte);
        return ByteToStr;
    }

    public String missCallGetByte(int MissCount) {

        byte[] HfitMissData = new byte[20];

        HfitMissData[0] = 0x7f;      //Start ID
        HfitMissData[1] = 0x04;      //Length
        HfitMissData[2] = 0x0f;      //Type
        HfitMissData[3] = 0x02;      //D1  IncomingCall = 0x00
        HfitMissData[4] = (byte) MissCount;      //D2  start - 0x01
        HfitMissData[5] = (byte) 0xff;

        String ByteToStr;
        ByteToStr = new String(HfitMissData);
        return ByteToStr;
    }

    public String getTimeInfo(String TimeData, int WeekData) {

        byte[] Data = new byte[20];

        Data[0] = 0x7f;      //Start ID
        Data[1] = 0x11;      //Length
        Data[2] = 0x0A;      //Type
        Data[3] = (byte) TimeData.charAt(0);      //2        //year
        Data[4] = (byte) TimeData.charAt(1);      //0
        Data[5] = (byte) TimeData.charAt(2);         //1
        Data[6] = (byte) TimeData.charAt(3);         //8
        Data[7] = (byte) TimeData.charAt(4);         //0         //month
        Data[8] = (byte) TimeData.charAt(5);         //2
        Data[9] = (byte) TimeData.charAt(6);         //0         //date
        Data[10] = (byte) TimeData.charAt(7);         //5
        Data[11] = (byte) TimeData.charAt(8);         //0        //hour
        Data[12] = (byte) TimeData.charAt(9);         //8
        Data[13] = (byte) TimeData.charAt(10);         //2       //minute
        Data[14] = (byte) TimeData.charAt(11);         //2
        Data[15] = (byte) TimeData.charAt(12);         //1           /second
        Data[16] = (byte) TimeData.charAt(13);         //1
        Data[17] = (byte) WeekData;         //1
        Data[18] = (byte) 0xff;

        String ByteToStr;
        ByteToStr = new String(Data);

        return ByteToStr;

    }

    public int getWeek() {
        Calendar cal = Calendar.getInstance();

        int week = cal.get(cal.DAY_OF_WEEK);

        return (week - 1);
    }

    public String getDate() {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        Date currentTime = new Date();
        String mTime = mSimpleDateFormat.format(currentTime);

        return mTime;
    }

}
