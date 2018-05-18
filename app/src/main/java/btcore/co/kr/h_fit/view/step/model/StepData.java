package btcore.co.kr.h_fit.view.step.model;

/**
 * Created by leehaneul on 2018-02-01.
 */

public interface StepData {

    interface View{
        void showData(String data , int type);
        void showErrorMesssage(String msg);
    }

    interface Presenter{
        void initBleData(String BleData);
    }


}
