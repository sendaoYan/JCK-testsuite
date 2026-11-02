/*
 * @test
 * @summary jdk.internal.module.Modules conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ModulesTest
 */
public class ModulesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.Modules", false, ModulesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addEnableFinalMutationToAllUnnamed"), "method addEnableFinalMutationToAllUnnamed");
        check(hasMethod(c, "addEnableNativeAccessToAllUnnamed"), "method addEnableNativeAccessToAllUnnamed");
        check(hasMethod(c, "addExports"), "method addExports");
        check(hasMethod(c, "addExportsToAllUnnamed"), "method addExportsToAllUnnamed");
        check(hasMethod(c, "addOpens"), "method addOpens");
        check(hasMethod(c, "addOpensToAllUnnamed"), "method addOpensToAllUnnamed");
        check(hasMethod(c, "addProvides"), "method addProvides");
        check(hasMethod(c, "addReads"), "method addReads");
        check(hasMethod(c, "addReadsAllUnnamed"), "method addReadsAllUnnamed");
        check(hasMethod(c, "addUses"), "method addUses");
        check(hasMethod(c, "defineModule"), "method defineModule");
        check(hasMethod(c, "findLoadedModule"), "method findLoadedModule");
        check(hasMethod(c, "isFinalMutationEnabled"), "method isFinalMutationEnabled");
        check(hasMethod(c, "isStaticallyExported"), "method isStaticallyExported");
        check(hasMethod(c, "isStaticallyOpened"), "method isStaticallyOpened");
        check(hasMethod(c, "loadModule"), "method loadModule");
        check(hasMethod(c, "newBootLayerConfiguration"), "method newBootLayerConfiguration");
        check(hasMethod(c, "transformedByAgent"), "method transformedByAgent");
        check(hasMethod(c, "tryEnableFinalMutation"), "method tryEnableFinalMutation");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "JLMA"), "field JLMA");
        check(hasField(c, "topLayer"), "field topLayer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
