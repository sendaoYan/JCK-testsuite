/*
 * @test
 * @summary sun.reflect.generics.tree.Wildcard conformance: internal package reflection.
 * @modules java.base/sun.reflect.generics.tree:+open
 * @run main WildcardTest
 */
public class WildcardTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.tree.Wildcard", false, WildcardTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.reflect.generics.tree.TypeArgument"), "interface sun.reflect.generics.tree.TypeArgument");
        check(hasMethod(c, "accept"), "method accept");
        check(hasMethod(c, "getLowerBounds"), "method getLowerBounds");
        check(hasMethod(c, "getUpperBounds"), "method getUpperBounds");
        check(hasMethod(c, "make"), "method make");
        check(hasField(c, "emptyBounds"), "field emptyBounds");
        check(hasField(c, "lowerBounds"), "field lowerBounds");
        check(hasField(c, "upperBounds"), "field upperBounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
