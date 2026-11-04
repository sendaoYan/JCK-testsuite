/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.Decoder conformance: abstract class hierarchy and members.
 * @run main DecoderTest
 */

public class DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.Decoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.Decoder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetParser"), "has interface com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetParser");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_isFastInfosetDocument"), "has method _isFastInfosetDocument");
        check(mn.contains("arrayEquals"), "has method arrayEquals");
        check(mn.contains("closeIfRequired"), "has method closeIfRequired");
        check(mn.contains("compareCharsWithCharBufferFromEndToStart"), "has method compareCharsWithCharBufferFromEndToStart");
        check(mn.contains("createQualifiedNameString"), "has method createQualifiedNameString");
        check(mn.contains("decodeAdditionalData"), "has method decodeAdditionalData");
        check(mn.contains("decodeAlphabetOctetsAsCharBuffer"), "has method decodeAlphabetOctetsAsCharBuffer");
        check(mn.contains("decodeCharacterEncodingScheme"), "has method decodeCharacterEncodingScheme");
        check(mn.contains("decodeDII"), "has method decodeDII");
        check(mn.contains("decodeEIIIndexLarge"), "has method decodeEIIIndexLarge");
        check(mn.contains("decodeEIIIndexMedium"), "has method decodeEIIIndexMedium");
        check(mn.contains("decodeExternalVocabularyURI"), "has method decodeExternalVocabularyURI");
        check(mn.contains("decodeFourBitAlphabetOctetsAsCharBuffer"), "has method decodeFourBitAlphabetOctetsAsCharBuffer");
        check(mn.contains("decodeHeader"), "has method decodeHeader");
        check(mn.contains("decodeIdentifyingNonEmptyStringIndexOnFirstBitAsNamespaceName"), "has method decodeIdentifyingNonEmptyStringIndexOnFirstBitAsNamespaceName");
        check(mn.contains("decodeIdentifyingNonEmptyStringIndexOnFirstBitAsPrefix"), "has method decodeIdentifyingNonEmptyStringIndexOnFirstBitAsPrefix");
        check(mn.contains("decodeIdentifyingNonEmptyStringOnFirstBit"), "has method decodeIdentifyingNonEmptyStringOnFirstBit");
        check(mn.contains("decodeIdentifyingNonEmptyStringOnFirstBitAsNamespaceName"), "has method decodeIdentifyingNonEmptyStringOnFirstBitAsNamespaceName");
        check(mn.contains("decodeIdentifyingNonEmptyStringOnFirstBitAsPrefix"), "has method decodeIdentifyingNonEmptyStringOnFirstBitAsPrefix");
        check(mn.contains("decodeInitialVocabulary"), "has method decodeInitialVocabulary");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
