/*
 * @test
 * @summary javax.xml.datatype.DatatypeConstants conformance: datatype constants.
 * @run main DatatypeConstantsTest
 */


public class DatatypeConstantsTest {
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
        check(javax.xml.datatype.DatatypeConstants.JANUARY == 1, "JANUARY");
        check(javax.xml.datatype.DatatypeConstants.DECEMBER == 12, "DECEMBER");
        check(javax.xml.datatype.DatatypeConstants.LESSER == -1, "LESSER");
        check(javax.xml.datatype.DatatypeConstants.EQUAL == 0, "EQUAL");
        check(javax.xml.datatype.DatatypeConstants.GREATER == 1, "GREATER");
        check(javax.xml.datatype.DatatypeConstants.INDETERMINATE == 2, "INDETERMINATE");
        check(javax.xml.datatype.DatatypeConstants.MAX_TIMEZONE_OFFSET == -14 * 60, "MAX_TIMEZONE_OFFSET");
        check(javax.xml.datatype.DatatypeConstants.MIN_TIMEZONE_OFFSET == 14 * 60, "MIN_TIMEZONE_OFFSET");
        check(javax.xml.datatype.DatatypeConstants.YEARS != null, "YEARS");
        check(javax.xml.datatype.DatatypeConstants.DATETIME != null, "DATETIME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
