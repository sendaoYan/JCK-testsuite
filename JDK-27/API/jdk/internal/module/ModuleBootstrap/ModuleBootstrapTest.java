/*
 * @test
 * @summary jdk.internal.module.ModuleBootstrap conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ModuleBootstrapTest
 */
public class ModuleBootstrapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ModuleBootstrap", false, ModuleBootstrapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "boot"), "method boot");
        check(hasMethod(c, "illegalFinalFieldMutation"), "method illegalFinalFieldMutation");
        check(hasMethod(c, "illegalNativeAccess"), "method illegalNativeAccess");
        check(hasMethod(c, "limitedFinder"), "method limitedFinder");
        check(hasMethod(c, "patcher"), "method patcher");
        check(hasMethod(c, "unlimitedFinder"), "method unlimitedFinder");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "ADD_EXPORTS"), "field ADD_EXPORTS");
        check(hasField(c, "ADD_MODULES"), "field ADD_MODULES");
        check(hasField(c, "ADD_OPENS"), "field ADD_OPENS");
        check(hasField(c, "ADD_READS"), "field ADD_READS");
        check(hasField(c, "ALL_DEFAULT"), "field ALL_DEFAULT");
        check(hasField(c, "ALL_MODULE_PATH"), "field ALL_MODULE_PATH");
        check(hasField(c, "ALL_SYSTEM"), "field ALL_SYSTEM");
        check(hasField(c, "ALL_UNNAMED"), "field ALL_UNNAMED");
        check(hasField(c, "ENABLE_NATIVE_ACCESS"), "field ENABLE_NATIVE_ACCESS");
        check(hasField(c, "ILLEGAL_FINAL_FIELD_MUTATION"), "field ILLEGAL_FINAL_FIELD_MUTATION");
        check(hasField(c, "ILLEGAL_NATIVE_ACCESS"), "field ILLEGAL_NATIVE_ACCESS");
        check(hasField(c, "JAVA_BASE"), "field JAVA_BASE");
        check(hasField(c, "JDK_NATIVE_ACCESS_MODULES"), "field JDK_NATIVE_ACCESS_MODULES");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "JLMA"), "field JLMA");
        check(hasField(c, "PATCH_MODULE"), "field PATCH_MODULE");
        check(hasField(c, "USER_NATIVE_ACCESS_MODULES"), "field USER_NATIVE_ACCESS_MODULES");
        check(hasField(c, "limitedFinder"), "field limitedFinder");
        check(hasField(c, "patcher"), "field patcher");
        check(hasField(c, "unlimitedFinder"), "field unlimitedFinder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
