package btcore.co.kr.h_fit.bus;

/**
 * Created by leehaneul on 2018-02-07.
 */

public class BusPhoneToDeviceEvent {
    private String eventData;

    public BusPhoneToDeviceEvent(String eventData) {
        this.eventData = eventData;
    }

    public String getEventData() {
        return eventData;
    }


}
