/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.sax.SAXBufferProcessor conformance: class hierarchy and members.
 * @run main SAXBufferProcessorTest
 */

public class SAXBufferProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.sax.SAXBufferProcessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.sax.SAXBufferProcessor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.AbstractProcessor".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.AbstractProcessor");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("org.xml.sax.XMLReader"), "has interface org.xml.sax.XMLReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("cacheNamespacePrefix"), "has method cacheNamespacePrefix");
        check(mn.contains("cacheNamespacePrefixIndex"), "has method cacheNamespacePrefixIndex");
        check(mn.contains("cacheNamespacePrefixStartingIndex"), "has method cacheNamespacePrefixStartingIndex");
        check(mn.contains("fixNull"), "has method fixNull");
        check(mn.contains("getContentHandler"), "has method getContentHandler");
        check(mn.contains("getDTDHandler"), "has method getDTDHandler");
        check(mn.contains("getEntityResolver"), "has method getEntityResolver");
        check(mn.contains("getErrorHandler"), "has method getErrorHandler");
        check(mn.contains("getFeature"), "has method getFeature");
        check(mn.contains("getLexicalHandler"), "has method getLexicalHandler");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("isInscope"), "has method isInscope");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("process"), "has method process");
        check(mn.contains("processAttributes"), "has method processAttributes");
        check(mn.contains("processComment"), "has method processComment");
        check(mn.contains("processCommentAsCharArrayCopy"), "has method processCommentAsCharArrayCopy");
        check(mn.contains("processCommentAsCharArrayMedium"), "has method processCommentAsCharArrayMedium");
        check(mn.contains("processCommentAsCharArraySmall"), "has method processCommentAsCharArraySmall");
        check(mn.contains("processDocument"), "has method processDocument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
