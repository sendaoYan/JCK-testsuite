/*
 * @test
 * @summary javax.xml.datatype.Duration conformance: duration value behavior.
 * @run main DurationTest
 */


public class DurationTest {
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
        javax.xml.datatype.DatatypeFactory df = javax.xml.datatype.DatatypeFactory.newInstance();
        javax.xml.datatype.Duration d = df.newDuration("P1Y2M3DT4H5M6S");
        check(d.getSign() == 1, "sign positive");
        check(d.getYears() == 1, "years");
        check(d.getMonths() == 2, "months");
        check(d.getDays() == 3, "days");
        check(d.getHours() == 4, "hours");
        check(d.getMinutes() == 5, "minutes");
        check(d.getSeconds() == 6, "seconds");
        check(d.isSet(javax.xml.datatype.DatatypeConstants.YEARS), "years set");
        check(d.getXMLSchemaType() != null, "schema type non-null");
        javax.xml.datatype.Duration y = df.newDuration("P1Y");
        check(y.multiply(2).getYears() == 2, "multiply");
        check(y.compare(df.newDuration("P1Y")) == 0, "compare equal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
