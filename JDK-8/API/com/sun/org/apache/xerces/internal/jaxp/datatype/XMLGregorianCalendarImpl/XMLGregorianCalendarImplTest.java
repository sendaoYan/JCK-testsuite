/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl conformance: class hierarchy and members.
 * @run main XMLGregorianCalendarImplTest
 */
public class XMLGregorianCalendarImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl", false, XMLGregorianCalendarImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("javax.xml.datatype.XMLGregorianCalendar"), "superclass javax.xml.datatype.XMLGregorianCalendar");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        check(hasInterface(c, "java.lang.Cloneable"), "has interface java.lang.Cloneable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createDateTime"), "method createDateTime");
        check(mn.contains("createDate"), "method createDate");
        check(mn.contains("createTime"), "method createTime");
        check(mn.contains("getEon"), "method getEon");
        check(mn.contains("getYear"), "method getYear");
        check(mn.contains("getEonAndYear"), "method getEonAndYear");
        check(mn.contains("getMonth"), "method getMonth");
        check(mn.contains("getDay"), "method getDay");
        check(mn.contains("getTimezone"), "method getTimezone");
        check(mn.contains("getHour"), "method getHour");
        check(mn.contains("getMinute"), "method getMinute");
        check(mn.contains("getSecond"), "method getSecond");
        check(mn.contains("getMillisecond"), "method getMillisecond");
        check(mn.contains("getFractionalSecond"), "method getFractionalSecond");
        check(mn.contains("setYear"), "method setYear");
        check(mn.contains("setMonth"), "method setMonth");
        check(mn.contains("setDay"), "method setDay");
        check(mn.contains("setTimezone"), "method setTimezone");
        check(mn.contains("setTime"), "method setTime");
        check(mn.contains("setHour"), "method setHour");
        check(mn.contains("setMinute"), "method setMinute");
        check(mn.contains("setSecond"), "method setSecond");
        check(mn.contains("compare"), "method compare");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("toXMLFormat"), "method toXMLFormat");
        check(mn.contains("getXMLSchemaType"), "method getXMLSchemaType");
        check(mn.contains("isValid"), "method isValid");
        check(mn.contains("add"), "method add");
        check(mn.contains("toGregorianCalendar"), "method toGregorianCalendar");
        check(mn.contains("getTimeZone"), "method getTimeZone");
        check(mn.contains("clone"), "method clone");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("setMillisecond"), "method setMillisecond");
        check(mn.contains("setFractionalSecond"), "method setFractionalSecond");
        check(mn.contains("sanitize"), "method sanitize");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("access$200"), "method access$200");
        check(mn.contains("access$300"), "method access$300");
        check(mn.contains("access$400"), "method access$400");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
