/*
 * @test
 * @summary sun.jvm.hotspot.runtime.Flags conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main FlagsTest
 */
import java.lang.reflect.*;

public class FlagsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.Flags", false, FlagsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == true, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Enum".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "value"), "method value");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasField(c, "DEFAULT"), "field DEFAULT");
        check(hasField(c, "COMMAND_LINE"), "field COMMAND_LINE");
        check(hasField(c, "ENVIRON_VAR"), "field ENVIRON_VAR");
        check(hasField(c, "CONFIG_FILE"), "field CONFIG_FILE");
        check(hasField(c, "MANAGEMENT"), "field MANAGEMENT");
        check(hasField(c, "ERGONOMIC"), "field ERGONOMIC");
        check(hasField(c, "ATTACH_ON_DEMAND"), "field ATTACH_ON_DEMAND");
        check(hasField(c, "INTERNAL"), "field INTERNAL");
        check(hasField(c, "JIMAGE_RESOURCE"), "field JIMAGE_RESOURCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
