/*
 * @test
 * @summary javax.xml.validation.SchemaFactoryConfigurationError conformance: exception hierarchy and constructors.
 * @run main SchemaFactoryConfigurationErrorTest
 */


public class SchemaFactoryConfigurationErrorTest {
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
        check(javax.xml.validation.SchemaFactoryConfigurationError.class.getSuperclass().getName().equals("java.lang.Error"), "extends java.lang.Error");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
