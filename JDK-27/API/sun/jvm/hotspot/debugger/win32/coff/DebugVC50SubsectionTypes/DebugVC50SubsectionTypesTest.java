/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SubsectionTypes conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SubsectionTypesTest
 */
import java.lang.reflect.*;

public class DebugVC50SubsectionTypesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SubsectionTypes", false, DebugVC50SubsectionTypesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "SST_MODULE"), "field SST_MODULE");
        check(hasField(c, "SST_TYPES"), "field SST_TYPES");
        check(hasField(c, "SST_PUBLIC"), "field SST_PUBLIC");
        check(hasField(c, "SST_PUBLIC_SYM"), "field SST_PUBLIC_SYM");
        check(hasField(c, "SST_SYMBOLS"), "field SST_SYMBOLS");
        check(hasField(c, "SST_ALIGN_SYM"), "field SST_ALIGN_SYM");
        check(hasField(c, "SST_SRC_LN_SEG"), "field SST_SRC_LN_SEG");
        check(hasField(c, "SST_SRC_MODULE"), "field SST_SRC_MODULE");
        check(hasField(c, "SST_LIBRARIES"), "field SST_LIBRARIES");
        check(hasField(c, "SST_GLOBAL_SYM"), "field SST_GLOBAL_SYM");
        check(hasField(c, "SST_GLOBAL_PUB"), "field SST_GLOBAL_PUB");
        check(hasField(c, "SST_GLOBAL_TYPES"), "field SST_GLOBAL_TYPES");
        check(hasField(c, "SST_MPC"), "field SST_MPC");
        check(hasField(c, "SST_SEG_MAP"), "field SST_SEG_MAP");
        check(hasField(c, "SST_SEG_NAME"), "field SST_SEG_NAME");
        check(hasField(c, "SST_PRE_COMP"), "field SST_PRE_COMP");
        check(hasField(c, "SST_UNUSED"), "field SST_UNUSED");
        check(hasField(c, "SST_OFFSET_MAP_16"), "field SST_OFFSET_MAP_16");
        check(hasField(c, "SST_OFFSET_MAP_32"), "field SST_OFFSET_MAP_32");
        check(hasField(c, "SST_FILE_INDEX"), "field SST_FILE_INDEX");
        check(hasField(c, "SST_STATIC_SYM"), "field SST_STATIC_SYM");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
