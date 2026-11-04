/*
 * @test
 * @summary sun.security.jgss.GSSUtil conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSUtilTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.GSSUtil", false, GSSUtilTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.GSSUtil"), "name");
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
        check(methods.contains("createOid"), "method:createOid");
        check(methods.contains("getMechStr"), "method:getMechStr");
        check(methods.contains("getSubject"), "method:getSubject");
        check(methods.contains("isKerberosMech"), "method:isKerberosMech");
        check(methods.contains("isSpNegoMech"), "method:isSpNegoMech");
        check(methods.contains("login"), "method:login");
        check(methods.contains("searchSubject"), "method:searchSubject");
        check(methods.contains("useMSInterop"), "method:useMSInterop");
        check(methods.contains("useSubjectCredsOnly"), "method:useSubjectCredsOnly");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("GSS_KRB5_MECH_OID"), "field:GSS_KRB5_MECH_OID");
        check(fields.contains("GSS_KRB5_MECH_OID2"), "field:GSS_KRB5_MECH_OID2");
        check(fields.contains("GSS_KRB5_MECH_OID_MS"), "field:GSS_KRB5_MECH_OID_MS");
        check(fields.contains("GSS_SPNEGO_MECH_OID"), "field:GSS_SPNEGO_MECH_OID");
        check(fields.contains("NT_GSS_KRB5_PRINCIPAL"), "field:NT_GSS_KRB5_PRINCIPAL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
