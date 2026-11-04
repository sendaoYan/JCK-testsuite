/*
 * @test
 * @summary javax.swing.ComponentInputMap conformance: white-box structure + black-box behavior.
 * @run main ComponentInputMapTest
 */

import java.lang.reflect.Modifier;

import javax.swing.ComponentInputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class ComponentInputMapTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ComponentInputMap");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.InputMap"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JComponent comp = new JPanel();
        ComponentInputMap map = new ComponentInputMap(comp);
        check(map.getComponent() == comp, "getComponent");

        KeyStroke ks = KeyStroke.getKeyStroke("A");
        map.put(ks, "action");
        check(map.size() == 1, "size");
        check("action".equals(map.get(ks)), "get");
        check(map.keys().length == 1, "keys");

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
