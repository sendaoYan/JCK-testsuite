/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.AuxWeakExternalRecord conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main AuxWeakExternalRecordTest
 */
import java.lang.reflect.*;

public class AuxWeakExternalRecordTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.AuxWeakExternalRecord", false, AuxWeakExternalRecordTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.win32.coff.AuxSymbolRecord"), "interface sun.jvm.hotspot.debugger.win32.coff.AuxSymbolRecord");
        check(hasMethod(c, "getTagIndex"), "method getTagIndex");
        check(hasMethod(c, "getCharacteristics"), "method getCharacteristics");
        check(hasField(c, "IMAGE_WEAK_EXTERN_SEARCH_NOLIBRARY"), "field IMAGE_WEAK_EXTERN_SEARCH_NOLIBRARY");
        check(hasField(c, "IMAGE_WEAK_EXTERN_SEARCH_LIBRARY"), "field IMAGE_WEAK_EXTERN_SEARCH_LIBRARY");
        check(hasField(c, "IMAGE_WEAK_EXTERN_SEARCH_ALIAS"), "field IMAGE_WEAK_EXTERN_SEARCH_ALIAS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
