/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.CgroupInfo conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform:+open
 * @run main CgroupInfoTest
 */
public class CgroupInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupInfo", false, CgroupInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCgroupPath"), "method getCgroupPath");
        check(hasMethod(c, "getHierarchyId"), "method getHierarchyId");
        check(hasMethod(c, "getMountPoint"), "method getMountPoint");
        check(hasMethod(c, "getMountRoot"), "method getMountRoot");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "setCgroupPath"), "method setCgroupPath");
        check(hasMethod(c, "setMountPoint"), "method setMountPoint");
        check(hasMethod(c, "setMountRoot"), "method setMountRoot");
        check(hasField(c, "cgroupPath"), "field cgroupPath");
        check(hasField(c, "enabled"), "field enabled");
        check(hasField(c, "hierarchyId"), "field hierarchyId");
        check(hasField(c, "mountPoint"), "field mountPoint");
        check(hasField(c, "mountRoot"), "field mountRoot");
        check(hasField(c, "name"), "field name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
