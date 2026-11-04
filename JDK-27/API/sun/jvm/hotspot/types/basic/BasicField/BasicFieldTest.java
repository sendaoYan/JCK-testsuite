/*
 * @test
 * @summary sun.jvm.hotspot.types.basic.BasicField conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.types.basic:+open
 * @run main BasicFieldTest
 */
import java.lang.reflect.*;

public class BasicFieldTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.types.basic.BasicField", false, BasicFieldTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.types.Field"), "interface sun.jvm.hotspot.types.Field");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "isStatic"), "method isStatic");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getAddress"), "method getAddress");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getStaticFieldAddress"), "method getStaticFieldAddress");
        check(hasMethod(c, "getJLong"), "method getJLong");
        check(hasMethod(c, "getJBoolean"), "method getJBoolean");
        check(hasMethod(c, "getJByte"), "method getJByte");
        check(hasMethod(c, "getJChar"), "method getJChar");
        check(hasMethod(c, "getJShort"), "method getJShort");
        check(hasMethod(c, "getJInt"), "method getJInt");
        check(hasMethod(c, "getJFloat"), "method getJFloat");
        check(hasMethod(c, "getJDouble"), "method getJDouble");
        check(hasMethod(c, "getCInteger"), "method getCInteger");
        check(hasMethod(c, "getOopHandle"), "method getOopHandle");
        check(hasMethod(c, "getNarrowOopHandle"), "method getNarrowOopHandle");
        check(hasField(c, "db"), "field db");
        check(hasField(c, "type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
