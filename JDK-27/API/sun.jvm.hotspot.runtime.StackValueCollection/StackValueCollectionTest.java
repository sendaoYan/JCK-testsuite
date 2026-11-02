/*
 * @test
 * @summary sun.jvm.hotspot.runtime.StackValueCollection conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main StackValueCollectionTest
 */
import java.lang.reflect.*;

public class StackValueCollectionTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.StackValueCollection", false, StackValueCollectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "charAt"), "method charAt");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "intAt"), "method intAt");
        check(hasMethod(c, "booleanAt"), "method booleanAt");
        check(hasMethod(c, "byteAt"), "method byteAt");
        check(hasMethod(c, "shortAt"), "method shortAt");
        check(hasMethod(c, "oopHandleAt"), "method oopHandleAt");
        check(hasMethod(c, "floatAt"), "method floatAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
