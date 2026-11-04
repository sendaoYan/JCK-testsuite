/*
 * @test
 * @summary sun.text.resources.cldr.ext.FormatData_de_AT conformance: internal package reflection.
 * @modules jdk.localedata/sun.text.resources.cldr.ext:+open
 * @run main FormatData_de_ATTest
 */
public class FormatData_de_ATTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.resources.cldr.ext.FormatData_de_AT", false, FormatData_de_ATTest.class.getClassLoader());
        check(c.getName().equals("sun.text.resources.cldr.ext.FormatData_de_AT"), "name");
        check(c.isInterface() == false && c.isEnum() == false && c.isAnnotation() == false && java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false && java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "kind");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces");
        java.util.Set<String> ms = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mm = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mm) || java.lang.reflect.Modifier.isProtected(mm)) ms.add(m.getName());
        }
        check(ms.contains("getContents"), "method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
}
