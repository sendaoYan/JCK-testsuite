/*
 * @test
 * @summary jdk.internal.event.X509CertificateEvent conformance: internal package reflection.
 * @modules java.base/jdk.internal.event:+open
 * @run main X509CertificateEventTest
 */
public class X509CertificateEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.event.X509CertificateEvent", false, X509CertificateEventTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.event.Event"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "begin"), "method begin");
        check(hasMethod(c, "commit"), "method commit");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "isTurnedOn"), "method isTurnedOn");
        check(hasMethod(c, "shouldCommit"), "method shouldCommit");
        check(hasField(c, "EVENT"), "field EVENT");
        check(hasField(c, "algorithm"), "field algorithm");
        check(hasField(c, "certificateId"), "field certificateId");
        check(hasField(c, "duration"), "field duration");
        check(hasField(c, "eventConfiguration"), "field eventConfiguration");
        check(hasField(c, "issuer"), "field issuer");
        check(hasField(c, "keyLength"), "field keyLength");
        check(hasField(c, "keyType"), "field keyType");
        check(hasField(c, "serialNumber"), "field serialNumber");
        check(hasField(c, "startTime"), "field startTime");
        check(hasField(c, "subject"), "field subject");
        check(hasField(c, "validFrom"), "field validFrom");
        check(hasField(c, "validUntil"), "field validUntil");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
