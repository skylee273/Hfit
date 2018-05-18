package btcore.co.kr.h_fit.view.cal.presenter;

import btcore.co.kr.h_fit.view.cal.model.KalData;
import btcore.co.kr.h_fit.view.cal.model.KalModel;

/**
 * Created by leehaneul on 2018-02-01.
 */

public class KalPresenter implements KalData.Presenter {

    KalData.View kalDadaView;
    KalModel kalModel;

    public KalPresenter(KalData.View kalDadaView){
        this.kalDadaView = kalDadaView;
        this.kalModel = new KalModel();
    }

    @Override
    public void initBleData(String BleData) {
        kalModel.setKaltData(BleData);
        kalDadaView.showData(kalModel.splitData(),kalModel.checkType());
    }
}
