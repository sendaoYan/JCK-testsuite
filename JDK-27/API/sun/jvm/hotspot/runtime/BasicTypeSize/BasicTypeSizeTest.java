/*
 * @test
 * @summary sun.jvm.hotspot.runtime.BasicTypeSize conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main BasicTypeSizeTest
 */
import java.lang.reflect.*;

public class BasicTypeSizeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.BasicTypeSize", false, BasicTypeSizeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getTBooleanSize"), "method getTBooleanSize");
        check(hasMethod(c, "getTCharSize"), "method getTCharSize");
        check(hasMethod(c, "getTFloatSize"), "method getTFloatSize");
        check(hasMethod(c, "getTDoubleSize"), "method getTDoubleSize");
        check(hasMethod(c, "getTByteSize"), "method getTByteSize");
        check(hasMethod(c, "getTShortSize"), "method getTShortSize");
        check(hasMethod(c, "getTIntSize"), "method getTIntSize");
        check(hasMethod(c, "getTLongSize"), "method getTLongSize");
        check(hasMethod(c, "getTVoidSize"), "method getTVoidSize");
        check(hasMethod(c, "getTObjectSize"), "method getTObjectSize");
        check(hasMethod(c, "getTArraySize"), "method getTArraySize");
        check(hasMethod(c, "getTNarrowOopSize"), "method getTNarrowOopSize");
        check(hasMethod(c, "getTNarrowKlassSize"), "method getTNarrowKlassSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
