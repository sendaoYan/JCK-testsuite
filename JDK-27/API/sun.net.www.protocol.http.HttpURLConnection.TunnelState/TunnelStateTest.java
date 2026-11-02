/*
 * @test
 * @summary sun.net.www.protocol.http.HttpURLConnection.TunnelState conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http:+open
 * @run main TunnelStateTest
 */
public class TunnelStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.HttpURLConnection$TunnelState", false, TunnelStateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "NONE"), "field NONE");
        check(hasField(c, "SETUP"), "field SETUP");
        check(hasField(c, "TUNNELING"), "field TUNNELING");
        check(hasEnumConstant(c, "NONE"), "enum NONE");
        check(hasEnumConstant(c, "SETUP"), "enum SETUP");
        check(hasEnumConstant(c, "TUNNELING"), "enum TUNNELING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
