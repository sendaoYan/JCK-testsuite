/*
 * @test
 * @summary sun.jvm.hotspot.runtime.JavaThreadState conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main JavaThreadStateTest
 */
import java.lang.reflect.*;

public class JavaThreadStateTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.JavaThreadState", false, JavaThreadStateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "UNINITIALIZED"), "field UNINITIALIZED");
        check(hasField(c, "NEW"), "field NEW");
        check(hasField(c, "NEW_TRANS"), "field NEW_TRANS");
        check(hasField(c, "IN_NATIVE"), "field IN_NATIVE");
        check(hasField(c, "IN_NATIVE_TRANS"), "field IN_NATIVE_TRANS");
        check(hasField(c, "IN_VM"), "field IN_VM");
        check(hasField(c, "IN_VM_TRANS"), "field IN_VM_TRANS");
        check(hasField(c, "IN_JAVA"), "field IN_JAVA");
        check(hasField(c, "IN_JAVA_TRANS"), "field IN_JAVA_TRANS");
        check(hasField(c, "BLOCKED"), "field BLOCKED");
        check(hasField(c, "BLOCKED_TRANS"), "field BLOCKED_TRANS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
