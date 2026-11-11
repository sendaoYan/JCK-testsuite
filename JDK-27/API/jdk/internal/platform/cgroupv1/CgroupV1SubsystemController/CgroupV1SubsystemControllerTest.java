/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.cgroupv1.CgroupV1SubsystemController conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform.cgroupv1:+open
 * @run main CgroupV1SubsystemControllerTest
 */
public class CgroupV1SubsystemControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.cgroupv1.CgroupV1SubsystemController", false, CgroupV1SubsystemControllerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "jdk.internal.platform.CgroupSubsystemController"), "interface jdk.internal.platform.CgroupSubsystemController");
        check(hasMethod(c, "convertHierachicalLimitLine"), "method convertHierachicalLimitLine");
        check(hasMethod(c, "convertStringToLong"), "method convertStringToLong");
        check(hasMethod(c, "getDoubleValue"), "method getDoubleValue");
        check(hasMethod(c, "getLongEntry"), "method getLongEntry");
        check(hasMethod(c, "getLongValueMatchingLine"), "method getLongValueMatchingLine");
        check(hasMethod(c, "longValOrUnlimited"), "method longValOrUnlimited");
        check(hasMethod(c, "path"), "method path");
        check(hasMethod(c, "setPath"), "method setPath");
        check(hasField(c, "DOUBLE_RETVAL_UNLIMITED"), "field DOUBLE_RETVAL_UNLIMITED");
        check(hasField(c, "UNLIMITED_MIN"), "field UNLIMITED_MIN");
        check(hasField(c, "mountPoint"), "field mountPoint");
        check(hasField(c, "path"), "field path");
        check(hasField(c, "root"), "field root");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
