/*
 * @test
 * @summary com.sun.security.ntlm.NTLMException conformance: internal package reflection.
 * @modules java.base/com.sun.security.ntlm:+open
 * @run main NTLMExceptionTest
 */
public class NTLMExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.ntlm.NTLMException", false, NTLMExceptionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.GeneralSecurityException"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "errorCode"), "method errorCode");
        check(hasField(c, "AUTH_FAILED"), "field AUTH_FAILED");
        check(hasField(c, "BAD_VERSION"), "field BAD_VERSION");
        check(hasField(c, "INVALID_INPUT"), "field INVALID_INPUT");
        check(hasField(c, "NO_DOMAIN_INFO"), "field NO_DOMAIN_INFO");
        check(hasField(c, "PACKET_READ_ERROR"), "field PACKET_READ_ERROR");
        check(hasField(c, "PROTOCOL"), "field PROTOCOL");
        check(hasField(c, "USER_UNKNOWN"), "field USER_UNKNOWN");
        check(hasField(c, "errorCode"), "field errorCode");
        check(hasField(c, "serialVersionUID"), "field serialVersionUID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
