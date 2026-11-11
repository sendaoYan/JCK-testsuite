/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.CgroupSubsystem conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform:+open
 * @run main CgroupSubsystemTest
 */
public class CgroupSubsystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupSubsystem", false, CgroupSubsystemTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "jdk.internal.platform.Metrics"), "interface jdk.internal.platform.Metrics");
        check(hasMethod(c, "isContainerized"), "method isContainerized");
        check(hasMethod(c, "limitFromString"), "method limitFromString");
        check(hasField(c, "LONG_RETVAL_UNLIMITED"), "field LONG_RETVAL_UNLIMITED");
        check(hasField(c, "MAX_VAL"), "field MAX_VAL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
