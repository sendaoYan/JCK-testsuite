/*
 * @test
 * @summary sun.security.krb5.internal.KerberosTime conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main KerberosTimeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KerberosTimeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.KerberosTime", false, KerberosTimeTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.KerberosTime"), "name");
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
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getDefaultSkew"), "method:getDefaultSkew");
        check(methods.contains("getMicroSeconds"), "method:getMicroSeconds");
        check(methods.contains("getSeconds"), "method:getSeconds");
        check(methods.contains("getTime"), "method:getTime");
        check(methods.contains("greaterThan"), "method:greaterThan");
        check(methods.contains("greaterThanWRTClockSkew"), "method:greaterThanWRTClockSkew");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("inClockSkew"), "method:inClockSkew");
        check(methods.contains("isZero"), "method:isZero");
        check(methods.contains("now"), "method:now");
        check(methods.contains("parse"), "method:parse");
        check(methods.contains("toDate"), "method:toDate");
        check(methods.contains("toGeneralizedTimeString"), "method:toGeneralizedTimeString");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("withMicroSeconds"), "method:withMicroSeconds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
