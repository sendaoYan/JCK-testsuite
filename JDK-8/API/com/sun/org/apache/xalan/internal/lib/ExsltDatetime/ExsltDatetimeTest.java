/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.ExsltDatetime conformance: reflection.
 * @run main ExsltDatetimeTest
 */

public class ExsltDatetimeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.ExsltDatetime", false, ExsltDatetimeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ExsltDatetime"), "simple name ExsltDatetime");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "dateTime"), "method dateTime");
        check(has(c, "date"), "method date");
        check(has(c, "time"), "method time");
        check(has(c, "year"), "method year");
        check(has(c, "monthInYear"), "method monthInYear");
        check(has(c, "weekInYear"), "method weekInYear");
        check(has(c, "dayInYear"), "method dayInYear");
        check(has(c, "dayInMonth"), "method dayInMonth");
        check(has(c, "dayOfWeekInMonth"), "method dayOfWeekInMonth");
        check(has(c, "dayInWeek"), "method dayInWeek");
        check(has(c, "hourInDay"), "method hourInDay");
        check(has(c, "minuteInHour"), "method minuteInHour");
        check(has(c, "secondInMinute"), "method secondInMinute");
        check(has(c, "leapYear"), "method leapYear");
        check(has(c, "monthName"), "method monthName");
        check(has(c, "monthAbbreviation"), "method monthAbbreviation");
        check(has(c, "dayName"), "method dayName");
        check(has(c, "dayAbbreviation"), "method dayAbbreviation");
        check(has(c, "formatDate"), "method formatDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
