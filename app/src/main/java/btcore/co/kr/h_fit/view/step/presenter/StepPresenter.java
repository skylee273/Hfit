package btcore.co.kr.h_fit.view.step.presenter;

import btcore.co.kr.h_fit.view.step.model.StepData;
import btcore.co.kr.h_fit.view.step.model.StepModel;

/**
 * Created by leehaneul on 2018-02-01.
 */

public class StepPresenter implements StepData.Presenter {

    StepData.View stepDataView;
    StepModel stepModel;

    public StepPresenter(StepData.View stepDataView){
        this.stepDataView = stepDataView;
        this.stepModel = new StepModel();
    }

    @Override
    public void initBleData(String BleData) {
        stepModel.setStepData(BleData);
        stepDataView.showData(stepModel.splitData(), stepModel.checkType());
    }
}