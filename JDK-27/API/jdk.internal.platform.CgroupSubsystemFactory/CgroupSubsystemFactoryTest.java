/*
 * @test
 * @summary jdk.internal.platform.CgroupSubsystemFactory conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform:+open
 * @run main CgroupSubsystemFactoryTest
 */
public class CgroupSubsystemFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupSubsystemFactory", false, CgroupSubsystemFactoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "determineType"), "method determineType");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BLKIO_CTRL"), "field BLKIO_CTRL");
        check(hasField(c, "CPUACCT_CTRL"), "field CPUACCT_CTRL");
        check(hasField(c, "CPUSET_CTRL"), "field CPUSET_CTRL");
        check(hasField(c, "CPU_CTRL"), "field CPU_CTRL");
        check(hasField(c, "MEMORY_CTRL"), "field MEMORY_CTRL");
        check(hasField(c, "MOUNTINFO_PATTERN"), "field MOUNTINFO_PATTERN");
        check(hasField(c, "PIDS_CTRL"), "field PIDS_CTRL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
