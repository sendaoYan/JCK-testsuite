/*
 * @test
 * @summary sun.security.krb5.internal.ccache.Credentials conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ccache:+open
 * @run main CredentialsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CredentialsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ccache.Credentials", false, CredentialsTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ccache.Credentials"), "name");
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
        check(methods.contains("getAuthTime"), "method:getAuthTime");
        check(methods.contains("getClientPrincipal"), "method:getClientPrincipal");
        check(methods.contains("getEType"), "method:getEType");
        check(methods.contains("getEndTime"), "method:getEndTime");
        check(methods.contains("getKey"), "method:getKey");
        check(methods.contains("getRenewTill"), "method:getRenewTill");
        check(methods.contains("getServicePrincipal"), "method:getServicePrincipal");
        check(methods.contains("getServicePrincipal2"), "method:getServicePrincipal2");
        check(methods.contains("getStartTime"), "method:getStartTime");
        check(methods.contains("getTicket"), "method:getTicket");
        check(methods.contains("getTicketFlags"), "method:getTicketFlags");
        check(methods.contains("getTktEType"), "method:getTktEType");
        check(methods.contains("getTktEType2"), "method:getTktEType2");
        check(methods.contains("isValid"), "method:isValid");
        check(methods.contains("setKrbCreds"), "method:setKrbCreds");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("isEncInSKey"), "field:isEncInSKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
