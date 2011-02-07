package au.org.emii.portal.composer;

import au.org.emii.portal.value.BoundingBox;
import java.util.HashMap;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

public class LeftMenuSearchComposer extends UtilityComposer {

    private static final long serialVersionUID = 2540820748110129339L;

    private double north;
    private double south;
    private double east;
    private double west;

    //map of event listeners for viewport changes (west Doublebox onchange)
    HashMap<String, EventListener> viewportChangeEvents = new HashMap<String,EventListener>();

    @Override
    public void afterCompose() {
        super.afterCompose();
    }

    public BoundingBox getViewportBoundingBox() {
        BoundingBox bbox = new BoundingBox();
        bbox.setMaxLatitude((float)north);
        bbox.setMinLatitude((float)south);
        bbox.setMinLongitude((float)west);
        bbox.setMaxLongitude((float)east);
        return bbox;
    }
    
    public void setExtents(Event event) {
        String [] extents = ((String) event.getData()).split(",");
        west = Double.parseDouble(extents[0]);
        north = Double.parseDouble(extents[1]);
        east = Double.parseDouble(extents[2]);
        south = Double.parseDouble(extents[3]);
        
        Events.echoEvent("triggerViewportChange", this, null);
    }

    public void triggerViewportChange(Event e) throws Exception {
        //update bounding box for this session
        BoundingBox bb = new BoundingBox();
        bb.setMinLatitude((float)south);
        bb.setMaxLatitude((float)north);
        bb.setMinLongitude((float)west);
        bb.setMaxLongitude((float)east);
        getMapComposer().getPortalSession().setDefaultBoundingbox(bb);
        
        for (EventListener el : viewportChangeEvents.values()) {
            try {
                el.onEvent(null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void addViewportEventListener(String eventName, EventListener eventListener) {
        viewportChangeEvents.put(eventName, eventListener);
    }

    public void removeViewportEventListener(String eventName) {
        viewportChangeEvents.remove(eventName);
    }
}