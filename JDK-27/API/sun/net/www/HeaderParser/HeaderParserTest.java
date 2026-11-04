/*
 * @test
 * @summary sun.net.www.HeaderParser conformance: internal package reflection.
 * @modules java.base/sun.net.www:+open
 * @run main HeaderParserTest
 */
public class HeaderParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.HeaderParser", false, HeaderParserTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "findInt"), "method findInt");
        check(hasMethod(c, "findKey"), "method findKey");
        check(hasMethod(c, "findValue"), "method findValue");
        check(hasMethod(c, "keys"), "method keys");
        check(hasMethod(c, "subsequence"), "method subsequence");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "asize"), "field asize");
        check(hasField(c, "nkeys"), "field nkeys");
        check(hasField(c, "raw"), "field raw");
        check(hasField(c, "tab"), "field tab");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
