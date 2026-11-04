/*
 * @test
 * @summary javax.swing.SpinnerDateModel conformance: white-box structure + black-box behavior.
 * @run main SpinnerDateModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Date;

import javax.swing.SpinnerDateModel;

public class SpinnerDateModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SpinnerDateModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractSpinnerModel"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        Date start = cal.getTime();

        SpinnerDateModel m = new SpinnerDateModel(start, null, null, Calendar.DAY_OF_MONTH);
        check(m.getDate().equals(start), "getDate");
        check(m.getCalendarField() == Calendar.DAY_OF_MONTH, "getCalendarField");
        check(m.getValue() != null, "getValue");

        Date next = (Date) m.getNextValue();
        Date prev = (Date) m.getPreviousValue();
        check(next != null && next.after(start), "getNextValue");
        check(prev != null && prev.before(start), "getPreviousValue");

        m.setStart(prev);
        check(m.getStart() == prev, "setStart/getStart");
        m.setEnd(next);
        check(m.getEnd() == next, "setEnd/getEnd");
        m.setCalendarField(Calendar.MONTH);
        check(m.getCalendarField() == Calendar.MONTH, "setCalendarField");

        Date d2 = new Date();
        m.setValue(d2);
        check(m.getDate().equals(d2), "setValue/getDate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
