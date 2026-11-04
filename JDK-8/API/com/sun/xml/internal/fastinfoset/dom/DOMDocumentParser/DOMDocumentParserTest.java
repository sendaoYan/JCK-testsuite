/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.dom.DOMDocumentParser conformance: class hierarchy and members.
 * @run main DOMDocumentParserTest
 */

public class DOMDocumentParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.dom.DOMDocumentParser", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.dom.DOMDocumentParser"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.Decoder".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.Decoder");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("appendOrCreateTextData"), "has method appendOrCreateTextData");
        check(mn.contains("convertEncodingAlgorithmDataToCharacters"), "has method convertEncodingAlgorithmDataToCharacters");
        check(mn.contains("createAttribute"), "has method createAttribute");
        check(mn.contains("createElement"), "has method createElement");
        check(mn.contains("parse"), "has method parse");
        check(mn.contains("processAIIs"), "has method processAIIs");
        check(mn.contains("processCommentII"), "has method processCommentII");
        check(mn.contains("processDII"), "has method processDII");
        check(mn.contains("processDIIOptionalProperties"), "has method processDIIOptionalProperties");
        check(mn.contains("processEII"), "has method processEII");
        check(mn.contains("processEIIWithNamespaces"), "has method processEIIWithNamespaces");
        check(mn.contains("processLiteralQualifiedName"), "has method processLiteralQualifiedName");
        check(mn.contains("processProcessingII"), "has method processProcessingII");
        check(mn.contains("processUtf8CharacterString"), "has method processUtf8CharacterString");
        check(mn.contains("resetOnError"), "has method resetOnError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
