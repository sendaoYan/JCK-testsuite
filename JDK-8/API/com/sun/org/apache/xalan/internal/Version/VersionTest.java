/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.Version conformance: reflection.
 * @run main VersionTest
 */

public class VersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.Version", false, VersionTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Version"), "simple name Version");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getVersion"), "method getVersion");
        check(has(c, "_main"), "method _main");
        check(has(c, "getProduct"), "method getProduct");
        check(has(c, "getImplementationLanguage"), "method getImplementationLanguage");
        check(has(c, "getMajorVersionNum"), "method getMajorVersionNum");
        check(has(c, "getReleaseVersionNum"), "method getReleaseVersionNum");
        check(has(c, "getMaintenanceVersionNum"), "method getMaintenanceVersionNum");
        check(has(c, "getDevelopmentVersionNum"), "method getDevelopmentVersionNum");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
