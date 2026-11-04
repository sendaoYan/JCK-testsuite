/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.OptionalHeaderStandardFields conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main OptionalHeaderStandardFieldsTest
 */
import java.lang.reflect.*;

public class OptionalHeaderStandardFieldsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.OptionalHeaderStandardFields", false, OptionalHeaderStandardFieldsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getMajorLinkerVersion"), "method getMajorLinkerVersion");
        check(hasMethod(c, "getMinorLinkerVersion"), "method getMinorLinkerVersion");
        check(hasMethod(c, "getSizeOfCode"), "method getSizeOfCode");
        check(hasMethod(c, "getSizeOfInitializedData"), "method getSizeOfInitializedData");
        check(hasMethod(c, "getSizeOfUninitializedData"), "method getSizeOfUninitializedData");
        check(hasMethod(c, "getAddressOfEntryPoint"), "method getAddressOfEntryPoint");
        check(hasMethod(c, "getBaseOfCode"), "method getBaseOfCode");
        check(hasMethod(c, "getBaseOfData"), "method getBaseOfData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
