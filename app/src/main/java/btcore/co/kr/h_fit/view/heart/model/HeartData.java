package btcore.co.kr.h_fit.view.heart.model;

/**
 * Created by leehaneul on 2018-02-01.
 */

public interface HeartData {
    interface View{
        void showData(String data, int type );
        void showErrorMesssage(String msg);
    }

    interface Presenter{
        void initBleData(String BleData);
    }

}
