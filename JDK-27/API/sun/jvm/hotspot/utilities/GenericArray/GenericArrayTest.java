/*
 * @test
 * @summary sun.jvm.hotspot.utilities.GenericArray conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main GenericArrayTest
 */
import java.lang.reflect.*;

public class GenericArrayTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.GenericArray", false, GenericArrayTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "length"), "method length");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getAddressAt"), "method getAddressAt");
        check(hasMethod(c, "getDataStart"), "method getDataStart");
        check(hasMethod(c, "getIntegerAt"), "method getIntegerAt");
        check(hasMethod(c, "getElemType"), "method getElemType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
