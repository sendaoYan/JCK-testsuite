/*
 * @test
 * @summary javax.swing.ActionMap conformance: white-box structure + black-box behavior.
 * @run main ActionMapTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;

public class ActionMapTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ActionMap");
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
        Action a1 = new AbstractAction("a1") { public void actionPerformed(ActionEvent e) {} };
        Action a2 = new AbstractAction("a2") { public void actionPerformed(ActionEvent e) {} };
        Action a3 = new AbstractAction("a3") { public void actionPerformed(ActionEvent e) {} };

        ActionMap map = new ActionMap();
        check(map.size() == 0, "empty size");
        map.put("one", a1);
        map.put("two", a2);
        check(map.size() == 2, "size after put");
        check(map.get("one") == a1, "get by key");
        check(map.get("missing") == null, "get missing null");
        check(map.keys().length == 2, "keys length");
        check(map.allKeys().length == 2, "allKeys length");

        // parent chain resolution
        ActionMap parent = new ActionMap();
        parent.put("three", a3);
        map.setParent(parent);
        check(map.getParent() == parent, "getParent");
        check(map.get("three") == a3, "get resolves through parent");
        check(map.allKeys().length == 3, "allKeys includes parent");

        // remove + clear
        map.remove("one");
        check(map.size() == 1, "size after remove");
        check(map.get("one") == null, "removed key gone");
        map.clear();
        check(map.size() == 0, "size after clear");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
