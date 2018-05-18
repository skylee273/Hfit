package btcore.co.kr.h_fit.bus;

/**
 * Created by leehaneul on 2018-02-01.
 */

public class BusFragmentEvent {
    private String eventData;
    private int type;

    public BusFragmentEvent(String eventData, int type) {

        this.eventData = eventData;
        this.type = type;
    }

    public String getEventData() {
        return eventData;
    }

    public int getEventType(){
        return type;
    }
}
