/*
 * @test
 * @summary javax.swing.text.DateFormatter conformance: white-box structure + black-box behavior.
 * @run main DateFormatterTest
 */

import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.text.DateFormatter;

public class DateFormatterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DateFormatter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.InternationalFormatter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        DateFormatter formatter = new DateFormatter(sdf);

        check(formatter.getFormat() == sdf, "constructor keeps format");

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.ROOT);
        cal.clear();
        cal.set(2024, Calendar.JANUARY, 15);
        Date fixed = cal.getTime();

        String text = formatter.valueToString(fixed);
        check("2024-01-15".equals(text), "valueToString fixed date -> " + text);

        Object parsed = formatter.stringToValue("2024-01-15");
        check(parsed instanceof Date, "stringToValue returns Date");
        check(fixed.equals(parsed), "stringToValue matches fixed date");

        Object roundTrip = formatter.stringToValue(formatter.valueToString(fixed));
        check(fixed.equals(roundTrip), "valueToString/stringToValue round trip");

        check("".equals(formatter.valueToString(null)), "valueToString(null) empty");

        boolean threw = false;
        try {
            formatter.stringToValue("not-a-date");
        } catch (ParseException pe) {
            threw = true;
        }
        check(threw, "illegal date -> ParseException");

        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
        formatter.setFormat(sdf2);
        check(formatter.getFormat() == sdf2, "setFormat/getFormat round trip");
        check("15/01/2024".equals(formatter.valueToString(fixed)), "valueToString after setFormat");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
