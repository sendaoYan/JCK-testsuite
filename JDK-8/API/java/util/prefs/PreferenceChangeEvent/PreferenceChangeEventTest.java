/*
 * @test
 * @summary java.util.prefs.PreferenceChangeEvent conformance: white-box structure + black-box behavior.
 * @run main PreferenceChangeEventTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.EventObject;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

public class PreferenceChangeEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.PreferenceChangeEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getSuperclass() == EventObject.class, "extends EventObject");

        Constructor<?> ctor = c.getConstructor(Preferences.class, String.class, String.class);
        check(Modifier.isPublic(ctor.getModifiers()), "public (Preferences, String, String) ctor");
        c.getMethod("getNode");
        c.getMethod("getKey");
        c.getMethod("getNewValue");
        check(true, "getNode/getKey/getNewValue present");
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
        Mem node = new Mem(null, "");
        PreferenceChangeEvent e = new PreferenceChangeEvent(node, "key", "newValue");
        check(e.getNode() == node, "getNode exact");
        check("key".equals(e.getKey()), "getKey exact");
        check("newValue".equals(e.getNewValue()), "getNewValue exact");
        check(e.getSource() == node, "EventObject source == node");
        check(e instanceof EventObject, "is EventObject");

        // newValue may be null (a removal notification)
        PreferenceChangeEvent removal = new PreferenceChangeEvent(node, "gone", null);
        check(removal.getNewValue() == null, "null newValue allowed");
        check("gone".equals(removal.getKey()), "removal key");
    }


    // =====================契约深化：key/newValue/oldValue/source 字段（真实触发，隔离节点） =====================
    static void testContracts() throws Exception {
        java.util.prefs.Preferences n = java.util.prefs.Preferences.userRoot().node("/jck/testPCE1");
        try {
            final java.util.List<java.util.prefs.PreferenceChangeEvent> events =
                    java.util.Collections.synchronizedList(new java.util.ArrayList<java.util.prefs.PreferenceChangeEvent>());
            n.addPreferenceChangeListener(events::add);
            n.put("k1", "v1");
            n.sync();
            check(events.size() == 1, "put fires exactly one event (after sync dispatch)");
            if (!events.isEmpty()) {
                java.util.prefs.PreferenceChangeEvent ev = events.get(0);
                check(ev.getNode() == n, "event node identity");
                check("k1".equals(ev.getKey()), "event key");
                check("v1".equals(ev.getNewValue()), "event newValue");
            }
            // 移除触发 newValue=null
            events.clear();
            n.remove("k1");
            n.sync();
            check(events.size() == 1, "remove fires one event");
            check(events.isEmpty() || events.get(0).getNewValue() == null, "remove event newValue null");
            // 覆盖触发新值
            events.clear();
            n.put("k2", "a");
            n.put("k2", "b");
            n.sync();
            check(events.size() == 2, "two puts fire two events");
            check("b".equals(events.get(1).getNewValue()), "second event carries new value");
        } finally {
            try { n.removeNode(); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
