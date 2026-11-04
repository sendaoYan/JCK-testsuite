/*
 * @test
 * @summary jdk.jpackage.internal.summary.StandardWarning conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.summary:+open
 * @run main StandardWarningTest
 */
public class StandardWarningTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.summary.StandardWarning", false, StandardWarningTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 4, "4 declared methods");
        check(c.getDeclaredFields().length == 7, "7 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 5, "5 enum constants");
        check(ec.contains("LINUX_DEB_MISSING_LICENSE_FILE"), "enum constant LINUX_DEB_MISSING_LICENSE_FILE");
        check(ec.contains("MAC_BUNDLE_NAME_TOO_LONG"), "enum constant MAC_BUNDLE_NAME_TOO_LONG");
        check(ec.contains("MAC_NON_STANDARD_APP_CONTENT"), "enum constant MAC_NON_STANDARD_APP_CONTENT");
        check(ec.contains("MAC_SIGNED_PKG_WITH_UNSIGNED_PREDEFINED_APP_IMAGE"), "enum constant MAC_SIGNED_PKG_WITH_UNSIGNED_PREDEFINED_APP_IMAGE");
        check(ec.contains("MAC_SIGNED_PREDEFINED_APP_IMAGE_WITHOUT_PACKAGE_FILE"), "enum constant MAC_SIGNED_PREDEFINED_APP_IMAGE_WITHOUT_PACKAGE_FILE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
