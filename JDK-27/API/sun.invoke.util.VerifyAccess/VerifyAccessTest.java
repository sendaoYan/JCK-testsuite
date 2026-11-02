/*
 * @test
 * @summary sun.invoke.util.VerifyAccess conformance: internal package reflection.
 * @modules java.base/sun.invoke.util:+open
 * @run main VerifyAccessTest
 */
public class VerifyAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.VerifyAccess", false, VerifyAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "classLoaderIsAncestor"), "method classLoaderIsAncestor");
        check(hasMethod(c, "ensureTypeVisible"), "method ensureTypeVisible");
        check(hasMethod(c, "isClassAccessible"), "method isClassAccessible");
        check(hasMethod(c, "isMemberAccessible"), "method isMemberAccessible");
        check(hasMethod(c, "isModuleAccessible"), "method isModuleAccessible");
        check(hasMethod(c, "isSameModule"), "method isSameModule");
        check(hasMethod(c, "isSamePackage"), "method isSamePackage");
        check(hasMethod(c, "isSamePackageMember"), "method isSamePackageMember");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "ALL_ACCESS_MODES"), "field ALL_ACCESS_MODES");
        check(hasField(c, "MODULE_ALLOWED"), "field MODULE_ALLOWED");
        check(hasField(c, "ORIGINAL_ALLOWED"), "field ORIGINAL_ALLOWED");
        check(hasField(c, "PACKAGE_ALLOWED"), "field PACKAGE_ALLOWED");
        check(hasField(c, "PACKAGE_ONLY"), "field PACKAGE_ONLY");
        check(hasField(c, "PROTECTED_OR_PACKAGE_ALLOWED"), "field PROTECTED_OR_PACKAGE_ALLOWED");
        check(hasField(c, "UNCONDITIONAL_ALLOWED"), "field UNCONDITIONAL_ALLOWED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
