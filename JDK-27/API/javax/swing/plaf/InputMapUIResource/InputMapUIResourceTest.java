/*
 * @test
 * @summary javax.swing.plaf.InputMapUIResource conformance: white-box structure + black-box behavior.
 * @run main InputMapUIResourceTest
 */

import java.lang.reflect.Modifier;

public class InputMapUIResourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.InputMapUIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.InputMap"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.InputMapUIResource m = new javax.swing.plaf.InputMapUIResource();
        check(m instanceof javax.swing.plaf.UIResource, "is UIResource");
        javax.swing.KeyStroke ks = javax.swing.KeyStroke.getKeyStroke('X');
        m.put(ks, "cmd");
        check("cmd".equals(m.get(ks)), "put/get");
        check(m.size() == 1, "size 1");
        m.remove(ks);
        check(m.get(ks) == null, "remove");
    }
}
