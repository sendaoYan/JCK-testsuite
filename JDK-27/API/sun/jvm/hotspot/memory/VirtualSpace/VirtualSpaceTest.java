/*
 * @test
 * @summary sun.jvm.hotspot.memory.VirtualSpace conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.memory:+open
 * @run main VirtualSpaceTest
 */
import java.lang.reflect.*;

public class VirtualSpaceTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.memory.VirtualSpace", false, VirtualSpaceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "high"), "method high");
        check(hasMethod(c, "low"), "method low");
        check(hasMethod(c, "highBoundary"), "method highBoundary");
        check(hasMethod(c, "lowBoundary"), "method lowBoundary");
        check(hasMethod(c, "reservedSize"), "method reservedSize");
        check(hasMethod(c, "committedSize"), "method committedSize");
        check(hasMethod(c, "uncommittedSize"), "method uncommittedSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
