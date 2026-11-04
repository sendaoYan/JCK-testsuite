/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugDirectoryEntry conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugDirectoryEntryTest
 */
import java.lang.reflect.*;

public class DebugDirectoryEntryTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugDirectoryEntry", false, DebugDirectoryEntryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getMajorVersion"), "method getMajorVersion");
        check(hasMethod(c, "getMinorVersion"), "method getMinorVersion");
        check(hasMethod(c, "getCharacteristics"), "method getCharacteristics");
        check(hasMethod(c, "getTimeDateStamp"), "method getTimeDateStamp");
        check(hasMethod(c, "getPointerToRawData"), "method getPointerToRawData");
        check(hasMethod(c, "getSizeOfData"), "method getSizeOfData");
        check(hasMethod(c, "getAddressOfRawData"), "method getAddressOfRawData");
        check(hasMethod(c, "getDebugVC50"), "method getDebugVC50");
        check(hasMethod(c, "getRawDataByte"), "method getRawDataByte");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
