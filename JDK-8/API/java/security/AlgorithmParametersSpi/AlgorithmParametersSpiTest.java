/*
 * @test
 * @summary java.security.AlgorithmParametersSpi conformance: abstract class hierarchy and key methods.
 * @run main AlgorithmParametersSpiTest
 */

public class AlgorithmParametersSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmParametersSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineGetEncoded"), "declares engineGetEncoded");
        check(ms.contains("engineGetParameterSpec"), "declares engineGetParameterSpec");
        check(ms.contains("engineInit"), "declares engineInit");
        check(ms.contains("engineToString"), "declares engineToString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
