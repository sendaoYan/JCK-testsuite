/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SegDescEnums conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SegDescEnumsTest
 */
import java.lang.reflect.*;

public class DebugVC50SegDescEnumsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SegDescEnums", false, DebugVC50SegDescEnumsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "SEGMAP_GROUP_MASK"), "field SEGMAP_GROUP_MASK");
        check(hasField(c, "SEGMAP_ABS_MASK"), "field SEGMAP_ABS_MASK");
        check(hasField(c, "SEGMAP_SEL_MASK"), "field SEGMAP_SEL_MASK");
        check(hasField(c, "SEGMAP_32BIT_MASK"), "field SEGMAP_32BIT_MASK");
        check(hasField(c, "SEGMAP_EXECUTABLE_MASK"), "field SEGMAP_EXECUTABLE_MASK");
        check(hasField(c, "SEGMAP_WRITABLE_MASK"), "field SEGMAP_WRITABLE_MASK");
        check(hasField(c, "SEGMAP_READABLE_MASK"), "field SEGMAP_READABLE_MASK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
