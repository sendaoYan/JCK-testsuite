/*
 * @test
 * @summary java.util.prefs.PreferencesFactory conformance: white-box structure + black-box behavior.
 * @run main PreferencesFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class PreferencesFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.prefs.PreferencesFactory");
        check(c.isInterface(), "interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no superinterfaces");

        Method user = c.getMethod("userRoot");
        Method sys = c.getMethod("systemRoot");
        check(Modifier.isAbstract(user.getModifiers()), "userRoot abstract");
        check(Modifier.isAbstract(sys.getModifiers()), "systemRoot abstract");
        check(user.getReturnType() == Preferences.class, "userRoot returns Preferences");
        check(sys.getReturnType() == Preferences.class, "systemRoot returns Preferences");
    }

    // in-memory Preferences node, so the factory never touches real prefs
    static class Mem extends AbstractPreferences {
        final Map<String, String> store = new TreeMap<>();
        final Map<String, Mem> children = new TreeMap<>();
        final boolean user;
        Mem(AbstractPreferences parent, String name, boolean user) { super(parent, name); this.user = user; }
        @Override public boolean isUserNode() { return user; }
        @Override protected String getSpi(String key) { return store.get(key); }
        @Override protected void putSpi(String key, String value) { store.put(key, value); }
        @Override protected void removeSpi(String key) { store.remove(key); }
        @Override protected String[] keysSpi() { return store.keySet().toArray(new String[0]); }
        @Override protected String[] childrenNamesSpi() { return children.keySet().toArray(new String[0]); }
        @Override protected AbstractPreferences childSpi(String name) {
            Mem c = children.get(name);
            if (c == null) { c = new Mem(this, name, user); children.put(name, c); }
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
        final Preferences injectedUser = new Mem(null, "", true);
        final Preferences injectedSystem = new Mem(null, "", false);

        PreferencesFactory factory = new PreferencesFactory() {
            public Preferences systemRoot() { return injectedSystem; }
            public Preferences userRoot() { return injectedUser; }
        };

        // the factory returns exactly the injected instances
        check(factory.userRoot() == injectedUser, "userRoot returns injected instance");
        check(factory.systemRoot() == injectedSystem, "systemRoot returns injected instance");
        check(factory.userRoot().isUserNode(), "userRoot isUserNode");
        check(!factory.systemRoot().isUserNode(), "systemRoot not isUserNode");
        check(factory.userRoot() != factory.systemRoot(), "distinct trees");

        // the returned nodes are real, usable Preferences
        Preferences node = factory.userRoot().node("app");
        node.put("k", "v");
        check("v".equals(node.get("k", null)), "put/get on factory userRoot");
        check(node.parent() == injectedUser, "node parent is factory root");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
