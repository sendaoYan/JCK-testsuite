/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50MemberAttributes conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50MemberAttributesTest
 */
import java.lang.reflect.*;

public class DebugVC50MemberAttributesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50MemberAttributes", false, DebugVC50MemberAttributesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "MEMATTR_ACCESS_MASK"), "field MEMATTR_ACCESS_MASK");
        check(hasField(c, "MEMATTR_ACCESS_NO_PROTECTION"), "field MEMATTR_ACCESS_NO_PROTECTION");
        check(hasField(c, "MEMATTR_ACCESS_PRIVATE"), "field MEMATTR_ACCESS_PRIVATE");
        check(hasField(c, "MEMATTR_ACCESS_PROTECTED"), "field MEMATTR_ACCESS_PROTECTED");
        check(hasField(c, "MEMATTR_ACCESS_PUBLIC"), "field MEMATTR_ACCESS_PUBLIC");
        check(hasField(c, "MEMATTR_MPROP_MASK"), "field MEMATTR_MPROP_MASK");
        check(hasField(c, "MEMATTR_MPROP_VANILLA"), "field MEMATTR_MPROP_VANILLA");
        check(hasField(c, "MEMATTR_MPROP_VIRTUAL"), "field MEMATTR_MPROP_VIRTUAL");
        check(hasField(c, "MEMATTR_MPROP_STATIC"), "field MEMATTR_MPROP_STATIC");
        check(hasField(c, "MEMATTR_MPROP_FRIEND"), "field MEMATTR_MPROP_FRIEND");
        check(hasField(c, "MEMATTR_MPROP_INTRODUCING_VIRTUAL"), "field MEMATTR_MPROP_INTRODUCING_VIRTUAL");
        check(hasField(c, "MEMATTR_MPROP_PURE_VIRTUAL"), "field MEMATTR_MPROP_PURE_VIRTUAL");
        check(hasField(c, "MEMATTR_MPROP_PURE_INTRODUCING_VIRTUAL"), "field MEMATTR_MPROP_PURE_INTRODUCING_VIRTUAL");
        check(hasField(c, "MEMATTR_PSEUDO_MASK"), "field MEMATTR_PSEUDO_MASK");
        check(hasField(c, "MEMATTR_NOINHERIT_MASK"), "field MEMATTR_NOINHERIT_MASK");
        check(hasField(c, "MEMATTR_NOCONSTRUCT_MASK"), "field MEMATTR_NOCONSTRUCT_MASK");
        check(hasField(c, "MEMATTR_COMPGENX_MASK"), "field MEMATTR_COMPGENX_MASK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
