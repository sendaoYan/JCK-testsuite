/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.algorithm.UUIDEncodingAlgorithm conformance: class hierarchy and members.
 * @run main UUIDEncodingAlgorithmTest
 */

public class UUIDEncodingAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.algorithm.UUIDEncodingAlgorithm", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.algorithm.UUIDEncodingAlgorithm"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.algorithm.LongEncodingAlgorithm".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.algorithm.LongEncodingAlgorithm");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertFromCharacters"), "has method convertFromCharacters");
        check(mn.contains("convertToCharacters"), "has method convertToCharacters");
        check(mn.contains("digits"), "has method digits");
        check(mn.contains("fromUUIDString"), "has method fromUUIDString");
        check(mn.contains("getPrimtiveLengthFromOctetLength"), "has method getPrimtiveLengthFromOctetLength");
        check(mn.contains("toUUIDString"), "has method toUUIDString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
