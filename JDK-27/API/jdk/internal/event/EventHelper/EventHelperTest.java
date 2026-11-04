/*
 * @test
 * @summary jdk.internal.event.EventHelper conformance: internal package reflection.
 * @modules java.base/jdk.internal.event:+open
 * @run main EventHelperTest
 */
public class EventHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.event.EventHelper", false, EventHelperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isLoggingSecurity"), "method isLoggingSecurity");
        check(hasMethod(c, "logSecurityPropertyEvent"), "method logSecurityPropertyEvent");
        check(hasMethod(c, "logTLSHandshakeEvent"), "method logTLSHandshakeEvent");
        check(hasMethod(c, "logX509CertificateEvent"), "method logX509CertificateEvent");
        check(hasMethod(c, "logX509ValidationEvent"), "method logX509ValidationEvent");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "JUJA"), "field JUJA");
        check(hasField(c, "LOGGER_HANDLE"), "field LOGGER_HANDLE");
        check(hasField(c, "LOG_LEVEL"), "field LOG_LEVEL");
        check(hasField(c, "SECURITY_LOGGER_NAME"), "field SECURITY_LOGGER_NAME");
        check(hasField(c, "loggingSecurity"), "field loggingSecurity");
        check(hasField(c, "securityLogger"), "field securityLogger");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
