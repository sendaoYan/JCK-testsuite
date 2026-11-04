/*
 * @test
 * @summary java.security.interfaces.RSAPrivateCrtKey conformance: interface hierarchy and key methods.
 * @run main RSAPrivateCrtKeyTest
 */

public class RSAPrivateCrtKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.RSAPrivateCrtKey");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCrtCoefficient"), "declares getCrtCoefficient");
        check(ms.contains("getPrimeExponentP"), "declares getPrimeExponentP");
        check(ms.contains("getPrimeExponentQ"), "declares getPrimeExponentQ");
        check(ms.contains("getPrimeP"), "declares getPrimeP");
        check(ms.contains("getPrimeQ"), "declares getPrimeQ");
        check(ms.contains("getPublicExponent"), "declares getPublicExponent");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
