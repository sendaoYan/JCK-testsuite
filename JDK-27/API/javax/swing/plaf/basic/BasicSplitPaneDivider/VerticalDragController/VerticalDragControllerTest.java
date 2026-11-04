/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSplitPaneDivider.VerticalDragController conformance: white-box structure + black-box behavior.
 * @run main VerticalDragControllerTest
 */
import java.lang.reflect.Modifier;

public class VerticalDragControllerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneDivider$VerticalDragController");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicSplitPaneDivider$DragController"), "superclass javax.swing.plaf.basic.BasicSplitPaneDivider$DragController");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "getNeededLocation"), "declares method getNeededLocation");
        check(hasDeclaredMethod(c, "positionForMouseEvent"), "declares method positionForMouseEvent");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneDivider$VerticalDragController");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length >= 1, "has constructors");
        for (java.lang.reflect.Constructor<?> ctor : ctors) {
            check(!Modifier.isPublic(ctor.getModifiers()), "constructor not public");
        }
    }

}
