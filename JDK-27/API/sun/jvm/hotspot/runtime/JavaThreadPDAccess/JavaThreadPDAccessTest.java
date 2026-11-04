/*
 * @test
 * @summary sun.jvm.hotspot.runtime.JavaThreadPDAccess conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main JavaThreadPDAccessTest
 */
import java.lang.reflect.*;

public class JavaThreadPDAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.JavaThreadPDAccess", false, JavaThreadPDAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCurrentFrameGuess"), "method getCurrentFrameGuess");
        check(hasMethod(c, "newRegisterMap"), "method newRegisterMap");
        check(hasMethod(c, "printInfoOn"), "method printInfoOn");
        check(hasMethod(c, "getLastJavaFP"), "method getLastJavaFP");
        check(hasMethod(c, "getBaseOfStackPointer"), "method getBaseOfStackPointer");
        check(hasMethod(c, "getLastFramePD"), "method getLastFramePD");
        check(hasMethod(c, "printThreadIDOn"), "method printThreadIDOn");
        check(hasMethod(c, "getThreadProxy"), "method getThreadProxy");
        check(hasMethod(c, "getLastJavaPC"), "method getLastJavaPC");
        check(hasMethod(c, "getLastSP"), "method getLastSP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
