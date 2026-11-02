/*
 * @test
 * @summary java.beans.EventHandler conformance: create proxy and delegate.
 * @run main EventHandlerTest
 */

import java.beans.*;

public class EventHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.List<String> list = new java.util.ArrayList<>();
        PropertyChangeListener l = EventHandler.create(PropertyChangeListener.class, list, "add", "newValue", "propertyChange");
        l.propertyChange(new PropertyChangeEvent(new Object(), "p", null, "added"));
        check(list.size() == 1 && "added".equals(list.get(0)), "delegated add");
        EventHandler h = new EventHandler(list, "add", "newValue", "propertyChange");
        check(h.getTarget() == list, "target");
        check("add".equals(h.getAction()), "action");
        check("newValue".equals(h.getEventPropertyName()), "event property");
        check("propertyChange".equals(h.getListenerMethodName()), "listener method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
