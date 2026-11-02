/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.algorithm.ShortEncodingAlgorithm conformance: class hierarchy and members.
 * @run main ShortEncodingAlgorithmTest
 */

public class ShortEncodingAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.algorithm.ShortEncodingAlgorithm", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.algorithm.ShortEncodingAlgorithm"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.algorithm.IntegerEncodingAlgorithm".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.algorithm.IntegerEncodingAlgorithm");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertFromCharacters"), "has method convertFromCharacters");
        check(mn.contains("convertToCharacters"), "has method convertToCharacters");
        check(mn.contains("convertToCharactersFromShortArray"), "has method convertToCharactersFromShortArray");
        check(mn.contains("decodeFromBytes"), "has method decodeFromBytes");
        check(mn.contains("decodeFromBytesToShortArray"), "has method decodeFromBytesToShortArray");
        check(mn.contains("decodeFromInputStream"), "has method decodeFromInputStream");
        check(mn.contains("decodeFromInputStreamToShortArray"), "has method decodeFromInputStreamToShortArray");
        check(mn.contains("encodeToBytes"), "has method encodeToBytes");
        check(mn.contains("encodeToBytesFromShortArray"), "has method encodeToBytesFromShortArray");
        check(mn.contains("encodeToOutputStream"), "has method encodeToOutputStream");
        check(mn.contains("encodeToOutputStreamFromShortArray"), "has method encodeToOutputStreamFromShortArray");
        check(mn.contains("generateArrayFromList"), "has method generateArrayFromList");
        check(mn.contains("getOctetLengthFromPrimitiveLength"), "has method getOctetLengthFromPrimitiveLength");
        check(mn.contains("getPrimtiveLengthFromOctetLength"), "has method getPrimtiveLengthFromOctetLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
