/*
 * @test
 * @summary sun.net.smtp.SmtpClient conformance: internal package reflection.
 * @modules java.base/sun.net.smtp:+open
 * @run main SmtpClientTest
 */
public class SmtpClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.smtp.SmtpClient", false, SmtpClientTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.TransferProtocolClient"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "closeServer"), "method closeServer");
        check(hasMethod(c, "from"), "method from");
        check(hasMethod(c, "getMailHost"), "method getMailHost");
        check(hasMethod(c, "startMessage"), "method startMessage");
        check(hasMethod(c, "to"), "method to");
        check(hasField(c, "DEFAULT_SMTP_PORT"), "field DEFAULT_SMTP_PORT");
        check(hasField(c, "mailhost"), "field mailhost");
        check(hasField(c, "message"), "field message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
