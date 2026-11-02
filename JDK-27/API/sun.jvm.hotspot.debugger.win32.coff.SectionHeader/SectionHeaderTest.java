/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.SectionHeader conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main SectionHeaderTest
 */
import java.lang.reflect.*;

public class SectionHeaderTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.SectionHeader", false, SectionHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getNumberOfRelocations"), "method getNumberOfRelocations");
        check(hasMethod(c, "getNumberOfLineNumbers"), "method getNumberOfLineNumbers");
        check(hasMethod(c, "getVirtualAddress"), "method getVirtualAddress");
        check(hasMethod(c, "getSizeOfRawData"), "method getSizeOfRawData");
        check(hasMethod(c, "getPointerToRawData"), "method getPointerToRawData");
        check(hasMethod(c, "getPointerToRelocations"), "method getPointerToRelocations");
        check(hasMethod(c, "getPointerToLineNumbers"), "method getPointerToLineNumbers");
        check(hasMethod(c, "getSectionFlags"), "method getSectionFlags");
        check(hasMethod(c, "hasSectionFlag"), "method hasSectionFlag");
        check(hasMethod(c, "getCOFFRelocation"), "method getCOFFRelocation");
        check(hasMethod(c, "getCOFFLineNumber"), "method getCOFFLineNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
