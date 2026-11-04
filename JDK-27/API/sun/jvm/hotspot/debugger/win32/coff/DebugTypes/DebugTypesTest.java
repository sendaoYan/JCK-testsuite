/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugTypes conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugTypesTest
 */
import java.lang.reflect.*;

public class DebugTypesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugTypes", false, DebugTypesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_DEBUG_TYPE_UNKNOWN"), "field IMAGE_DEBUG_TYPE_UNKNOWN");
        check(hasField(c, "IMAGE_DEBUG_TYPE_COFF"), "field IMAGE_DEBUG_TYPE_COFF");
        check(hasField(c, "IMAGE_DEBUG_TYPE_CODEVIEW"), "field IMAGE_DEBUG_TYPE_CODEVIEW");
        check(hasField(c, "IMAGE_DEBUG_TYPE_FPO"), "field IMAGE_DEBUG_TYPE_FPO");
        check(hasField(c, "IMAGE_DEBUG_TYPE_MISC"), "field IMAGE_DEBUG_TYPE_MISC");
        check(hasField(c, "IMAGE_DEBUG_TYPE_EXCEPTION"), "field IMAGE_DEBUG_TYPE_EXCEPTION");
        check(hasField(c, "IMAGE_DEBUG_TYPE_FIXUP"), "field IMAGE_DEBUG_TYPE_FIXUP");
        check(hasField(c, "IMAGE_DEBUG_TYPE_OMAP_TO_SRC"), "field IMAGE_DEBUG_TYPE_OMAP_TO_SRC");
        check(hasField(c, "IMAGE_DEBUG_TYPE_OMAP_FROM_SRC"), "field IMAGE_DEBUG_TYPE_OMAP_FROM_SRC");
        check(hasField(c, "IMAGE_DEBUG_TYPE_BORLAND"), "field IMAGE_DEBUG_TYPE_BORLAND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
