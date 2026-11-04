/*
 * @test
 * @summary jdk.jpackage.internal.cli.StandardBundlingOperation conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.cli:+open
 * @run main StandardBundlingOperationTest
 */
public class StandardBundlingOperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.cli.StandardBundlingOperation", false, StandardBundlingOperationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 18, "18 declared methods");
        check(c.getDeclaredFields().length == 29, "29 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 10, "10 enum constants");
        check(ec.contains("CREATE_LINUX_APP_IMAGE"), "enum constant CREATE_LINUX_APP_IMAGE");
        check(ec.contains("CREATE_LINUX_DEB"), "enum constant CREATE_LINUX_DEB");
        check(ec.contains("CREATE_LINUX_RPM"), "enum constant CREATE_LINUX_RPM");
        check(ec.contains("CREATE_MAC_APP_IMAGE"), "enum constant CREATE_MAC_APP_IMAGE");
        check(ec.contains("CREATE_MAC_DMG"), "enum constant CREATE_MAC_DMG");
        check(ec.contains("CREATE_MAC_PKG"), "enum constant CREATE_MAC_PKG");
        check(ec.contains("CREATE_WIN_APP_IMAGE"), "enum constant CREATE_WIN_APP_IMAGE");
        check(ec.contains("CREATE_WIN_EXE"), "enum constant CREATE_WIN_EXE");
        check(ec.contains("CREATE_WIN_MSI"), "enum constant CREATE_WIN_MSI");
        check(ec.contains("SIGN_MAC_APP_IMAGE"), "enum constant SIGN_MAC_APP_IMAGE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
