package btcore.co.kr.h_fit.bus;

/**
 * Created by leehaneul on 2018-02-07.
 */

public class BusPhoneToDeviceProvider extends CustomBus {
    private static final CustomBus BUS = new CustomBus();

    public static CustomBus getInstance() {
        return BUS;
    }

    private BusPhoneToDeviceProvider() {
    }
}

