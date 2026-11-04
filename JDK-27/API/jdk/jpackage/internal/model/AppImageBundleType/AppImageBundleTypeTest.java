/*
 * @test
 * @summary jdk.jpackage.internal.model.AppImageBundleType conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.model:+open
 * @run main AppImageBundleTypeTest
 */
public class AppImageBundleTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.model.AppImageBundleType", false, AppImageBundleTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 4, "4 declared methods");
        check(c.getDeclaredFields().length == 5, "5 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 3, "3 enum constants");
        check(ec.contains("LINUX_APP_IMAGE"), "enum constant LINUX_APP_IMAGE");
        check(ec.contains("MAC_APP_IMAGE"), "enum constant MAC_APP_IMAGE");
        check(ec.contains("WIN_APP_IMAGE"), "enum constant WIN_APP_IMAGE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
