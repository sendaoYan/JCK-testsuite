/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.tools.VocabularyGenerator conformance: class hierarchy and members.
 * @run main VocabularyGeneratorTest
 */

public class VocabularyGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.tools.VocabularyGenerator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.tools.VocabularyGenerator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("org.xml.sax.helpers.DefaultHandler".equals(c.getSuperclass().getName()), "extends org.xml.sax.helpers.DefaultHandler");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("org.xml.sax.ext.LexicalHandler"), "has interface org.xml.sax.ext.LexicalHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addToCharArrayTable"), "has method addToCharArrayTable");
        check(mn.contains("addToNameTable"), "has method addToNameTable");
        check(mn.contains("addToTable"), "has method addToTable");
        check(mn.contains("characters"), "has method characters");
        check(mn.contains("comment"), "has method comment");
        check(mn.contains("endCDATA"), "has method endCDATA");
        check(mn.contains("endDTD"), "has method endDTD");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endEntity"), "has method endEntity");
        check(mn.contains("endPrefixMapping"), "has method endPrefixMapping");
        check(mn.contains("getAttributeValueSizeLimit"), "has method getAttributeValueSizeLimit");
        check(mn.contains("getCharacterContentChunkSizeLimit"), "has method getCharacterContentChunkSizeLimit");
        check(mn.contains("getPrefixFromQualifiedName"), "has method getPrefixFromQualifiedName");
        check(mn.contains("getVocabulary"), "has method getVocabulary");
        check(mn.contains("ignorableWhitespace"), "has method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "has method processingInstruction");
        check(mn.contains("setAttributeValueSizeLimit"), "has method setAttributeValueSizeLimit");
        check(mn.contains("setCharacterContentChunkSizeLimit"), "has method setCharacterContentChunkSizeLimit");
        check(mn.contains("setDocumentLocator"), "has method setDocumentLocator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
