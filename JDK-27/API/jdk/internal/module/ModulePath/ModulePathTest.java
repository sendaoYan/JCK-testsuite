/*
 * @test
 * @summary jdk.internal.module.ModulePath conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ModulePathTest
 */
public class ModulePathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ModulePath", false, ModulePathTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.module.ModuleFinder"), "interface java.lang.module.ModuleFinder");
        check(hasMethod(c, "find"), "method find");
        check(hasMethod(c, "findAll"), "method findAll");
        check(hasMethod(c, "of"), "method of");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "AUTOMATIC_MODULE_NAME"), "field AUTOMATIC_MODULE_NAME");
        check(hasField(c, "MODULE_INFO"), "field MODULE_INFO");
        check(hasField(c, "SERVICES_PREFIX"), "field SERVICES_PREFIX");
        check(hasField(c, "cachedModules"), "field cachedModules");
        check(hasField(c, "entries"), "field entries");
        check(hasField(c, "isLinkPhase"), "field isLinkPhase");
        check(hasField(c, "moduleCount"), "field moduleCount");
        check(hasField(c, "next"), "field next");
        check(hasField(c, "patcher"), "field patcher");
        check(hasField(c, "releaseVersion"), "field releaseVersion");
        check(hasField(c, "scanTime"), "field scanTime");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
