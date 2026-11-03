/*
 * @test
 * @summary javax.swing.plaf.basic.DefaultMenuLayout conformance: white-box structure + black-box behavior.
 * @run main DefaultMenuLayoutTest
 */
import java.lang.reflect.Modifier;

public class DefaultMenuLayoutTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.DefaultMenuLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.BoxLayout"), "superclass javax.swing.BoxLayout");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");

        check(hasDeclaredMethod(c, "preferredLayoutSize"), "declares method preferredLayoutSize");
    }

    static void testBlackBox() throws Exception {
        java.awt.Container target = new java.awt.Container();
        javax.swing.plaf.basic.DefaultMenuLayout l = new javax.swing.plaf.basic.DefaultMenuLayout(target, javax.swing.BoxLayout.X_AXIS);
        check(l instanceof javax.swing.plaf.UIResource, "UIResource marker");
        check(l instanceof java.awt.LayoutManager2, "LayoutManager2");
        check(l.preferredLayoutSize(target) != null, "preferred layout size non-null");
        check(l.minimumLayoutSize(target) != null, "minimum layout size non-null");
    }

}
