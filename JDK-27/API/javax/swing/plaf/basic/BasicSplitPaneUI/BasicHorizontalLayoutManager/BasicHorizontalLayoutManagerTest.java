/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSplitPaneUI.BasicHorizontalLayoutManager conformance: white-box structure + black-box behavior.
 * @run main BasicHorizontalLayoutManagerTest
 */
import java.lang.reflect.Modifier;

public class BasicHorizontalLayoutManagerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneUI$BasicHorizontalLayoutManager");
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
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager2"), "interface 0");

        check(hasDeclaredMethod(c, "layoutContainer"), "declares method layoutContainer");
        check(hasDeclaredMethod(c, "preferredLayoutSize"), "declares method preferredLayoutSize");
        check(hasDeclaredMethod(c, "minimumLayoutSize"), "declares method minimumLayoutSize");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneUI$BasicHorizontalLayoutManager");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length >= 1, "has constructors");
        for (java.lang.reflect.Constructor<?> ctor : ctors) {
            check(!Modifier.isPublic(ctor.getModifiers()), "constructor not public");
        }
    }

}
