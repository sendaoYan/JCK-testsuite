/*
 * @test
 * @summary java.security.Provider.Service conformance: constructor and getters.
 * @run main ServiceTest
 */

public class ServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.Provider p = java.security.Security.getProviders()[0];
        java.security.Provider.Service s = new java.security.Provider.Service(
            p, "MessageDigest", "SHA-256", "java.lang.String",
            java.util.Arrays.asList("SHA256"), new java.util.HashMap<String, String>());
        check("MessageDigest".equals(s.getType()), "getType");
        check("SHA-256".equals(s.getAlgorithm()), "getAlgorithm");
        check(p.equals(s.getProvider()), "getProvider");
        check("java.lang.String".equals(s.getClassName()), "getClassName");

        // ==== contract deepening: real instantiation loop, supportsParameter, null matrix ====

        // ---- real SUN service instantiates a working engine (closed loop with known vector) ----
        java.security.Provider sun = java.security.Security.getProvider("SUN");
        java.security.Provider.Service md5 = sun.getService("MessageDigest", "MD5");
        Object engine = md5.newInstance(null);
        // recorded: SUN registers SPI classes; newInstance returns the SPI object, not the facade
        check(engine.getClass().getName().equals(md5.getClassName()),
                "newInstance returns registered class instance");
        check(engine instanceof java.security.MessageDigestSpi, "MD5 service instance is a MessageDigestSpi");
        check(!(engine instanceof java.security.MessageDigest),
                "SPI instance is not the facade MessageDigest (recorded)");
        // passing a non-null constructor parameter fails (engine uses no-arg ctor)
        try {
            md5.newInstance(new Object());
            check(false, "newInstance(param) should fail");
        } catch (java.security.NoSuchAlgorithmException e) {
            check(true, "newInstance(param) NoSuchAlgorithmException");
        }
        // distinct instances per call
        check(md5.newInstance(null) != engine, "newInstance distinct instances");

        // ---- SecureRandom service instantiation ----
        java.security.Provider.Service srng = sun.getService("SecureRandom", "SHA1PRNG");
        check(srng != null && srng.newInstance(null) instanceof java.security.SecureRandomSpi,
                "SHA1PRNG service newInstance returns SecureRandomSpi (recorded)");

        // ---- constructor null matrix -> NPE ----
        try { new java.security.Provider.Service(null, "T", "A", "C", null, null); check(false, "null provider"); }
        catch (NullPointerException e) { check(true, "Service(null provider) NPE"); }
        try { new java.security.Provider.Service(p, null, "A", "C", null, null); check(false, "null type"); }
        catch (NullPointerException e) { check(true, "Service(null type) NPE"); }
        try { new java.security.Provider.Service(p, "T", null, "C", null, null); check(false, "null algorithm"); }
        catch (NullPointerException e) { check(true, "Service(null algorithm) NPE"); }
        try { new java.security.Provider.Service(p, "T", "A", null, null, null); check(false, "null className"); }
        catch (NullPointerException e) { check(true, "Service(null className) NPE"); }

        // ---- supportsParameter matrix ----
        java.security.Key dsaKey = java.security.KeyPairGenerator.getInstance("DSA")
                .generateKeyPair().getPublic();
        java.security.Key rsaKey = java.security.KeyPairGenerator.getInstance("RSA")
                .generateKeyPair().getPublic();
        try { md5.supportsParameter(dsaKey); check(false, "supportsParameter on MD"); }
        catch (java.security.InvalidParameterException e) { check(true, "supportsParameter MD engine IPE"); }
        java.util.Map<String, String> sigAttrs = new java.util.HashMap<>();
        sigAttrs.put("SupportedKeyClasses", "java.security.interfaces.DSAPublicKey");
        java.security.Provider.Service sigSvc = new java.security.Provider.Service(
                p, "Signature", "FakeSig", "java.lang.String", null, sigAttrs);
        check(sigSvc.supportsParameter(dsaKey), "supportsParameter matching key");
        check(!sigSvc.supportsParameter(rsaKey), "supportsParameter non-matching key");
        try { sigSvc.supportsParameter("not-a-key"); check(false, "supportsParameter non-Key"); }
        catch (java.security.InvalidParameterException e) { check(true, "supportsParameter non-Key IPE"); }

        // ---- aliases and attributes surface in toString ----
        java.util.Map<String, String> attrs = new java.util.HashMap<>();
        attrs.put("SupportedKeyClasses", "java.security.Key");
        java.security.Provider.Service aliased = new java.security.Provider.Service(
                p, "MessageDigest", "Aliased", "java.lang.String",
                java.util.Arrays.asList("Alias1"), attrs);
        String ts = aliased.toString();
        check(ts.contains("Aliased") && ts.contains("Alias1") && ts.contains("SupportedKeyClasses"),
                "toString contains algorithm, alias, attribute");
        check("java.security.Key".equals(aliased.getAttribute("SupportedKeyClasses")),
                "custom attribute round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
