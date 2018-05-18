package btcore.co.kr.h_fit.view.heart.presenter;

import btcore.co.kr.h_fit.view.heart.model.HeartData;
import btcore.co.kr.h_fit.view.step.model.StepModel;

/**
 * Created by leehaneul on 2018-02-01.
 */

public class HeartPresenter implements HeartData.Presenter {

    HeartData.View heartDataView;
    StepModel heartModel;

    public HeartPresenter(HeartData.View heartDataView){
        this.heartDataView = heartDataView;
        this.heartModel = new StepModel();
    }

    @Override
    public void initBleData(String BleData) {
        heartModel.setStepData(BleData);
        heartDataView.showData(heartModel.splitData(), heartModel.checkType());
    }
}