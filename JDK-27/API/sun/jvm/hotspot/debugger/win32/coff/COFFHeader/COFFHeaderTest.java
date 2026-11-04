/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.COFFHeader conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main COFFHeaderTest
 */
import java.lang.reflect.*;

public class COFFHeaderTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.COFFHeader", false, COFFHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getString"), "method getString");
        check(hasMethod(c, "getSectionHeader"), "method getSectionHeader");
        check(hasMethod(c, "getNumberOfSymbols"), "method getNumberOfSymbols");
        check(hasMethod(c, "getCharacteristics"), "method getCharacteristics");
        check(hasMethod(c, "getMachineType"), "method getMachineType");
        check(hasMethod(c, "getNumberOfSections"), "method getNumberOfSections");
        check(hasMethod(c, "getTimeDateStamp"), "method getTimeDateStamp");
        check(hasMethod(c, "getPointerToSymbolTable"), "method getPointerToSymbolTable");
        check(hasMethod(c, "getSizeOfOptionalHeader"), "method getSizeOfOptionalHeader");
        check(hasMethod(c, "getOptionalHeader"), "method getOptionalHeader");
        check(hasMethod(c, "hasCharacteristic"), "method hasCharacteristic");
        check(hasMethod(c, "getCOFFSymbol"), "method getCOFFSymbol");
        check(hasMethod(c, "getNumberOfStrings"), "method getNumberOfStrings");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
