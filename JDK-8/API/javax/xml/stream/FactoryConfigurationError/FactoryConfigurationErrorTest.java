/*
 * @test
 * @summary javax.xml.stream.FactoryConfigurationError conformance: exception hierarchy and constructors.
 * @run main FactoryConfigurationErrorTest
 */


public class FactoryConfigurationErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.stream.FactoryConfigurationError.class.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
        javax.xml.stream.FactoryConfigurationError fe = new javax.xml.stream.FactoryConfigurationError();
        check(fe.getException() == null, "no exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
