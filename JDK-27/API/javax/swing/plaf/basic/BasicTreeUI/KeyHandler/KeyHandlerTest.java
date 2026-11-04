/*
 * @test
 * @summary javax.swing.plaf.basic.BasicTreeUI.KeyHandler conformance: white-box structure + black-box behavior.
 * @run main KeyHandlerTest
 */
import java.lang.reflect.Modifier;

public class KeyHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicTreeUI$KeyHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.KeyAdapter"), "superclass java.awt.event.KeyAdapter");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "keyTyped"), "declares method keyTyped");
        check(hasDeclaredMethod(c, "keyPressed"), "declares method keyPressed");
        check(hasDeclaredMethod(c, "keyReleased"), "declares method keyReleased");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicTreeUI o = new javax.swing.plaf.basic.BasicTreeUI();
        javax.swing.plaf.basic.BasicTreeUI.KeyHandler h = o.new KeyHandler();
        check(h instanceof java.awt.event.KeyAdapter, "extends KeyAdapter");
    }
}
