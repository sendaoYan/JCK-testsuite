/*
 * @test
 * @summary sun.jvm.hotspot.memory.FileMapInfo.FileMapHeader conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.memory:+open
 * @run main FileMapHeaderTest
 */
import java.lang.reflect.*;

public class FileMapHeaderTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.memory.FileMapInfo$FileMapHeader", false, FileMapHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "inCopiedVtableSpace"), "method inCopiedVtableSpace");
        check(hasMethod(c, "createVtableTypeMapping"), "method createVtableTypeMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
