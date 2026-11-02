/*
 * @test
 * @summary sun.util.calendar.Era conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main EraTest
 */
public class EraTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.Era", false, EraTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getAbbreviation"), "method getAbbreviation");
        check(hasMethod(c, "getDisplayName"), "method getDisplayName");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getSince"), "method getSince");
        check(hasMethod(c, "getSinceDate"), "method getSinceDate");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isLocalTime"), "method isLocalTime");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "abbr"), "field abbr");
        check(hasField(c, "hash"), "field hash");
        check(hasField(c, "localTime"), "field localTime");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "since"), "field since");
        check(hasField(c, "sinceDate"), "field sinceDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
