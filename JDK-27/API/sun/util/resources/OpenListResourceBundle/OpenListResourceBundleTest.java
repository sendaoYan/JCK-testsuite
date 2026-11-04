/*
 * @test
 * @summary sun.util.resources.OpenListResourceBundle conformance: internal package reflection.
 * @modules java.base/sun.util.resources:+open
 * @run main OpenListResourceBundleTest
 */
public class OpenListResourceBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.OpenListResourceBundle", false, OpenListResourceBundleTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ResourceBundle"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "createMap"), "method createMap");
        check(hasMethod(c, "createSet"), "method createSet");
        check(hasMethod(c, "getContents"), "method getContents");
        check(hasMethod(c, "getKeys"), "method getKeys");
        check(hasMethod(c, "handleGetObject"), "method handleGetObject");
        check(hasMethod(c, "handleKeySet"), "method handleKeySet");
        check(hasMethod(c, "keySet"), "method keySet");
        check(hasField(c, "keyset"), "field keyset");
        check(hasField(c, "lookup"), "field lookup");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
