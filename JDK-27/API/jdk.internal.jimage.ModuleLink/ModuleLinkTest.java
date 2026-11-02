/*
 * @test
 * @summary jdk.internal.jimage.ModuleLink conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main ModuleLinkTest
 */
public class ModuleLinkTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ModuleLink", false, ModuleLinkTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Comparable"), "interface java.lang.Comparable");
        check(hasMethod(c, "compareTo"), "method compareTo");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "forEmptyPackage"), "method forEmptyPackage");
        check(hasMethod(c, "forPackage"), "method forPackage");
        check(hasMethod(c, "hasPreviewVersion"), "method hasPreviewVersion");
        check(hasMethod(c, "hasResources"), "method hasResources");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isPreviewOnly"), "method isPreviewOnly");
        check(hasMethod(c, "merge"), "method merge");
        check(hasMethod(c, "name"), "method name");
        check(hasMethod(c, "readNameOffsets"), "method readNameOffsets");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "write"), "method write");
        check(hasField(c, "FLAGS_PKG_HAS_NORMAL_VERSION"), "field FLAGS_PKG_HAS_NORMAL_VERSION");
        check(hasField(c, "FLAGS_PKG_HAS_PREVIEW_VERSION"), "field FLAGS_PKG_HAS_PREVIEW_VERSION");
        check(hasField(c, "FLAGS_PKG_HAS_RESOURCES"), "field FLAGS_PKG_HAS_RESOURCES");
        check(hasField(c, "PREVIEW_FIRST"), "field PREVIEW_FIRST");
        check(hasField(c, "flags"), "field flags");
        check(hasField(c, "name"), "field name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
