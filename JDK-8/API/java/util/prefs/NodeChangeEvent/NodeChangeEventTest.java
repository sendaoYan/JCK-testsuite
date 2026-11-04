/*
 * @test
 * @summary java.util.prefs.NodeChangeEvent conformance: white-box structure + black-box behavior.
 * @run main NodeChangeEventTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.EventObject;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.Preferences;

public class NodeChangeEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.NodeChangeEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getSuperclass() == EventObject.class, "extends EventObject");

        Constructor<?> ctor = c.getConstructor(Preferences.class, Preferences.class);
        check(Modifier.isPublic(ctor.getModifiers()), "public (Preferences, Preferences) ctor");
        c.getMethod("getParent");
        c.getMethod("getChild");
        check(true, "getParent/getChild present");
    }

    // in-memory node for constructing the event without touching real prefs
    static class Mem extends AbstractPreferences {
        final Map<String, String> store = new TreeMap<>();
        final Map<String, Mem> children = new TreeMap<>();
        Mem(AbstractPreferences parent, String name) { super(parent, name); }
        @Override protected String getSpi(String key) { return store.get(key); }
        @Override protected void putSpi(String key, String value) { store.put(key, value); }
        @Override protected void removeSpi(String key) { store.remove(key); }
        @Override protected String[] keysSpi() { return store.keySet().toArray(new String[0]); }
        @Override protected String[] childrenNamesSpi() { return children.keySet().toArray(new String[0]); }
        @Override protected AbstractPreferences childSpi(String name) {
            Mem c = children.get(name);
            if (c == null) { c = new Mem(this, name); children.put(name, c); }
            return c;
        }
        @Override protected void removeNodeSpi() {
            AbstractPreferences p = (AbstractPreferences) parent();
            if (p instanceof Mem) ((Mem) p).children.remove(name());
        }
        @Override protected void flushSpi() {}
        @Override protected void syncSpi() {}
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Mem parent = new Mem(null, "");
        Mem child = (Mem) parent.node("child");

        NodeChangeEvent e = new NodeChangeEvent(parent, child);
        check(e.getParent() == parent, "getParent exact");
        check(e.getChild() == child, "getChild exact");
        check(e.getSource() == parent, "EventObject source == parent");
        check(e instanceof EventObject, "is EventObject");

        // constructing with distinct in-memory nodes keeps them independent
        Mem other = new Mem(null, "");
        NodeChangeEvent e2 = new NodeChangeEvent(other, (Mem) other.node("x"));
        check(e2.getParent() == other && e2.getChild() != child, "independent event payload");
    }


    // ===================== 契约深化：childAdded/childRemoved 事件字段（真实触发，隔离节点） =====================
    static void testContracts() throws Exception {
        java.util.prefs.Preferences parent = java.util.prefs.Preferences.userRoot().node("/jck/testNCE1");
        try {
            final java.util.List<java.util.prefs.NodeChangeEvent> events =
                    java.util.Collections.synchronizedList(new java.util.ArrayList<java.util.prefs.NodeChangeEvent>());
            parent.addNodeChangeListener(new java.util.prefs.NodeChangeListener() {
                public void childAdded(java.util.prefs.NodeChangeEvent e) { events.add(e); }
                public void childRemoved(java.util.prefs.NodeChangeEvent e) { events.add(e); }
            });
            java.util.prefs.Preferences c = parent.node("newchild");
            java.util.prefs.Preferences.userRoot().flush();
            for (int i = 0; i < 100 && events.isEmpty(); i++) Thread.sleep(20);
            check(events.size() >= 1, "childAdded fired");
            if (!events.isEmpty()) {
                java.util.prefs.NodeChangeEvent ev = events.get(0);
                check(ev.getParent() == parent, "event parent is source node");
                check(ev.getChild() != null && "newchild".equals(ev.getChild().name()), "event child name");
            }
            events.clear();
            c.removeNode();
            java.util.prefs.Preferences.userRoot().flush();
            for (int i = 0; i < 100 && events.isEmpty(); i++) Thread.sleep(20);
            check(events.size() >= 1, "childRemoved fired");
            if (!events.isEmpty())
                check(events.get(0).getParent() == parent, "remove event parent is source");
        } finally {
            try { parent.removeNode(); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
