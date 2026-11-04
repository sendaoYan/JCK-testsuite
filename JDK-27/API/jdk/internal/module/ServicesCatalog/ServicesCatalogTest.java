/*
 * @test
 * @summary jdk.internal.module.ServicesCatalog conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main ServicesCatalogTest
 */
public class ServicesCatalogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ServicesCatalog", false, ServicesCatalogTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addProvider"), "method addProvider");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "findServices"), "method findServices");
        check(hasMethod(c, "getServicesCatalog"), "method getServicesCatalog");
        check(hasMethod(c, "getServicesCatalogOrNull"), "method getServicesCatalogOrNull");
        check(hasMethod(c, "putServicesCatalog"), "method putServicesCatalog");
        check(hasMethod(c, "register"), "method register");
        check(hasField(c, "CLV"), "field CLV");
        check(hasField(c, "map"), "field map");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
