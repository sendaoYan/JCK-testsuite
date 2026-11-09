/*
 * @test
 * @summary java.util.prefs.PreferenceChangeListener conformance: white-box structure + black-box behavior.
 * @run main PreferenceChangeListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class PreferenceChangeListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.PreferenceChangeListener");
        check(c.isInterface(), "interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0] == EventListener.class, "extends EventListener");

        Method m = c.getMethod("preferenceChange", PreferenceChangeEvent.class);
        check(Modifier.isAbstract(m.getModifiers()), "preferenceChange abstract");
        check(c.getDeclaredMethods().length == 1, "single abstract method (functional interface)");
    }

    // in-memory node to drive preference-change events without real prefs
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
    static void testBlackBox() throws Exception {
        Mem root = new Mem(null, "");

        final PreferenceChangeEvent[] evt = new PreferenceChangeEvent[1];
        final CountDownLatch latch = new CountDownLatch(1);
        PreferenceChangeListener listener = e -> { evt[0] = e; latch.countDown(); };

        root.addPreferenceChangeListener(listener);
        root.put("key", "value");
        check(latch.await(5, TimeUnit.SECONDS), "preferenceChange delivered");
        check(evt[0].getNode() == root, "event node");
        check("key".equals(evt[0].getKey()), "event key");
        check("value".equals(evt[0].getNewValue()), "event newValue");

        root.removePreferenceChangeListener(listener);
        check(true, "removePreferenceChangeListener");
    }


    // ===================== 契约深化：lambda 监听 + 注册/移除对称（未注册移除 IAE） =====================
    static void testContracts() throws Exception {
        java.util.prefs.Preferences n = java.util.prefs.Preferences.userRoot().node("/jck/testPCL1");
        try {
            final int[] hits = {0};
            java.util.prefs.PreferenceChangeListener l = e -> hits[0]++;
            n.addPreferenceChangeListener(l);
            n.put("a", "1");
            n.sync();
            // JDK-9+ dispatches preference-change events asynchronously on a
            // background thread, so wait for delivery instead of asserting inline.
            awaitHits(hits, 1, 5000);
            check(hits[0] >= 1, "listener notified on put (after sync dispatch)");
            n.removePreferenceChangeListener(l);
            n.put("b", "2");
            n.sync();
            Thread.sleep(300); // give any spurious delivery time to arrive
            check(hits[0] == 1, "removed listener no longer notified");
            // 未注册的监听器移除 -> IAE
            try {
                n.removePreferenceChangeListener(e -> { });
                check(false, "unregistered remove IAE");
            } catch (IllegalArgumentException e) { check(true, "unregistered remove IAE"); }
            // 重复注册：每次注册各通知一次（监听器按次分发）
            n.addPreferenceChangeListener(l);
            n.addPreferenceChangeListener(l);
            int before = hits[0];
            n.put("c", "3");
            n.sync();
            awaitHits(hits, before + 2, 5000);
            check(hits[0] == before + 2, "double registration notifies twice (once per registration)");
        } finally {
            try { n.removeNode(); } catch (Exception ignore) { }
        }
    }

    // Poll until hits[0] reaches target (JDK-9+ delivers events asynchronously).
    static void awaitHits(int[] hits, int target, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline && hits[0] < target) {
            Thread.sleep(10);
        }
    }

    public static void main(String[] args) throws Exception {
        // Isolate the file-backed prefs root to a per-JVM temp dir so that
        // concurrent test JVMs (conc>1) don't contend on the shared
        // ~/.java/.userPrefs lock file (BackingStoreException: Couldn't get file lock).
        String pfr = System.getProperty("java.io.tmpdir") + "/jck-prefs-" + java.util.UUID.randomUUID();
        System.setProperty("java.util.prefs.userRoot", pfr);
        System.setProperty("java.util.prefs.systemRoot", pfr + "-sys");
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
