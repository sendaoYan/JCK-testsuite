/*
 * @test
 * @summary jdk.jpackage.internal.model.StandardPackageType conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.model:+open
 * @run main StandardPackageTypeTest
 */
public class StandardPackageTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.model.StandardPackageType", false, StandardPackageTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 5, "5 declared methods");
        check(c.getDeclaredFields().length == 9, "9 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 6, "6 enum constants");
        check(ec.contains("LINUX_DEB"), "enum constant LINUX_DEB");
        check(ec.contains("LINUX_RPM"), "enum constant LINUX_RPM");
        check(ec.contains("MAC_DMG"), "enum constant MAC_DMG");
        check(ec.contains("MAC_PKG"), "enum constant MAC_PKG");
        check(ec.contains("WIN_EXE"), "enum constant WIN_EXE");
        check(ec.contains("WIN_MSI"), "enum constant WIN_MSI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
