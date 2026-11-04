/*
 * @test
 * @summary javax.xml.datatype.DatatypeConstants.Field conformance: datatype field.
 * @run main FieldTest
 */


public class FieldTest {
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
        check(javax.xml.datatype.DatatypeConstants.YEARS.getId() == 0, "YEARS id");
        check("YEARS".equals(javax.xml.datatype.DatatypeConstants.YEARS.toString()), "YEARS toString");
        check(javax.xml.datatype.DatatypeConstants.SECONDS.getId() == 5, "SECONDS id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
