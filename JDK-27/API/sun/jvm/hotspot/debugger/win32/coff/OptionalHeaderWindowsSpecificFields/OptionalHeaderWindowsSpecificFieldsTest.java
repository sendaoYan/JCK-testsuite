/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.OptionalHeaderWindowsSpecificFields conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main OptionalHeaderWindowsSpecificFieldsTest
 */
import java.lang.reflect.*;

public class OptionalHeaderWindowsSpecificFieldsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.OptionalHeaderWindowsSpecificFields", false, OptionalHeaderWindowsSpecificFieldsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCheckSum"), "method getCheckSum");
        check(hasMethod(c, "getImageBase"), "method getImageBase");
        check(hasMethod(c, "getSectionAlignment"), "method getSectionAlignment");
        check(hasMethod(c, "getFileAlignment"), "method getFileAlignment");
        check(hasMethod(c, "getMajorOperatingSystemVersion"), "method getMajorOperatingSystemVersion");
        check(hasMethod(c, "getMinorOperatingSystemVersion"), "method getMinorOperatingSystemVersion");
        check(hasMethod(c, "getMajorImageVersion"), "method getMajorImageVersion");
        check(hasMethod(c, "getMinorImageVersion"), "method getMinorImageVersion");
        check(hasMethod(c, "getMajorSubsystemVersion"), "method getMajorSubsystemVersion");
        check(hasMethod(c, "getMinorSubsystemVersion"), "method getMinorSubsystemVersion");
        check(hasMethod(c, "getSizeOfImage"), "method getSizeOfImage");
        check(hasMethod(c, "getSizeOfHeaders"), "method getSizeOfHeaders");
        check(hasMethod(c, "getSubsystem"), "method getSubsystem");
        check(hasMethod(c, "getDLLCharacteristics"), "method getDLLCharacteristics");
        check(hasMethod(c, "getSizeOfStackReserve"), "method getSizeOfStackReserve");
        check(hasMethod(c, "getSizeOfStackCommit"), "method getSizeOfStackCommit");
        check(hasMethod(c, "getSizeOfHeapReserve"), "method getSizeOfHeapReserve");
        check(hasMethod(c, "getSizeOfHeapCommit"), "method getSizeOfHeapCommit");
        check(hasMethod(c, "getLoaderFlags"), "method getLoaderFlags");
        check(hasMethod(c, "getNumberOfRvaAndSizes"), "method getNumberOfRvaAndSizes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
