/*
 * @test
 * @summary sun.jvm.hotspot.utilities.AddressOps conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main AddressOpsTest
 */
import java.lang.reflect.*;

public class AddressOpsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.AddressOps", false, AddressOpsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "min"), "method min");
        check(hasMethod(c, "max"), "method max");
        check(hasMethod(c, "equal"), "method equal");
        check(hasMethod(c, "lt"), "method lt");
        check(hasMethod(c, "lessThan"), "method lessThan");
        check(hasMethod(c, "lessThanOrEqual"), "method lessThanOrEqual");
        check(hasMethod(c, "greaterThan"), "method greaterThan");
        check(hasMethod(c, "greaterThanOrEqual"), "method greaterThanOrEqual");
        check(hasMethod(c, "gt"), "method gt");
        check(hasMethod(c, "lte"), "method lte");
        check(hasMethod(c, "gte"), "method gte");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
