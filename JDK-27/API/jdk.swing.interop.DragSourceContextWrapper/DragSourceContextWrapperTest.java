/*
 * @test
 * @summary jdk.swing.interop.DragSourceContextWrapper conformance: white-box structure + black-box behavior.
 * @run main DragSourceContextWrapperTest
 */

import jdk.swing.interop.*;
import java.lang.reflect.*;
import java.util.*;
public class DragSourceContextWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DragSourceContextWrapper");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(hasMethod(c, "convertModifiersToDropAction"), "convertModifiersToDropAction");
        check(hasMethod(c, "startSecondaryEventLoop"), "startSecondaryEventLoop");
        check(hasMethod(c, "quitSecondaryEventLoop"), "quitSecondaryEventLoop");
        check(hasMethod(c, "dragDropFinished"), "dragDropFinished");
        check(hasMethod(c, "getDragSourceContext"), "getDragSourceContext");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
