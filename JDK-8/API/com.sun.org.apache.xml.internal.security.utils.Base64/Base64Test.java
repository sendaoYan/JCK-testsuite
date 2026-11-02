/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.Base64 conformance: class hierarchy and members.
 * @run main Base64Test
 */
public class Base64Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.Base64", false, Base64Test.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBytes"), "method getBytes");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("decodeBigIntegerFromElement"), "method decodeBigIntegerFromElement");
        check(mn.contains("decodeBigIntegerFromText"), "method decodeBigIntegerFromText");
        check(mn.contains("fillElementWithBigInteger"), "method fillElementWithBigInteger");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("encodeToElement"), "method encodeToElement");
        check(mn.contains("isWhiteSpace"), "method isWhiteSpace");
        check(mn.contains("isPad"), "method isPad");
        check(mn.contains("getBytesInternal"), "method getBytesInternal");
        check(mn.contains("decodeInternal"), "method decodeInternal");
        check(mn.contains("removeWhiteSpace"), "method removeWhiteSpace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
