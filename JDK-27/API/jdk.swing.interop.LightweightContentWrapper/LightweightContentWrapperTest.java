/*
 * @test
 * @summary jdk.swing.interop.LightweightContentWrapper conformance: abstract class.
 * @run main LightweightContentWrapperTest
 */

import java.lang.reflect.*;


public class LightweightContentWrapperTest {
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
        Class<?> c = Class.forName("jdk.swing.interop.LightweightContentWrapper");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getContent"), "declares getContent");
        check(hasMethod(c, "imageBufferReset"), "declares imageBufferReset");
        check(hasMethod(c, "getComponent"), "declares getComponent");
        check(hasMethod(c, "paintLock"), "declares paintLock");
        check(hasMethod(c, "paintUnlock"), "declares paintUnlock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
