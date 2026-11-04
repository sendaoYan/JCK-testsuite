/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer conformance: class hierarchy and members.
 * @run main SAXDocumentSerializerTest
 */

public class SAXDocumentSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.Encoder".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.Encoder");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetWriter"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("alphabetCharacters"), "has method alphabetCharacters");
        check(mn.contains("booleans"), "has method booleans");
        check(mn.contains("bytes"), "has method bytes");
        check(mn.contains("characters"), "has method characters");
        check(mn.contains("comment"), "has method comment");
        check(mn.contains("countAttributes"), "has method countAttributes");
        check(mn.contains("dateTimeCharacters"), "has method dateTimeCharacters");
        check(mn.contains("doubles"), "has method doubles");
        check(mn.contains("encodeAttribute"), "has method encodeAttribute");
        check(mn.contains("encodeAttributes"), "has method encodeAttributes");
        check(mn.contains("encodeElement"), "has method encodeElement");
        check(mn.contains("endCDATA"), "has method endCDATA");
        check(mn.contains("endDTD"), "has method endDTD");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endEntity"), "has method endEntity");
        check(mn.contains("floats"), "has method floats");
        check(mn.contains("ignorableWhitespace"), "has method ignorableWhitespace");
        check(mn.contains("ints"), "has method ints");
        check(mn.contains("longs"), "has method longs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
