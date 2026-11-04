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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
