/*
 * @test
 * @summary sun.security.krb5.internal.KDCReqBody conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main KDCReqBodyTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KDCReqBodyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.KDCReqBody", false, KDCReqBodyTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.KDCReqBody"), "name");
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
        check(methods.contains("getNonce"), "method:getNonce");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("addresses"), "field:addresses");
        check(fields.contains("cname"), "field:cname");
        check(fields.contains("eType"), "field:eType");
        check(fields.contains("from"), "field:from");
        check(fields.contains("kdcOptions"), "field:kdcOptions");
        check(fields.contains("rtime"), "field:rtime");
        check(fields.contains("sname"), "field:sname");
        check(fields.contains("till"), "field:till");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
