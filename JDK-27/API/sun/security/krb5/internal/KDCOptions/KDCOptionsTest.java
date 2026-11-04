/*
 * @test
 * @summary sun.security.krb5.internal.KDCOptions conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main KDCOptionsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KDCOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.KDCOptions", false, KDCOptionsTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.KDCOptions"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.util.KerberosFlags"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("get"), "method:get");
        check(methods.contains("parse"), "method:parse");
        check(methods.contains("set"), "method:set");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("with"), "method:with");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALLOW_POSTDATE"), "field:ALLOW_POSTDATE");
        check(fields.contains("CANONICALIZE"), "field:CANONICALIZE");
        check(fields.contains("CNAME_IN_ADDL_TKT"), "field:CNAME_IN_ADDL_TKT");
        check(fields.contains("ENC_TKT_IN_SKEY"), "field:ENC_TKT_IN_SKEY");
        check(fields.contains("FORWARDABLE"), "field:FORWARDABLE");
        check(fields.contains("FORWARDED"), "field:FORWARDED");
        check(fields.contains("POSTDATED"), "field:POSTDATED");
        check(fields.contains("PROXIABLE"), "field:PROXIABLE");
        check(fields.contains("PROXY"), "field:PROXY");
        check(fields.contains("RENEW"), "field:RENEW");
        check(fields.contains("RENEWABLE"), "field:RENEWABLE");
        check(fields.contains("RENEWABLE_OK"), "field:RENEWABLE_OK");
        check(fields.contains("RESERVED"), "field:RESERVED");
        check(fields.contains("UNUSED10"), "field:UNUSED10");
        check(fields.contains("UNUSED11"), "field:UNUSED11");
        check(fields.contains("UNUSED7"), "field:UNUSED7");
        check(fields.contains("UNUSED9"), "field:UNUSED9");
        check(fields.contains("VALIDATE"), "field:VALIDATE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
