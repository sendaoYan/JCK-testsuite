/*
 * @test
 * @summary jdk.jpackage.internal.summary.StandardProperty conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.summary:+open
 * @run main StandardPropertyTest
 */
public class StandardPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.summary.StandardProperty", false, StandardPropertyTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 6, "6 declared methods");
        check(c.getDeclaredFields().length == 14, "14 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 11, "11 enum constants");
        check(ec.contains("LINUX_DISABLE_REQUIRED_PACKAGES_SEARCH"), "enum constant LINUX_DISABLE_REQUIRED_PACKAGES_SEARCH");
        check(ec.contains("LINUX_PACKAGE_NAME"), "enum constant LINUX_PACKAGE_NAME");
        check(ec.contains("MAC_BUNDLE_IDENTIFIER"), "enum constant MAC_BUNDLE_IDENTIFIER");
        check(ec.contains("MAC_BUNDLE_NAME"), "enum constant MAC_BUNDLE_NAME");
        check(ec.contains("MAC_SIGN_APP_IMAGE_OPERATION"), "enum constant MAC_SIGN_APP_IMAGE_OPERATION");
        check(ec.contains("OPERATION"), "enum constant OPERATION");
        check(ec.contains("OUTPUT_BUNDLE"), "enum constant OUTPUT_BUNDLE");
        check(ec.contains("VERSION"), "enum constant VERSION");
        check(ec.contains("WIN_MSI_PRODUCT_CODE"), "enum constant WIN_MSI_PRODUCT_CODE");
        check(ec.contains("WIN_MSI_UPGRADE_CODE"), "enum constant WIN_MSI_UPGRADE_CODE");
        check(ec.contains("WIN_WIX_VERSION"), "enum constant WIN_WIX_VERSION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
