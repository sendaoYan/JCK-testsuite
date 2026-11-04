/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.RFC2253Parser conformance: class hierarchy and members.
 * @run main RFC2253ParserTest
 */
public class RFC2253ParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.RFC2253Parser", false, RFC2253ParserTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("rfc2253toXMLdsig"), "method rfc2253toXMLdsig");
        check(mn.contains("xmldsigtoRFC2253"), "method xmldsigtoRFC2253");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("parseRDN"), "method parseRDN");
        check(mn.contains("parseATAV"), "method parseATAV");
        check(mn.contains("normalizeAT"), "method normalizeAT");
        check(mn.contains("normalizeV"), "method normalizeV");
        check(mn.contains("rfctoXML"), "method rfctoXML");
        check(mn.contains("xmltoRFC"), "method xmltoRFC");
        check(mn.contains("changeLess32toRFC"), "method changeLess32toRFC");
        check(mn.contains("changeLess32toXML"), "method changeLess32toXML");
        check(mn.contains("changeWStoXML"), "method changeWStoXML");
        check(mn.contains("changeWStoRFC"), "method changeWStoRFC");
        check(mn.contains("semicolonToComma"), "method semicolonToComma");
        check(mn.contains("removeWhiteSpace"), "method removeWhiteSpace");
        check(mn.contains("removeWSandReplace"), "method removeWSandReplace");
        check(mn.contains("trim"), "method trim");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
