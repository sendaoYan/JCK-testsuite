/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.COFFSymbol conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main COFFSymbolTest
 */
import java.lang.reflect.*;

public class COFFSymbolTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.COFFSymbol", false, COFFSymbolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "isFile"), "method isFile");
        check(hasMethod(c, "getSectionNumber"), "method getSectionNumber");
        check(hasMethod(c, "getStorageClass"), "method getStorageClass");
        check(hasMethod(c, "getNumberOfAuxSymbols"), "method getNumberOfAuxSymbols");
        check(hasMethod(c, "isFunctionDefinition"), "method isFunctionDefinition");
        check(hasMethod(c, "getAuxFunctionDefinitionRecord"), "method getAuxFunctionDefinitionRecord");
        check(hasMethod(c, "isBfOrEfSymbol"), "method isBfOrEfSymbol");
        check(hasMethod(c, "getAuxBfEfRecord"), "method getAuxBfEfRecord");
        check(hasMethod(c, "isWeakExternal"), "method isWeakExternal");
        check(hasMethod(c, "getAuxWeakExternalRecord"), "method getAuxWeakExternalRecord");
        check(hasMethod(c, "getAuxFileRecord"), "method getAuxFileRecord");
        check(hasMethod(c, "isSectionDefinition"), "method isSectionDefinition");
        check(hasMethod(c, "getAuxSectionDefinitionsRecord"), "method getAuxSectionDefinitionsRecord");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
