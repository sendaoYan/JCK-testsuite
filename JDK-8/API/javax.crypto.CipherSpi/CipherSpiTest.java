/*
 * @test
 * @summary javax.crypto.CipherSpi conformance: abstract class hierarchy and key methods.
 * @run main CipherSpiTest
 */

public class CipherSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.crypto.CipherSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineDoFinal"), "declares engineDoFinal");
        check(ms.contains("engineGetBlockSize"), "declares engineGetBlockSize");
        check(ms.contains("engineGetIV"), "declares engineGetIV");
        check(ms.contains("engineGetKeySize"), "declares engineGetKeySize");
        check(ms.contains("engineGetOutputSize"), "declares engineGetOutputSize");
        check(ms.contains("engineGetParameters"), "declares engineGetParameters");
        check(ms.contains("engineInit"), "declares engineInit");
        check(ms.contains("engineSetMode"), "declares engineSetMode");
        check(ms.contains("engineSetPadding"), "declares engineSetPadding");
        check(ms.contains("engineUnwrap"), "declares engineUnwrap");
        check(ms.contains("engineUpdate"), "declares engineUpdate");
        check(ms.contains("engineUpdateAAD"), "declares engineUpdateAAD");
        check(ms.contains("engineWrap"), "declares engineWrap");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
