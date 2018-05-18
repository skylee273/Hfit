package btcore.co.kr.h_fit.view.main.presenter;

import btcore.co.kr.h_fit.view.main.model.HfitData;
import btcore.co.kr.h_fit.view.main.model.HfitModel;

/**
 * Created by leehaneul on 2018-02-01.
 */

public class HfitPresenter implements HfitData.Presenter {

    HfitData.View bleDataView;
    HfitModel hfitModel;

    public HfitPresenter(HfitData.View bleDataView){
        this.bleDataView = bleDataView;
        this.hfitModel = new HfitModel();
    }

    @Override
    public void initBleData(String BleData) {
        hfitModel.setBleData(BleData);
        bleDataView.showData(hfitModel.splitData(), hfitModel.checkType());
    }
}
