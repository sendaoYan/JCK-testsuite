/*
 * @test
 * @summary sun.net.www.protocol.http.AuthenticationHeader conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http:+open
 * @run main AuthenticationHeaderTest
 */
public class AuthenticationHeaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.AuthenticationHeader", false, AuthenticationHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getHttpCallerInfo"), "method getHttpCallerInfo");
        check(hasMethod(c, "headerParser"), "method headerParser");
        check(hasMethod(c, "isPresent"), "method isPresent");
        check(hasMethod(c, "raw"), "method raw");
        check(hasMethod(c, "scheme"), "method scheme");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "authPref"), "field authPref");
        check(hasField(c, "dontUseNegotiate"), "field dontUseNegotiate");
        check(hasField(c, "hci"), "field hci");
        check(hasField(c, "hdrname"), "field hdrname");
        check(hasField(c, "preferred"), "field preferred");
        check(hasField(c, "preferred_r"), "field preferred_r");
        check(hasField(c, "rsp"), "field rsp");
        check(hasField(c, "schemes"), "field schemes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
