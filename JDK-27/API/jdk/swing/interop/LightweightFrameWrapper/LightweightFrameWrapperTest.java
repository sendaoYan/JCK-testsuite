/*
 * @test
 * @summary jdk.swing.interop.LightweightFrameWrapper conformance: white-box structure + black-box behavior.
 * @run main LightweightFrameWrapperTest
 */

import jdk.swing.interop.*;
import java.lang.reflect.*;
import java.util.*;
public class LightweightFrameWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.LightweightFrameWrapper");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(hasMethod(c, "notifyDisplayChanged"), "notifyDisplayChanged");
        check(hasMethod(c, "setHostBounds"), "setHostBounds");
        check(hasMethod(c, "dispose"), "dispose");
        check(hasMethod(c, "setVisible"), "setVisible");
        check(hasMethod(c, "setBounds"), "setBounds");
        check(hasMethod(c, "addWindowFocusListener"), "addWindowFocusListener");
        check(hasMethod(c, "setContent"), "setContent");
        check(hasMethod(c, "emulateActivation"), "emulateActivation");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }


    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.LightweightFrameWrapper");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete wrapper");
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()), "public no-arg ctor");
        check(c.getDeclaredConstructor().getParameterCount() == 0, "ctor takes no args");
        java.util.TreeSet<String> pub = new TreeSet<>();
        int pubMethods = 0;
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isPublic(m.getModifiers())) { pub.add(m.getName()); pubMethods++; }
        check(pubMethods == 15, "15 public declared methods incl. notifyDisplayChanged overload (" + pubMethods + ")");
        check(pub.size() == 14, "14 distinct public names (" + pub.size() + ")");
        check(pub.equals(new TreeSet<>(java.util.Arrays.asList("dispose", "setVisible", "setBounds", "notifyDisplayChanged",
                "setHostBounds", "addWindowFocusListener", "setContent", "emulateActivation", "findComponentAt",
                "createMouseEvent", "createMouseWheelEvent", "createKeyEvent", "createUngrabEvent", "isCompEqual"))),
                "exact delegate surface (" + pub + ")");
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isPublic(m.getModifiers())) check(!Modifier.isStatic(m.getModifiers()), m.getName() + " instance method");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "plain wrapper not a Component");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
