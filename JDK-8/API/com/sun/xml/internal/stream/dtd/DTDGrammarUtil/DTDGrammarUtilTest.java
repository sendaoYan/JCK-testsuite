/*
 * @test
 * @summary com.sun.xml.internal.stream.dtd.DTDGrammarUtil conformance: class hierarchy and members.
 * @run main DTDGrammarUtilTest
 */

public class DTDGrammarUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.dtd.DTDGrammarUtil", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.dtd.DTDGrammarUtil"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addDTDDefaultAttrs"), "has method addDTDDefaultAttrs");
        check(mn.contains("endCDATA"), "has method endCDATA");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("ensureStackCapacity"), "has method ensureStackCapacity");
        check(mn.contains("getAttributeTypeName"), "has method getAttributeTypeName");
        check(mn.contains("handleEndElement"), "has method handleEndElement");
        check(mn.contains("handleStartElement"), "has method handleStartElement");
        check(mn.contains("isIgnorableWhiteSpace"), "has method isIgnorableWhiteSpace");
        check(mn.contains("isInElementContent"), "has method isInElementContent");
        check(mn.contains("normalizeAttrValue"), "has method normalizeAttrValue");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("startCDATA"), "has method startCDATA");
        check(mn.contains("startElement"), "has method startElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
