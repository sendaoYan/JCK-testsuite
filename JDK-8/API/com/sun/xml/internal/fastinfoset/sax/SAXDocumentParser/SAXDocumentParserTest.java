/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.sax.SAXDocumentParser conformance: class hierarchy and members.
 * @run main SAXDocumentParserTest
 */

public class SAXDocumentParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.sax.SAXDocumentParser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.sax.SAXDocumentParser"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.Decoder".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.Decoder");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetReader"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.sax.FastInfosetReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getContentHandler"), "has method getContentHandler");
        check(mn.contains("getDTDHandler"), "has method getDTDHandler");
        check(mn.contains("getDeclHandler"), "has method getDeclHandler");
        check(mn.contains("getEncodingAlgorithmContentHandler"), "has method getEncodingAlgorithmContentHandler");
        check(mn.contains("getEntityResolver"), "has method getEntityResolver");
        check(mn.contains("getErrorHandler"), "has method getErrorHandler");
        check(mn.contains("getFeature"), "has method getFeature");
        check(mn.contains("getLexicalHandler"), "has method getLexicalHandler");
        check(mn.contains("getPrimitiveTypeContentHandler"), "has method getPrimitiveTypeContentHandler");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("processAIIEncodingAlgorithm"), "has method processAIIEncodingAlgorithm");
        check(mn.contains("processAIIs"), "has method processAIIs");
        check(mn.contains("processBuiltInEncodingAlgorithmAsCharacters"), "has method processBuiltInEncodingAlgorithmAsCharacters");
        check(mn.contains("processBuiltInEncodingAlgorithmAsObject"), "has method processBuiltInEncodingAlgorithmAsObject");
        check(mn.contains("processCIIBuiltInEncodingAlgorithmAsPrimitive"), "has method processCIIBuiltInEncodingAlgorithmAsPrimitive");
        check(mn.contains("processCIIEncodingAlgorithm"), "has method processCIIEncodingAlgorithm");
        check(mn.contains("processCommentII"), "has method processCommentII");
        check(mn.contains("processDII"), "has method processDII");
        check(mn.contains("processDIIFragment"), "has method processDIIFragment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
