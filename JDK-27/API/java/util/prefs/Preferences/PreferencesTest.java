/*
 * @test
 * @summary java.util.prefs.Preferences conformance: white-box structure + black-box behavior
 *          via a custom in-memory PreferencesFactory (no real prefs written).
 * @run main PreferencesTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class PreferencesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.Preferences");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "class not interface");
        check(c.getSuperclass() == Object.class, "extends Object");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected no-arg ctor");

        // constants
        Field maxKey = c.getField("MAX_KEY_LENGTH");
        Field maxVal = c.getField("MAX_VALUE_LENGTH");
        Field maxName = c.getField("MAX_NAME_LENGTH");
        check(Modifier.isPublic(maxKey.getModifiers()) && Modifier.isStatic(maxKey.getModifiers())
                && Modifier.isFinal(maxKey.getModifiers()), "MAX_KEY_LENGTH public static final");
        check(maxKey.getInt(null) == 80, "MAX_KEY_LENGTH == 80");
        check(maxVal.getInt(null) == 8192, "MAX_VALUE_LENGTH == 8192");
        check(maxName.getInt(null) == 80, "MAX_NAME_LENGTH == 80");

        // static entry points
        c.getMethod("userRoot");
        c.getMethod("systemRoot");
        c.getMethod("userNodeForPackage", Class.class);
        c.getMethod("systemNodeForPackage", Class.class);
        c.getMethod("importPreferences", java.io.InputStream.class);
        // abstract instance methods
        Method put = c.getMethod("put", String.class, String.class);
        check(Modifier.isAbstract(put.getModifiers()), "put abstract");
        c.getMethod("get", String.class, String.class);
        c.getMethod("keys");
        c.getMethod("childrenNames");
        c.getMethod("node", String.class);
        c.getMethod("nodeExists", String.class);
        c.getMethod("removeNode");
        c.getMethod("flush");
        c.getMethod("sync");
        c.getMethod("addPreferenceChangeListener", java.util.prefs.PreferenceChangeListener.class);
        c.getMethod("addNodeChangeListener", java.util.prefs.NodeChangeListener.class);
        c.getMethod("exportNode", java.io.OutputStream.class);
        c.getMethod("exportSubtree", java.io.OutputStream.class);
        check(true, "abstract API methods present");
    }

    // ---- in-memory backing store ----
    public static class MemNode extends AbstractPreferences {
        final Map<String, String> store = new TreeMap<>();
        final Map<String, MemNode> children = new TreeMap<>();
        final boolean user;
        public MemNode(AbstractPreferences parent, String name, boolean user) {
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
            MemNode c = children.get(name);
            if (c == null) { c = new MemNode(this, name, user); children.put(name, c); }
            return c;
        }
        @Override protected void removeNodeSpi() {
            AbstractPreferences p = (AbstractPreferences) parent();
            if (p instanceof MemNode) ((MemNode) p).children.remove(name());
        }
        @Override protected void flushSpi() {}
        @Override protected void syncSpi() {}
    }

    public static class MemFactory implements PreferencesFactory {
        private final Preferences userRoot = new MemNode(null, "", true);
        private final Preferences systemRoot = new MemNode(null, "", false);
        public Preferences userRoot() { return userRoot; }
        public Preferences systemRoot() { return systemRoot; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Preferences user = Preferences.userRoot();
        Preferences sys = Preferences.systemRoot();

        check(user.isUserNode(), "userRoot isUserNode");
        check(!sys.isUserNode(), "systemRoot not isUserNode");
        check(user != sys, "userRoot != systemRoot");
        check(Preferences.userRoot() == user, "userRoot cached");
        check(Preferences.systemRoot() == sys, "systemRoot cached");

        // constants reachable as API
        check(Preferences.MAX_KEY_LENGTH == 80, "MAX_KEY_LENGTH");
        check(Preferences.MAX_VALUE_LENGTH == 8192, "MAX_VALUE_LENGTH");
        check(Preferences.MAX_NAME_LENGTH == 80, "MAX_NAME_LENGTH");

        // package-based node name maps dots to slashes
        Preferences mapNode = Preferences.userNodeForPackage(java.util.Map.class);
        check("/java/util".equals(mapNode.absolutePath()), "userNodeForPackage absolutePath");
        check(mapNode.isUserNode(), "userNodeForPackage isUserNode");
        Preferences sysMapNode = Preferences.systemNodeForPackage(java.util.Map.class);
        check("/java/util".equals(sysMapNode.absolutePath()), "systemNodeForPackage absolutePath");
        check(!sysMapNode.isUserNode(), "systemNodeForPackage not isUserNode");

        // multi-step round trip on the in-memory user node
        Preferences tmp = user.node("jck_prefs_test");
        tmp.put("key", "value");
        check("value".equals(tmp.get("key", null)), "put/get");
        tmp.putInt("int", 7);
        check(tmp.getInt("int", 0) == 7, "putInt/getInt");
        Preferences child = tmp.node("child");
        check(child.parent() == tmp, "parent");
        check("child".equals(child.name()), "name");
        check("/jck_prefs_test/child".equals(child.absolutePath()), "absolutePath");
        check(tmp.nodeExists("child"), "nodeExists");
        check(tmp.keys().length >= 2, "keys");
        check(tmp.childrenNames().length == 1, "childrenNames");
        tmp.flush();
        tmp.sync();
        check(tmp.toString() != null, "toString");
        check(("User Preference Node: /jck_prefs_test").equals(tmp.toString()), "user toString exact");

        tmp.removeNode();
        check(!user.nodeExists("jck_prefs_test"), "removeNode");
    }


    // ===================== 契约深化：键值契约/键序/长度上限 IAE/node 树/导出导入 round-trip（隔离节点 + finally removeNode） =====================
    static void testContracts() throws Exception {
        java.util.prefs.Preferences n = java.util.prefs.Preferences.userRoot().node("/jck/testPrefs1");
        try {
            // 键值 round-trip + 默认值
            n.clear();
            n.put("k", "v");
            check("v".equals(n.get("k", "d")), "put/get round-trip");
            check("d".equals(n.get("absent", "d")), "get default on absent");
            n.put("k", "v2");
            check("v2".equals(n.get("k", "d")), "put overwrites");
            n.remove("k");
            check("d".equals(n.get("k", "d")), "remove restores default");
            // keys() 有序（TreeMap 语义）
            n.clear();
            n.put("z", "1");
            n.put("a", "2");
            n.put("m", "3");
            check(java.util.Arrays.equals(n.keys(), new String[] {"a", "m", "z"}), "keys() sorted");
            // clear 清空全部
            n.clear();
            check(n.keys().length == 0, "clear empties node");
            // 长度上限（80 / 8192）IAE
            try {
                n.put(new String(new char[81]).replace('\0', 'k'), "v");
                check(false, "key len 81 IAE");
            } catch (IllegalArgumentException e) { check(true, "key len 81 IAE"); }
            check(new String(new char[80]).replace('\0', 'k').length() == 80
                  && n.get(new String(new char[80]).replace('\0', 'k'), "ok").equals("ok"),
                  "key len exactly 80 accepted (only get)");
            try {
                n.put("big", new String(new char[8193]).replace('\0', 'v'));
                check(false, "value len 8193 IAE");
            } catch (IllegalArgumentException e) { check(true, "value len 8193 IAE"); }
            n.put("big", new String(new char[8192]).replace('\0', 'v'));
            check(n.get("big", "").length() == 8192, "value len exactly 8192 accepted");
            // null 契约
            try { n.put(null, "v"); check(false, "null key NPE"); }
            catch (NullPointerException e) { check(true, "null key NPE"); }
            try { n.put("k", null); check(false, "null value NPE"); }
            catch (NullPointerException e) { check(true, "null value NPE"); }
            try { n.get(null, "d"); check(false, "get(null) NPE"); }
            catch (NullPointerException e) { check(true, "get(null) NPE"); }
            // 节点树：parent/name/absolutePath/childrenNames/nodeExists
            n.clear();
            java.util.prefs.Preferences child = n.node("child");
            check("/jck/testPrefs1/child".equals(child.absolutePath()), "child absolutePath");
            check("child".equals(child.name()), "child name");
            check(child.parent() == n, "parent identity");
            check(java.util.Arrays.asList(n.childrenNames()).contains("child"), "childrenNames contains child");
            check(n.nodeExists("child"), "nodeExists relative true");
            check(!n.nodeExists("nope"), "nodeExists relative false");
            check(java.util.prefs.Preferences.userRoot().nodeExists("/jck/testPrefs1"), "nodeExists absolute true");
            check(!java.util.prefs.Preferences.userRoot().nodeExists("/jck/never"), "nodeExists absolute false");
            // 同一路径 node() 返回同一实例
            check(n.node("child") == child, "node() caches same instance");
            // removeNode 后 nodeExists false、操作 ISE
            java.util.prefs.Preferences doomed = n.node("doomed");
            doomed.removeNode();
            check(!n.nodeExists("doomed"), "removed node no longer exists");
            try {
                doomed.put("x", "y");
                check(false, "put after removeNode ISE");
            } catch (IllegalStateException e) { check(true, "put after removeNode ISE"); }
            // exportNode / exportSubtree 产生 XML 声明
            n.clear();
            n.put("exp", "exv");
            java.io.ByteArrayOutputStream nodeXml = new java.io.ByteArrayOutputStream();
            n.exportNode(nodeXml);
            String xml = nodeXml.toString("UTF-8");
            check(xml.startsWith("<?xml"), "exportNode emits XML declaration");
            check(xml.contains("testPrefs1"), "exportNode carries node name");
            check(xml.contains("exp"), "exportNode carries key");
            java.io.ByteArrayOutputStream treeXml = new java.io.ByteArrayOutputStream();
            java.util.prefs.Preferences.userRoot().node("/jck/testPrefs2src");
            java.util.prefs.Preferences.userRoot().node("/jck/testPrefs2src").put("t", "1");
            java.util.prefs.Preferences.userRoot().node("/jck/testPrefs2src").exportSubtree(treeXml);
            check(treeXml.toString("UTF-8").contains("testPrefs2src"), "exportSubtree carries root node name");
            // importPreferences round-trip：导入到干净树后值一致
            n.clear();
            n.put("imp", "imv");
            java.io.ByteArrayOutputStream ex = new java.io.ByteArrayOutputStream();
            n.exportNode(ex);
            n.clear();
            check(n.keys().length == 0, "cleared before import");
            java.util.prefs.Preferences.importPreferences(
                    new java.io.ByteArrayInputStream(ex.toByteArray()));
            check("imv".equals(n.get("imp", "x")), "importPreferences restores values");
            // flush/sync 不抛（文件后端）
            n.flush();
            n.sync();
            check(true, "flush/sync no-throw on user node");
        } finally {
            try { java.util.prefs.Preferences.userRoot().node("/jck/testPrefs1").removeNode(); } catch (Exception ignore) { }
            try { java.util.prefs.Preferences.userRoot().node("/jck/testPrefs2src").removeNode(); } catch (Exception ignore) { }
        }
        // systemRoot 可获取且与 userRoot 不同
        java.util.prefs.Preferences sys = java.util.prefs.Preferences.systemRoot();
        check(sys != java.util.prefs.Preferences.userRoot(), "systemRoot distinct from userRoot");
        check(sys.absolutePath().equals("/"), "root absolutePath is /");
        check("".equals(sys.name()), "root name empty");
    }

    public static void main(String[] args) throws Exception {
        // Isolate the file-backed prefs root to a per-JVM temp dir so that
        // concurrent test JVMs (conc>1) don't contend on the shared
        // ~/.java/.userPrefs lock file (BackingStoreException: Couldn't get file lock).
        String pfr = System.getProperty("java.io.tmpdir") + "/jck-prefs-" + java.util.UUID.randomUUID();
        System.setProperty("java.util.prefs.userRoot", pfr);
        System.setProperty("java.util.prefs.systemRoot", pfr + "-sys");
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        String old = System.getProperty("java.util.prefs.PreferencesFactory");
        System.setProperty("java.util.prefs.PreferencesFactory", "PreferencesTest$MemFactory");
        try {
            testWhiteBox();
            testBlackBox();
        } finally {
            if (old == null) {
                System.clearProperty("java.util.prefs.PreferencesFactory");
            } else {
                System.setProperty("java.util.prefs.PreferencesFactory", old);
            }
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
