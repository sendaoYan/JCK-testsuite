/*
 * @test
 * @summary javax.swing.plaf.basic.BasicTableUI.MouseInputHandler conformance: white-box structure + black-box behavior.
 * @run main MouseInputHandlerTest
 */
import java.lang.reflect.Modifier;

public class MouseInputHandlerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicTableUI$MouseInputHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.MouseInputListener"), "interface 0");

        check(hasDeclaredMethod(c, "mouseClicked"), "declares method mouseClicked");
        check(hasDeclaredMethod(c, "mousePressed"), "declares method mousePressed");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicTableUI o = new javax.swing.plaf.basic.BasicTableUI();
        javax.swing.plaf.basic.BasicTableUI.MouseInputHandler h = o.new MouseInputHandler();
        check(h instanceof javax.swing.event.MouseInputListener, "implements MouseInputListener");
    }
}
