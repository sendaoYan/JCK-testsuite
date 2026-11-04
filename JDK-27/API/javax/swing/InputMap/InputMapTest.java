/*
 * @test
 * @summary javax.swing.InputMap conformance: white-box structure + black-box behavior.
 * @run main InputMapTest
 */

import java.lang.reflect.Modifier;

import javax.swing.InputMap;
import javax.swing.KeyStroke;

public class InputMapTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.InputMap");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        InputMap map = new InputMap();
        KeyStroke ks = KeyStroke.getKeyStroke("A");
        check(map.size() == 0, "empty size");
        map.put(ks, "action");
        check(map.size() == 1, "size after put");
        check("action".equals(map.get(ks)), "get");
        check(map.get(KeyStroke.getKeyStroke("B")) == null, "get missing null");
        check(map.keys().length == 1, "keys");
        check(map.allKeys().length == 1, "allKeys");

        InputMap parent = new InputMap();
        KeyStroke ks2 = KeyStroke.getKeyStroke("B");
        parent.put(ks2, "parentAction");
        map.setParent(parent);
        check(map.getParent() == parent, "getParent");
        check("parentAction".equals(map.get(ks2)), "resolve through parent");
        check(map.allKeys().length == 2, "allKeys includes parent");

        map.remove(ks);
        check(map.get(ks) == null, "removed");
        map.clear();
        check(map.size() == 0, "cleared");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
