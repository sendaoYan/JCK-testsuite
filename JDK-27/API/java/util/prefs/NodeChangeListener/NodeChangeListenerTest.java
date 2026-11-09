/*
 * @test
 * @summary java.util.prefs.NodeChangeListener conformance: white-box structure + black-box behavior.
 * @run main NodeChangeListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;

public class NodeChangeListenerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.NodeChangeListener");
        check(c.isInterface(), "interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0] == EventListener.class, "extends EventListener");

        Method added = c.getMethod("childAdded", NodeChangeEvent.class);
        Method removed = c.getMethod("childRemoved", NodeChangeEvent.class);
        check(Modifier.isAbstract(added.getModifiers()), "childAdded abstract");
        check(Modifier.isAbstract(removed.getModifiers()), "childRemoved abstract");
    }

    // in-memory node to drive node-added/node-removed events without real prefs
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
            if (c == null) { c = new Mem(this, name); c.newNode = true; children.put(name, c); }
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

        final NodeChangeEvent[] addedEvt = new NodeChangeEvent[1];
        final NodeChangeEvent[] removedEvt = new NodeChangeEvent[1];
        final CountDownLatch addedLatch = new CountDownLatch(1);
        final CountDownLatch removedLatch = new CountDownLatch(1);

        NodeChangeListener listener = new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) { addedEvt[0] = evt; addedLatch.countDown(); }
            public void childRemoved(NodeChangeEvent evt) { removedEvt[0] = evt; removedLatch.countDown(); }
        };

        root.addNodeChangeListener(listener);

        Mem child = (Mem) root.node("child");
        check(addedLatch.await(5, TimeUnit.SECONDS), "childAdded delivered");
        check(addedEvt[0].getChild() == child, "childAdded child payload");
        check(addedEvt[0].getParent() == root, "childAdded parent payload");

        child.removeNode();
        check(removedLatch.await(5, TimeUnit.SECONDS), "childRemoved delivered");
        check(removedEvt[0].getChild() == child, "childRemoved child payload");
        check(removedEvt[0].getParent() == root, "childRemoved parent payload");

        root.removeNodeChangeListener(listener);
        check(true, "removeNodeChangeListener");
    }


    // ===================== 契约深化：childAdded/childRemoved 双回调（真实触发，隔离节点） =====================
    static void testContracts() throws Exception {
        java.util.prefs.Preferences n = java.util.prefs.Preferences.userRoot().node("/jck/testNCL1");
        try {
            final int[] added = {0}, removed = {0};
            java.util.prefs.NodeChangeListener l = new java.util.prefs.NodeChangeListener() {
                public void childAdded(java.util.prefs.NodeChangeEvent e) { added[0]++; }
                public void childRemoved(java.util.prefs.NodeChangeEvent e) { removed[0]++; }
            };
            n.addNodeChangeListener(l);
            java.util.prefs.Preferences c = n.node("kid");
            java.util.prefs.Preferences.userRoot().flush();
            for (int i = 0; i < 100 && added[0] == 0; i++) Thread.sleep(20);
            check(added[0] >= 1, "childAdded callback invoked");
            c.removeNode();
            java.util.prefs.Preferences.userRoot().flush();
            for (int i = 0; i < 100 && removed[0] == 0; i++) Thread.sleep(20);
            check(removed[0] >= 1, "childRemoved callback invoked");
            // 移除监听器（已注册）不抛
            n.removeNodeChangeListener(l);
            check(true, "removeNodeChangeListener registered ok");
        } finally {
            try { n.removeNode(); } catch (Exception ignore) { }
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
