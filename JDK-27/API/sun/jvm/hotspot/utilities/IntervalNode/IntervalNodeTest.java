/*
 * @test
 * @summary sun.jvm.hotspot.utilities.IntervalNode conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main IntervalNodeTest
 */
import java.lang.reflect.*;

public class IntervalNodeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.IntervalNode", false, IntervalNodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.utilities.RBNode".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "update"), "method update");
        check(hasMethod(c, "copyFrom"), "method copyFrom");
        check(hasMethod(c, "getInterval"), "method getInterval");
        check(hasMethod(c, "getMaxEndpoint"), "method getMaxEndpoint");
        check(hasMethod(c, "computeMaxEndpoint"), "method computeMaxEndpoint");
        check(hasMethod(c, "getMinEndpoint"), "method getMinEndpoint");
        check(hasMethod(c, "computeMinEndpoint"), "method computeMinEndpoint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
