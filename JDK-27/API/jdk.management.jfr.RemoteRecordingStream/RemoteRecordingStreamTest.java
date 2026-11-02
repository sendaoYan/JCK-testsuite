/*
 * @test
 * @summary jdk.management.jfr.RemoteRecordingStream conformance: class.
 * @run main RemoteRecordingStreamTest
 */

import java.lang.reflect.*;


public class RemoteRecordingStreamTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.RemoteRecordingStream");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(hasMethod(c, "configurations"), "declares configurations");
        check(hasMethod(c, "onMetadata"), "declares onMetadata");
        check(hasMethod(c, "checkFileAccess"), "declares checkFileAccess");
        check(hasMethod(c, "closeSilently"), "declares closeSilently");
        check(hasMethod(c, "createProxy"), "declares createProxy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
