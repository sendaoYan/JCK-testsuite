/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.Encoder conformance: abstract class hierarchy and members.
 * @run main EncoderTest
 */

public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.Encoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.Encoder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("org.xml.sax.helpers.DefaultHandler".equals(c.getSuperclass().getName()), "extends org.xml.sax.helpers.DefaultHandler");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSerializer"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSerializer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_flush"), "has method _flush");
        check(mn.contains("canAddAttributeToTable"), "has method canAddAttributeToTable");
        check(mn.contains("canAddCharacterContentToTable"), "has method canAddCharacterContentToTable");
        check(mn.contains("encodeAIIBuiltInAlgorithmData"), "has method encodeAIIBuiltInAlgorithmData");
        check(mn.contains("encodeAIIObjectAlgorithmData"), "has method encodeAIIObjectAlgorithmData");
        check(mn.contains("encodeAIIOctetAlgorithmData"), "has method encodeAIIOctetAlgorithmData");
        check(mn.contains("encodeAlphabetCharacters"), "has method encodeAlphabetCharacters");
        check(mn.contains("encodeAttributeQualifiedNameOnSecondBit"), "has method encodeAttributeQualifiedNameOnSecondBit");
        check(mn.contains("encodeCIIBuiltInAlgorithmData"), "has method encodeCIIBuiltInAlgorithmData");
        check(mn.contains("encodeCIIBuiltInAlgorithmDataAsCDATA"), "has method encodeCIIBuiltInAlgorithmDataAsCDATA");
        check(mn.contains("encodeCIIObjectAlgorithmData"), "has method encodeCIIObjectAlgorithmData");
        check(mn.contains("encodeCIIOctetAlgorithmData"), "has method encodeCIIOctetAlgorithmData");
        check(mn.contains("encodeCharacterAsUtf8FourByte"), "has method encodeCharacterAsUtf8FourByte");
        check(mn.contains("encodeCharacters"), "has method encodeCharacters");
        check(mn.contains("encodeCharactersNoClone"), "has method encodeCharactersNoClone");
        check(mn.contains("encodeComment"), "has method encodeComment");
        check(mn.contains("encodeCommentNoClone"), "has method encodeCommentNoClone");
        check(mn.contains("encodeDateTimeFourBitCharacters"), "has method encodeDateTimeFourBitCharacters");
        check(mn.contains("encodeDateTimeNonIdentifyingStringOnFirstBit"), "has method encodeDateTimeNonIdentifyingStringOnFirstBit");
        check(mn.contains("encodeDocumentTermination"), "has method encodeDocumentTermination");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
