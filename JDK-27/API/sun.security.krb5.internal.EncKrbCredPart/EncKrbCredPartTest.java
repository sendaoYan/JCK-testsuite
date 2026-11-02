/*
 * @test
 * @summary sun.security.krb5.internal.EncKrbCredPart conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main EncKrbCredPartTest
 */
import java.lang.reflect.*;
import java.util.*;
public class EncKrbCredPartTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.EncKrbCredPart", false, EncKrbCredPartTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.EncKrbCredPart"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asn1Encode"), "method:asn1Encode");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ticketInfo"), "field:ticketInfo");
        check(fields.contains("timeStamp"), "field:timeStamp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
