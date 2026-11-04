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

    public static void main(String[] args) throws Exception {
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
