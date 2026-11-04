/*
 * @test
 * @summary sun.util.BuddhistCalendar conformance: internal package reflection.
 * @modules java.base/sun.util:+open
 * @run main BuddhistCalendarTest
 */
public class BuddhistCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.BuddhistCalendar", false, BuddhistCalendarTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.GregorianCalendar"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getActualMaximum"), "method getActualMaximum");
        check(hasMethod(c, "getCalendarType"), "method getCalendarType");
        check(hasMethod(c, "getDisplayName"), "method getDisplayName");
        check(hasMethod(c, "getDisplayNames"), "method getDisplayNames");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "roll"), "method roll");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "BUDDHIST_YEAR_OFFSET"), "field BUDDHIST_YEAR_OFFSET");
        check(hasField(c, "serialVersionUID"), "field serialVersionUID");
        check(hasField(c, "yearOffset"), "field yearOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
