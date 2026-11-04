/*
 * @test
 * @summary java.util.prefs.AbstractPreferences conformance: in-memory node/put/get/keys/remove.
 * @run main AbstractPreferencesTest
 */

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class AbstractPreferencesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MemoryPrefs extends AbstractPreferences {
        private final Map<String, String> store = new HashMap<>();
        private final Map<String, MemoryPrefs> children = new HashMap<>();

        MemoryPrefs(AbstractPreferences parent, String name) {
            super(parent, name);
        }

        protected String getSpi(String key) {
            return store.get(key);
        }

        protected void putSpi(String key, String value) {
            store.put(key, value);
        }

        protected void removeSpi(String key) {
            store.remove(key);
        }

        protected String[] keysSpi() {
            return store.keySet().toArray(new String[0]);
        }

        protected String[] childrenNamesSpi() {
            return children.keySet().toArray(new String[0]);
        }

        protected AbstractPreferences childSpi(String name) {
            MemoryPrefs c = children.get(name);
            if (c == null) {
                c = new MemoryPrefs(this, name);
                children.put(name, c);
            }
            return c;
        }

        protected void flushSpi() {
        }

        protected void syncSpi() {
        }

        protected void removeNodeSpi() {
            AbstractPreferences p = (AbstractPreferences) parent();
            if (p instanceof MemoryPrefs) {
                ((MemoryPrefs) p).children.remove(name());
            }
        }
    }

    static void testAbstractPreferences() throws Exception {
        MemoryPrefs root = new MemoryPrefs(null, "");
        check("/".equals(root.absolutePath()), "root absolutePath");
        root.put("key", "value");
        check("value".equals(root.get("key", null)), "put/get");
        check("def".equals(root.get("missing", "def")), "get default");
        root.putInt("i", 42);
        check(root.getInt("i", 0) == 42, "putInt/getInt");
        root.putBoolean("b", true);
        check(root.getBoolean("b", false), "putBoolean/getBoolean");
        check(root.keys().length == 3, "keys");

        AbstractPreferences child = (AbstractPreferences) root.node("child");
        check("child".equals(child.name()), "node name");
        check("/child".equals(child.absolutePath()), "absolutePath");
        check(child.parent() == root, "parent");
        check(root.nodeExists("child"), "nodeExists");
        check(root.node("child/grand") != null, "nested node");
        check(root.childrenNames().length == 1, "childrenNames");

        root.addPreferenceChangeListener(new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent evt) {
            }
        });
        root.addNodeChangeListener(new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) {
            }

            public void childRemoved(NodeChangeEvent evt) {
            }
        });

        root.remove("key");
        check(root.get("key", null) == null, "remove");
        root.flush();
        root.sync();
        check(root.toString() != null, "toString");
        root.clear();
        check(root.keys().length == 0, "clear");

        root.node("child").removeNode();
        check(!root.nodeExists("child"), "removeNode");
    }

    public static void main(String[] args) throws Exception {
        testAbstractPreferences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
