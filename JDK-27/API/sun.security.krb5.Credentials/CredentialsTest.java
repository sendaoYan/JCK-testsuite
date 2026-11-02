/*
 * @test
 * @summary sun.security.krb5.Credentials conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main CredentialsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CredentialsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.Credentials", false, CredentialsTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.Credentials"), "name");
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
        check(methods.contains("acquireDefaultCreds"), "method:acquireDefaultCreds");
        check(methods.contains("acquireS4U2proxyCreds"), "method:acquireS4U2proxyCreds");
        check(methods.contains("acquireS4U2selfCreds"), "method:acquireS4U2selfCreds");
        check(methods.contains("acquireServiceCreds"), "method:acquireServiceCreds");
        check(methods.contains("acquireTGTFromCache"), "method:acquireTGTFromCache");
        check(methods.contains("checkDelegate"), "method:checkDelegate");
        check(methods.contains("getAuthTime"), "method:getAuthTime");
        check(methods.contains("getAuthzData"), "method:getAuthzData");
        check(methods.contains("getClient"), "method:getClient");
        check(methods.contains("getClientAddresses"), "method:getClientAddresses");
        check(methods.contains("getClientAlias"), "method:getClientAlias");
        check(methods.contains("getEncoded"), "method:getEncoded");
        check(methods.contains("getEndTime"), "method:getEndTime");
        check(methods.contains("getFlags"), "method:getFlags");
        check(methods.contains("getProxy"), "method:getProxy");
        check(methods.contains("getRenewTill"), "method:getRenewTill");
        check(methods.contains("getServer"), "method:getServer");
        check(methods.contains("getServerAlias"), "method:getServerAlias");
        check(methods.contains("getSessionKey"), "method:getSessionKey");
        check(methods.contains("getStartTime"), "method:getStartTime");
        check(methods.contains("getTicket"), "method:getTicket");
        check(methods.contains("getTicketFlags"), "method:getTicketFlags");
        check(methods.contains("isForwardable"), "method:isForwardable");
        check(methods.contains("isRenewable"), "method:isRenewable");
        check(methods.contains("printDebug"), "method:printDebug");
        check(methods.contains("renew"), "method:renew");
        check(methods.contains("resetDelegate"), "method:resetDelegate");
        check(methods.contains("setProxy"), "method:setProxy");
        check(methods.contains("toCCacheCreds"), "method:toCCacheCreds");
        check(methods.contains("toString"), "method:toString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("S4U2PROXY_ACCEPT_NON_FORWARDABLE"), "field:S4U2PROXY_ACCEPT_NON_FORWARDABLE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
