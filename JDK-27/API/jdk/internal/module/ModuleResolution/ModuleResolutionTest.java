/*
 * @test
 * @summary jdk.internal.module.ModuleResolution conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ModuleResolutionTest
 */
public class ModuleResolutionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ModuleResolution", false, ModuleResolutionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "doNotResolveByDefault"), "method doNotResolveByDefault");
        check(hasMethod(c, "empty"), "method empty");
        check(hasMethod(c, "hasDeprecatedForRemovalWarning"), "method hasDeprecatedForRemovalWarning");
        check(hasMethod(c, "hasDeprecatedWarning"), "method hasDeprecatedWarning");
        check(hasMethod(c, "hasIncubatingWarning"), "method hasIncubatingWarning");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "value"), "method value");
        check(hasMethod(c, "withDeprecated"), "method withDeprecated");
        check(hasMethod(c, "withDeprecatedForRemoval"), "method withDeprecatedForRemoval");
        check(hasMethod(c, "withDoNotResolveByDefault"), "method withDoNotResolveByDefault");
        check(hasMethod(c, "withIncubating"), "method withIncubating");
        check(hasField(c, "value"), "field value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
