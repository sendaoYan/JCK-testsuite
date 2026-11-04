/*
 * @test
 * @summary sun.jvm.hotspot.debugger.MachineDescription conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger:+open
 * @run main MachineDescriptionTest
 */
import java.lang.reflect.*;

public class MachineDescriptionTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.MachineDescription", false, MachineDescriptionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.io.Serializable"), "interface java.io.Serializable");
        check(hasMethod(c, "isBigEndian"), "method isBigEndian");
        check(hasMethod(c, "getAddressSize"), "method getAddressSize");
        check(hasMethod(c, "isLP64"), "method isLP64");
        check(hasMethod(c, "cIntegerTypeMaxValue"), "method cIntegerTypeMaxValue");
        check(hasMethod(c, "cIntegerTypeMinValue"), "method cIntegerTypeMinValue");
        check(hasMethod(c, "supports32bitAlignmentOf64bitTypes"), "method supports32bitAlignmentOf64bitTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
