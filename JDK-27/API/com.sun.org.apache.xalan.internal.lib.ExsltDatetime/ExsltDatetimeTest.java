/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.lib.ExsltDatetime conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.lib:+open
 * @run main ExsltDatetimeTest
 */
public class ExsltDatetimeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.lib.ExsltDatetime", false, ExsltDatetimeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.lib.ExsltDatetime"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 19, "method-count");
        check(md.contains("date"), "method-date");
        check(md.contains("dateTime"), "method-dateTime");
        check(md.contains("dayAbbreviation"), "method-dayAbbreviation");
        check(md.contains("dayInMonth"), "method-dayInMonth");
        check(md.contains("dayInWeek"), "method-dayInWeek");
        check(md.contains("dayInYear"), "method-dayInYear");
        check(md.contains("dayName"), "method-dayName");
        check(md.contains("dayOfWeekInMonth"), "method-dayOfWeekInMonth");
        check(md.contains("formatDate"), "method-formatDate");
        check(md.contains("hourInDay"), "method-hourInDay");
        check(md.contains("leapYear"), "method-leapYear");
        check(md.contains("minuteInHour"), "method-minuteInHour");
        check(md.contains("monthAbbreviation"), "method-monthAbbreviation");
        check(md.contains("monthInYear"), "method-monthInYear");
        check(md.contains("monthName"), "method-monthName");
        check(md.contains("secondInMinute"), "method-secondInMinute");
        check(md.contains("time"), "method-time");
        check(md.contains("weekInYear"), "method-weekInYear");
        check(md.contains("year"), "method-year");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
