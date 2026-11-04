/*
 * @test
 * @summary javax.xml.transform.TransformerFactoryConfigurationError conformance: exception hierarchy and constructors.
 * @run main TransformerFactoryConfigurationErrorTest
 */


public class TransformerFactoryConfigurationErrorTest {
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
        check(javax.xml.transform.TransformerFactoryConfigurationError.class.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
        javax.xml.transform.TransformerFactoryConfigurationError fe = new javax.xml.transform.TransformerFactoryConfigurationError();
        check(fe.getException() == null, "no exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
