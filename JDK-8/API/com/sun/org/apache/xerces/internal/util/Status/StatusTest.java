/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.Status conformance: enum constants and members.
 * @run main StatusTest
 */
public class StatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.Status", false, StatusTest.class.getClassLoader());
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass is Enum");
        try {
            check(c.getEnumConstants() != null, "has enum constants");
        } catch (ExceptionInInitializerError e) {
            check(c.isEnum(), "is enum (init skipped)");
        } catch (UnsatisfiedLinkError e) {
            check(c.isEnum(), "is enum (native init skipped)");
        }
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("values"), "method values");
        check(mn.contains("valueOf"), "method valueOf");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("isExceptional"), "method isExceptional");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
