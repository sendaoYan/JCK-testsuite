/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicIndexableFieldIdentifier conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicIndexableFieldIdentifierTest
 */
import java.lang.reflect.*;

public class BasicIndexableFieldIdentifierTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicIndexableFieldIdentifier", false, BasicIndexableFieldIdentifierTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.IndexableFieldIdentifier"), "interface sun.jvm.hotspot.debugger.cdbg.IndexableFieldIdentifier");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getIndex"), "method getIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
