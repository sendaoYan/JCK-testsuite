/*
 * @test
 * @summary sun.net.www.protocol.http.ntlm.NTLMAuthenticationCallback conformance: internal package reflection.
 * @modules java.base/sun.net.www.protocol.http.ntlm:+open
 * @run main NTLMAuthenticationCallbackTest
 */
public class NTLMAuthenticationCallbackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.ntlm.NTLMAuthenticationCallback", false, NTLMAuthenticationCallbackTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getNTLMAuthenticationCallback"), "method getNTLMAuthenticationCallback");
        check(hasMethod(c, "isTrustedSite"), "method isTrustedSite");
        check(hasMethod(c, "setNTLMAuthenticationCallback"), "method setNTLMAuthenticationCallback");
        check(hasField(c, "callback"), "field callback");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
