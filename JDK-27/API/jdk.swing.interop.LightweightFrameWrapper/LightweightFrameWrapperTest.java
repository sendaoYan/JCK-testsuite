/*
 * @test
 * @summary jdk.swing.interop.LightweightFrameWrapper conformance: class.
 * @run main LightweightFrameWrapperTest
 */

import java.lang.reflect.*;


public class LightweightFrameWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.LightweightFrameWrapper");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(hasMethod(c, "getLightweightFrame"), "declares getLightweightFrame");
        check(hasMethod(c, "notifyDisplayChanged"), "declares notifyDisplayChanged");
        check(hasMethod(c, "overrideNativeWindowHandle"), "declares overrideNativeWindowHandle");
        check(hasMethod(c, "setHostBounds"), "declares setHostBounds");
        check(hasMethod(c, "dispose"), "declares dispose");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
