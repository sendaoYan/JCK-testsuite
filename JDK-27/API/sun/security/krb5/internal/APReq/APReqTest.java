/*
 * @test
 * @summary sun.security.krb5.internal.APReq conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main APReqTest
 */
import java.lang.reflect.*;
import java.util.*;
public class APReqTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.APReq", false, APReqTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.APReq"), "name");
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
        check(fields.contains("apOptions"), "field:apOptions");
        check(fields.contains("authenticator"), "field:authenticator");
        check(fields.contains("msgType"), "field:msgType");
        check(fields.contains("pvno"), "field:pvno");
        check(fields.contains("ticket"), "field:ticket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
