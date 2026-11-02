/*
 * @test
 * @summary jdk.internal.platform.cgroupv1.CgroupV1MemorySubSystemController conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform.cgroupv1:+open
 * @run main CgroupV1MemorySubSystemControllerTest
 */
public class CgroupV1MemorySubSystemControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.cgroupv1.CgroupV1MemorySubSystemController", false, CgroupV1MemorySubSystemControllerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.platform.cgroupv1.CgroupV1SubsystemController"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "hierarchical"), "field hierarchical");
        check(hasField(c, "swapenabled"), "field swapenabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
