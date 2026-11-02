/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.algorithm.IEEE754FloatingPointEncodingAlgorithm conformance: abstract class hierarchy and members.
 * @run main IEEE754FloatingPointEncodingAlgorithmTest
 */

public class IEEE754FloatingPointEncodingAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.algorithm.IEEE754FloatingPointEncodingAlgorithm", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.algorithm.IEEE754FloatingPointEncodingAlgorithm"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
