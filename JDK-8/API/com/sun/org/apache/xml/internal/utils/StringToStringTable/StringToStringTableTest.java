/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.StringToStringTable conformance: class hierarchy and members.
 * @run main StringToStringTableTest
 */
public class StringToStringTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.StringToStringTable", false, StringToStringTableTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("put"), "method put");
        check(mn.contains("get"), "method get");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("getIgnoreCase"), "method getIgnoreCase");
        check(mn.contains("getByValue"), "method getByValue");
        check(mn.contains("elementAt"), "method elementAt");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("containsValue"), "method containsValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
