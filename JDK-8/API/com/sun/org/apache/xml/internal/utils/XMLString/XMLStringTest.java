/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.XMLString conformance: interface hierarchy and members.
 * @run main XMLStringTest
 */
public class XMLStringTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.XMLString", false, XMLStringTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("dispatchCharactersEvents"), "method dispatchCharactersEvents");
        check(mn.contains("dispatchAsComment"), "method dispatchAsComment");
        check(mn.contains("fixWhiteSpace"), "method fixWhiteSpace");
        check(mn.contains("length"), "method length");
        check(mn.contains("charAt"), "method charAt");
        check(mn.contains("getChars"), "method getChars");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("equalsIgnoreCase"), "method equalsIgnoreCase");
        check(mn.contains("compareTo"), "method compareTo");
        check(mn.contains("compareToIgnoreCase"), "method compareToIgnoreCase");
        check(mn.contains("startsWith"), "method startsWith");
        check(mn.contains("endsWith"), "method endsWith");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("indexOf"), "method indexOf");
        check(mn.contains("lastIndexOf"), "method lastIndexOf");
        check(mn.contains("substring"), "method substring");
        check(mn.contains("concat"), "method concat");
        check(mn.contains("toLowerCase"), "method toLowerCase");
        check(mn.contains("toUpperCase"), "method toUpperCase");
        check(mn.contains("trim"), "method trim");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("hasString"), "method hasString");
        check(mn.contains("toDouble"), "method toDouble");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
