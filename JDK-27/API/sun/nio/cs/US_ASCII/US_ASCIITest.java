/*
 * @test
 * @summary sun.nio.cs.US_ASCII conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main US_ASCIITest
 */
public class US_ASCIITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.US_ASCII", false, US_ASCIITest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.Charset"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.nio.cs.HistoricallyNamedCharset"), "interface sun.nio.cs.HistoricallyNamedCharset");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "historicalName"), "method historicalName");
        check(hasMethod(c, "newDecoder"), "method newDecoder");
        check(hasMethod(c, "newEncoder"), "method newEncoder");
        check(hasField(c, "INSTANCE"), "field INSTANCE");
        check(hasField(c, "JLA"), "field JLA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
