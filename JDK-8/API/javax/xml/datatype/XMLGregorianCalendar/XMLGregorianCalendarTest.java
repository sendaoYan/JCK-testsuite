/*
 * @test
 * @summary javax.xml.datatype.XMLGregorianCalendar conformance: xml gregorian calendar behavior.
 * @run main XMLGregorianCalendarTest
 */


public class XMLGregorianCalendarTest {
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
        javax.xml.datatype.XMLGregorianCalendar xc = df.newXMLGregorianCalendar();
        xc.setYear(2001);
        xc.setMonth(10);
        xc.setDay(26);
        check(xc.getYear() == 2001, "year");
        check(xc.getMonth() == 10, "month");
        check(xc.getDay() == 26, "day");
        check(xc.isValid(), "valid");
        check(xc.toXMLFormat() != null, "xml format non-null");
        javax.xml.datatype.XMLGregorianCalendar xc2 = df.newXMLGregorianCalendar("2001-10-26");
        check(xc2.getYear() == 2001, "parse year");
        javax.xml.datatype.XMLGregorianCalendar clone = (javax.xml.datatype.XMLGregorianCalendar) xc.clone();
        check(clone.getYear() == 2001, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
