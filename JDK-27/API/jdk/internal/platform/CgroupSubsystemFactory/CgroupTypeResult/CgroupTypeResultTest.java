/*
 * @test
 * @summary jdk.internal.platform.CgroupSubsystemFactory.CgroupTypeResult conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform:+open
 * @run main CgroupTypeResultTest
 */
public class CgroupTypeResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupSubsystemFactory$CgroupTypeResult", false, CgroupTypeResultTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getInfos"), "method getInfos");
        check(hasMethod(c, "isAnyCgroupV1Controllers"), "method isAnyCgroupV1Controllers");
        check(hasMethod(c, "isAnyCgroupV2Controllers"), "method isAnyCgroupV2Controllers");
        check(hasMethod(c, "isAnyControllersEnabled"), "method isAnyControllersEnabled");
        check(hasMethod(c, "isCgroupV2"), "method isCgroupV2");
        check(hasField(c, "anyCgroupV1Controllers"), "field anyCgroupV1Controllers");
        check(hasField(c, "anyCgroupV2Controllers"), "field anyCgroupV2Controllers");
        check(hasField(c, "anyControllersEnabled"), "field anyControllersEnabled");
        check(hasField(c, "infos"), "field infos");
        check(hasField(c, "isCgroupV2"), "field isCgroupV2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
