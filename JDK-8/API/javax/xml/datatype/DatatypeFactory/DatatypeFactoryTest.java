/*
 * @test
 * @summary javax.xml.datatype.DatatypeFactory conformance: datatype factory.
 * @run main DatatypeFactoryTest
 */


public class DatatypeFactoryTest {
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
        check(df != null, "factory non-null");
        javax.xml.datatype.Duration d = df.newDuration("P1Y2M3DT4H5M6S");
        check(d.getYears() == 1 && d.getMonths() == 2 && d.getDays() == 3, "duration date fields");
        check(d.getHours() == 4 && d.getMinutes() == 5 && d.getSeconds() == 6, "duration time fields");
        javax.xml.datatype.XMLGregorianCalendar xc = df.newXMLGregorianCalendar();
        check(xc != null, "xmlgregoriancalendar non-null");
        javax.xml.datatype.XMLGregorianCalendar xc2 = df.newXMLGregorianCalendar("2001-10-26T21:32:52Z");
        check(xc2.getYear() == 2001 && xc2.getMonth() == 10 && xc2.getDay() == 26, "parsed date fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
