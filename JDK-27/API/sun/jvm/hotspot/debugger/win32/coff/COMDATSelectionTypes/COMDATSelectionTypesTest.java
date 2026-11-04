/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.COMDATSelectionTypes conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main COMDATSelectionTypesTest
 */
import java.lang.reflect.*;

public class COMDATSelectionTypesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.COMDATSelectionTypes", false, COMDATSelectionTypesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_COMDAT_SELECT_NODUPLICATES"), "field IMAGE_COMDAT_SELECT_NODUPLICATES");
        check(hasField(c, "IMAGE_COMDAT_SELECT_ANY"), "field IMAGE_COMDAT_SELECT_ANY");
        check(hasField(c, "IMAGE_COMDAT_SELECT_SAME_SIZE"), "field IMAGE_COMDAT_SELECT_SAME_SIZE");
        check(hasField(c, "IMAGE_COMDAT_SELECT_EXACT_MATCH"), "field IMAGE_COMDAT_SELECT_EXACT_MATCH");
        check(hasField(c, "IMAGE_COMDAT_SELECT_ASSOCIATIVE"), "field IMAGE_COMDAT_SELECT_ASSOCIATIVE");
        check(hasField(c, "IMAGE_COMDAT_SELECT_LARGEST"), "field IMAGE_COMDAT_SELECT_LARGEST");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
