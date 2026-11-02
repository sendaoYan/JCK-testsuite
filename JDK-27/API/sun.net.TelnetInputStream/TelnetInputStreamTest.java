/*
 * @test
 * @summary sun.net.TelnetInputStream conformance: internal package reflection.
 * @modules java.base/sun.net:+open
 * @run main TelnetInputStreamTest
 */
public class TelnetInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.TelnetInputStream", false, TelnetInputStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.FilterInputStream"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "read"), "method read");
        check(hasMethod(c, "setStickyCRLF"), "method setStickyCRLF");
        check(hasField(c, "binaryMode"), "field binaryMode");
        check(hasField(c, "seenCR"), "field seenCR");
        check(hasField(c, "stickyCRLF"), "field stickyCRLF");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
