/*
 * @test
 * @summary sun.jvm.hotspot.debugger.linux.DwarfParser conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.linux:+open
 * @run main DwarfParserTest
 */
import java.lang.reflect.*;

public class DwarfParserTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.linux.DwarfParser", false, DwarfParserTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "processDwarf"), "method processDwarf");
        check(hasMethod(c, "isBPOffsetAvailable"), "method isBPOffsetAvailable");
        check(hasMethod(c, "getBasePointerOffsetFromCFA"), "method getBasePointerOffsetFromCFA");
        check(hasMethod(c, "getReturnAddressOffsetFromCFA"), "method getReturnAddressOffsetFromCFA");
        check(hasMethod(c, "isIn"), "method isIn");
        check(hasMethod(c, "getCFARegister"), "method getCFARegister");
        check(hasMethod(c, "getCFAOffset"), "method getCFAOffset");
        check(hasMethod(c, "getOffsetFromCFA"), "method getOffsetFromCFA");
        check(hasMethod(c, "getRARegister"), "method getRARegister");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
