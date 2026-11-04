/*
 * @test
 * @summary java.util.prefs.AbstractPreferences conformance: white-box structure + black-box behavior.
 * @run main AbstractPreferencesTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class AbstractPreferencesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static <T extends Throwable> T expectThrows(Class<T> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                passed++;
                return type.cast(t);
            }
            throw new AssertionError(msg + " (got " + t + ")", t);
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.AbstractPreferences");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "class not interface");
        check(c.getSuperclass() == Preferences.class, "extends Preferences");

        Constructor<?> ctor = c.getDeclaredConstructor(AbstractPreferences.class, String.class);
        check(Modifier.isProtected(ctor.getModifiers()), "protected (parent, name) ctor");

        // the nine protected abstract SPI methods
        String[][] spi = {
            {"putSpi", "java.lang.String", "java.lang.String"},
            {"getSpi", "java.lang.String"},
            {"removeSpi", "java.lang.String"},
            {"removeNodeSpi"},
            {"keysSpi"},
            {"childrenNamesSpi"},
            {"childSpi", "java.lang.String"},
            {"syncSpi"},
            {"flushSpi"},
        };
        for (String[] m : spi) {
            Class<?>[] params = new Class<?>[m.length - 1];
            for (int i = 1; i < m.length; i++) {
                params[i - 1] = (m[i].equals("java.lang.String")) ? String.class : null;
                if (params[i - 1] == null) throw new AssertionError("unmapped param " + m[i]);
            }
            Method meth = c.getDeclaredMethod(m[0], params);
            check(Modifier.isProtected(meth.getModifiers()), "protected " + m[0]);
            check(Modifier.isAbstract(meth.getModifiers()), "abstract " + m[0]);
        }

        // representative public API present
        c.getMethod("put", String.class, String.class);
        c.getMethod("get", String.class, String.class);
        c.getMethod("remove", String.class);
        c.getMethod("clear");
        c.getMethod("node", String.class);
        c.getMethod("nodeExists", String.class);
        c.getMethod("removeNode");
        c.getMethod("keys");
        c.getMethod("childrenNames");
        c.getMethod("parent");
        c.getMethod("absolutePath");
        c.getMethod("isUserNode");
        c.getMethod("toString");
        c.getMethod("flush");
        c.getMethod("sync");
        c.getMethod("addPreferenceChangeListener", PreferenceChangeListener.class);
        c.getMethod("addNodeChangeListener", NodeChangeListener.class);
        check(true, "public API methods present");
    }

    // In-memory AbstractPreferences backed by TreeMaps; no real prefs touched.
    static class Mem extends AbstractPreferences {
        final Map<String, String> store = new TreeMap<>();
        final Map<String, Mem> children = new TreeMap<>();
        final boolean user;

        Mem(AbstractPreferences parent, String name, boolean user) {
            super(parent, name);
            this.user = user;
        }

        @Override public boolean isUserNode() { return user; }
        @Override protected String getSpi(String key) { return store.get(key); }
        @Override protected void putSpi(String key, String value) { store.put(key, value); }
        @Override protected void removeSpi(String key) { store.remove(key); }
        @Override protected String[] keysSpi() { return store.keySet().toArray(new String[0]); }
        @Override protected String[] childrenNamesSpi() { return children.keySet().toArray(new String[0]); }
        @Override protected AbstractPreferences childSpi(String name) {
            Mem c = children.get(name);
            if (c == null) { c = new Mem(this, name, user); c.newNode = true; children.put(name, c); }
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
        testConstructorAndIdentity();
        testPutGetScalars();
        testKeysClearRemove();
        testNodeTree();
        testExceptions();
        testEventListeners();
    }

    static void testConstructorAndIdentity() {
        Mem root = new Mem(null, "", true);
        check("/".equals(root.absolutePath()), "root absolutePath");
        check("".equals(root.name()), "root name");
        check(root.parent() == null, "root parent null");
        check(root.isUserNode(), "user node true");
        check("/".equals(root.toString().split(" ")[3]), "root toString ends with /");
        check(("User Preference Node: /").equals(root.toString()), "root toString exact");

        Mem sys = new Mem(null, "", false);
        check(!sys.isUserNode(), "system node false");
        check(("System Preference Node: /").equals(sys.toString()), "sys toString exact");
    }

    static void testPutGetScalars() {
        Mem root = new Mem(null, "", true);

        root.put("key", "value");
        check("value".equals(root.get("key", null)), "put/get");
        check("def".equals(root.get("missing", "def")), "get default");
        check(root.get("missing", null) == null, "get default null");

        root.putInt("i", 42);
        check(root.getInt("i", 0) == 42, "putInt/getInt");
        check(root.getInt("missing", 7) == 7, "getInt default");

        root.putLong("l", 123456789012345L);
        check(root.getLong("l", 0L) == 123456789012345L, "putLong/getLong");

        root.putBoolean("b", true);
        check(root.getBoolean("b", false), "putBoolean/getBoolean true");
        check(root.getBoolean("missing", true), "getBoolean default");

        root.putFloat("f", 1.5f);
        check(root.getFloat("f", 0f) == 1.5f, "putFloat/getFloat");

        root.putDouble("d", 2.25d);
        check(root.getDouble("d", 0d) == 2.25d, "putDouble/getDouble");

        byte[] arr = {1, 2, 3, -1};
        root.putByteArray("bytes", arr);
        check(Arrays.equals(arr, root.getByteArray("bytes", null)), "putByteArray/getByteArray");
        byte[] dflt = {9};
        check(root.getByteArray("missing", dflt) == dflt, "getByteArray default ref");

        // getInt/getBoolean fall back to default when the stored value is unparseable
        root.put("bad", "not-a-number");
        check(root.getInt("bad", 99) == 99, "getInt unparseable -> default");
        root.put("flag", "YES");
        check(root.getBoolean("flag", true), "getBoolean unparseable -> default");
    }

    static void testKeysClearRemove() throws BackingStoreException {
        Mem root = new Mem(null, "", true);
        root.put("b", "2");
        root.put("a", "1");
        root.put("c", "3");
        String[] ks = root.keys();
        check(Arrays.equals(new String[]{"a", "b", "c"}, ks), "keys sorted");

        root.remove("b");
        check(root.get("b", null) == null, "remove clears value");
        check(Arrays.equals(new String[]{"a", "c"}, root.keys()), "keys after remove");

        root.clear();
        check(root.keys().length == 0, "clear empties keys");
    }

    static void testNodeTree() throws BackingStoreException {
        Mem root = new Mem(null, "", true);

        // empty / slash / absolute path handling
        check(root.node("") == root, "node('') returns this");
        check(root.node("/") == root, "node('/') returns root");

        Mem child = (Mem) root.node("child");
        check("child".equals(child.name()), "node name");
        check("/child".equals(child.absolutePath()), "child absolutePath");
        check(child.parent() == root, "child parent");
        check(child.isUserNode(), "child inherits user flag");

        check(root.node("child") == child, "node caches same child instance");
        check(root.nodeExists("child"), "nodeExists child");
        check(root.nodeExists("child/missing") == false, "nodeExists missing false");
        check(root.nodeExists("no-such") == false, "nodeExists no-such false");

        Mem grand = (Mem) root.node("child/grand");
        check("/child/grand".equals(grand.absolutePath()), "nested absolutePath");
        check(grand.parent() == child, "grand parent");
        check(root.node("/child/grand") == grand, "absolute path node lookup");

        check(Arrays.equals(new String[]{"child"}, root.childrenNames()), "childrenNames");
        check(child.childrenNames().length == 1, "child has one child");

        root.flush();
        root.sync();
        check(true, "flush/sync no-op on in-memory node");

        // remove a whole subtree
        child.removeNode();
        check(!root.nodeExists("child"), "removeNode removes subtree");
        check(root.childrenNames().length == 0, "no children after removeNode");
        expectThrows(IllegalStateException.class, () -> child.put("k", "v"), "put on removed node");
    }

    static void testExceptions() {
        Mem root = new Mem(null, "", true);

        // constructor validation
        expectThrows(IllegalArgumentException.class, () -> new Mem(null, "x", true), "root name must be empty");
        expectThrows(IllegalArgumentException.class, () -> new Mem(root, "", true), "empty non-root name");
        expectThrows(IllegalArgumentException.class, () -> new Mem(root, "a/b", true), "name contains slash");

        // key/value validation
        expectThrows(NullPointerException.class, () -> root.put(null, "v"), "put null key");
        expectThrows(NullPointerException.class, () -> root.put("k", null), "put null value");
        expectThrows(NullPointerException.class, () -> root.get(null, "d"), "get null key");
        expectThrows(NullPointerException.class, () -> root.remove(null), "remove null key");
        expectThrows(IllegalArgumentException.class,
                () -> root.put(repeat('k', 81), "v"), "key too long");
        expectThrows(IllegalArgumentException.class,
                () -> root.put("k", repeat('v', 8193)), "value too long");

        // path validation
        expectThrows(IllegalArgumentException.class, () -> root.node("a//b"), "consecutive slashes");
        expectThrows(IllegalArgumentException.class, () -> root.node("a/"), "path ends with slash");
        expectThrows(IllegalArgumentException.class, () -> root.node(repeat('n', 81)), "node name too long");
        expectThrows(IllegalArgumentException.class, () -> root.nodeExists("a//b"), "nodeExists consecutive slashes");
    }

    static void testEventListeners() throws Exception {
        Mem root = new Mem(null, "", true);

        // --- preference change events ---
        final List<PreferenceChangeEvent> pce = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch pceLatch = new CountDownLatch(1);
        PreferenceChangeListener pcl = evt -> { pce.add(evt); pceLatch.countDown(); };
        root.addPreferenceChangeListener(pcl);

        root.put("ekey", "evalue");
        check(pceLatch.await(5, TimeUnit.SECONDS), "pref change delivered");
        check(pce.size() == 1, "one pref change event");
        check(pce.get(0).getNode() == root, "event node == root");
        check("ekey".equals(pce.get(0).getKey()), "event key");
        check("evalue".equals(pce.get(0).getNewValue()), "event newValue");

        final CountDownLatch pceLatch2 = new CountDownLatch(1);
        root.remove("ekey");
        // remove() enqueues a change event with newValue == null; deliver into same list
        root.addPreferenceChangeListener(evt -> pceLatch2.countDown());
        // trigger a third event to flush; assert the earlier removal produced a null-value event
        root.put("ekey2", "v2");
        check(pceLatch2.await(5, TimeUnit.SECONDS), "second listener delivered");
        // find the remove event among recorded events
        PreferenceChangeEvent removal = null;
        for (PreferenceChangeEvent e : pce) {
            if ("ekey".equals(e.getKey()) && e.getNewValue() == null) removal = e;
        }
        check(removal != null, "remove event has newValue == null");

        // listener removal: removed listener stops receiving events
        final List<PreferenceChangeEvent> stale = Collections.synchronizedList(new ArrayList<>());
        PreferenceChangeListener staleL = stale::add;
        root.addPreferenceChangeListener(staleL);
        root.removePreferenceChangeListener(staleL);
        final CountDownLatch liveLatch = new CountDownLatch(1);
        PreferenceChangeListener liveL = evt -> liveLatch.countDown();
        root.addPreferenceChangeListener(liveL);
        root.put("ekey3", "v3");
        check(liveLatch.await(5, TimeUnit.SECONDS), "live listener delivered");
        check(stale.isEmpty(), "removed listener not delivered");

        // --- node change events ---
        final List<NodeChangeEvent> added = Collections.synchronizedList(new ArrayList<>());
        final List<NodeChangeEvent> removed = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch addedLatch = new CountDownLatch(1);
        final CountDownLatch removedLatch = new CountDownLatch(1);
        NodeChangeListener ncl = new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) { added.add(evt); addedLatch.countDown(); }
            public void childRemoved(NodeChangeEvent evt) { removed.add(evt); removedLatch.countDown(); }
        };
        root.addNodeChangeListener(ncl);

        Mem nchild = (Mem) root.node("nchild");
        check(addedLatch.await(5, TimeUnit.SECONDS), "node added delivered");
        check(added.size() == 1, "one node added event");
        check(added.get(0).getParent() == root, "added parent == root");
        check(added.get(0).getChild() == nchild, "added child == nchild");

        nchild.removeNode();
        check(removedLatch.await(5, TimeUnit.SECONDS), "node removed delivered");
        check(removed.size() == 1, "one node removed event");
        check(removed.get(0).getParent() == root, "removed parent == root");
        check(removed.get(0).getChild() == nchild, "removed child == nchild");

        root.removeNodeChangeListener(ncl);
        check(true, "removeNodeChangeListener returns");
    }


    // ===================== 契约深化：removeNode 级联删除子树（隔离节点 + finally） =====================
    static void testContracts() throws Exception {
        java.util.prefs.Preferences root = java.util.prefs.Preferences.userRoot().node("/jck/testAbs1");
        try {
            java.util.prefs.Preferences a = root.node("a");
            java.util.prefs.Preferences ab = a.node("b");
            ab.put("deep", "1");
            a.put("mid", "2");
            root.put("top", "3");
            // removeNode(a) 级联：b 一并消失
            a.removeNode();
            check(!root.nodeExists("a"), "a removed");
            check(!root.nodeExists("a/b"), "descendant removed with ancestor");
            check("3".equals(root.get("top", "x")), "sibling keys unaffected");
            // 重建同名节点是全新节点
            java.util.prefs.Preferences a2 = root.node("a");
            check(a2.keys().length == 0, "recreated node is empty");
            // clear 只影响本节点
            root.clear();
            check(root.keys().length == 0, "clear local only");
        } finally {
            try { root.removeNode(); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
