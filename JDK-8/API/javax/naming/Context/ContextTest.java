/*
 * @test
 * @summary javax.naming.Context conformance: interface hierarchy, key method
 *          presence, and stable environment property constant values.
 * @run main ContextTest
 */

import javax.naming.Context;

public class ContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.naming.Context");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addToEnvironment"), "has method addToEnvironment");
        check(names.contains("bind"), "has method bind");
        check(names.contains("close"), "has method close");
    }

    static void testMoreMethods() {
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : Context.class.getMethods()) names.add(m.getName());
        String[] expected = {"lookup", "rebind", "unbind", "rename", "list", "listBindings",
                "createSubcontext", "destroySubcontext", "lookupLink", "getNameParser",
                "composeName", "removeFromEnvironment", "getEnvironment", "getNameInNamespace"};
        for (String e : expected) check(names.contains(e), "has method " + e);
    }

    static void testConstants() {
        check("java.naming.factory.initial".equals(Context.INITIAL_CONTEXT_FACTORY), "INITIAL_CONTEXT_FACTORY");
        check("java.naming.factory.object".equals(Context.OBJECT_FACTORIES), "OBJECT_FACTORIES");
        check("java.naming.factory.state".equals(Context.STATE_FACTORIES), "STATE_FACTORIES");
        check("java.naming.factory.url.pkgs".equals(Context.URL_PKG_PREFIXES), "URL_PKG_PREFIXES");
        check("java.naming.provider.url".equals(Context.PROVIDER_URL), "PROVIDER_URL");
        check("java.naming.dns.url".equals(Context.DNS_URL), "DNS_URL");
        check("java.naming.authoritative".equals(Context.AUTHORITATIVE), "AUTHORITATIVE");
        check("java.naming.batchsize".equals(Context.BATCHSIZE), "BATCHSIZE");
        check("java.naming.referral".equals(Context.REFERRAL), "REFERRAL");
        check("java.naming.security.protocol".equals(Context.SECURITY_PROTOCOL), "SECURITY_PROTOCOL");
        check("java.naming.security.authentication".equals(Context.SECURITY_AUTHENTICATION), "SECURITY_AUTHENTICATION");
        check("java.naming.security.principal".equals(Context.SECURITY_PRINCIPAL), "SECURITY_PRINCIPAL");
        check("java.naming.security.credentials".equals(Context.SECURITY_CREDENTIALS), "SECURITY_CREDENTIALS");
        check("java.naming.language".equals(Context.LANGUAGE), "LANGUAGE");
        check("java.naming.applet".equals(Context.APPLET), "APPLET");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testMoreMethods();
        testConstants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
