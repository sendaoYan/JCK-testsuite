/*
 * @test
 * @summary java.security.Provider.Service conformance: white-box structure + black-box behavior.
 * @run main ServiceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Provider;
import java.security.Security;
import java.util.List;
import java.util.Map;

public class ServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Provider$Service");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"getType", "getAlgorithm", "getProvider", "getClassName", "getAttribute", "newInstance", "supportsParameter", "toString"});
    }

    static void testBlackBox() throws Exception {
        Provider sun = Security.getProvider("SUN");
        check(sun != null, "SUN provider present");
        Provider.Service s = sun.getService("MessageDigest", "SHA-256");
        check(s != null, "MessageDigest SHA-256 service");
        check("MessageDigest".equals(s.getType()), "getType");
        check("SHA-256".equals(s.getAlgorithm()), "getAlgorithm");
        check(s.getClassName() != null, "getClassName");
        check(sun.equals(s.getProvider()), "getProvider");
        check(s.getAttribute("NotAnAttribute") == null, "getAttribute unknown null");
        check(s.toString() != null && s.toString().contains("SHA-256"), "toString");

        Provider.Service custom = new Provider.Service(sun, "MessageDigest", "MyAlg", "com.example.MyDigest", (List<String>) null, (Map<String, String>) null);
        check("MessageDigest".equals(custom.getType()), "constructed getType");
        check("MyAlg".equals(custom.getAlgorithm()), "constructed getAlgorithm");
        check("com.example.MyDigest".equals(custom.getClassName()), "constructed getClassName");
        check(sun.equals(custom.getProvider()), "constructed getProvider");
        check(custom.getAttribute("x") == null, "constructed getAttribute null");
        check(custom.toString() != null && custom.toString().contains("MyAlg"), "constructed toString");
    }


    static void testDeepened() throws Exception {
        Provider sun = Security.getProvider("SUN");
        // real SUN service instantiates a working engine (closed loop with known vector)
        Provider.Service md5 = sun.getService("MessageDigest", "MD5");
        Object engine = md5.newInstance(null);
        // recorded: SUN registers SPI classes; newInstance returns the SPI object, not the facade
        check(engine.getClass().getName().equals(md5.getClassName()),
                "newInstance returns registered class instance");
        check(engine instanceof java.security.MessageDigestSpi, "MD5 service instance is a MessageDigestSpi");
        check(!(engine instanceof java.security.MessageDigest),
                "SPI instance is not the facade MessageDigest (recorded)");
        try { md5.newInstance(new Object()); check(false, "newInstance(param) should fail"); }
        catch (java.security.NoSuchAlgorithmException e) { check(true, "newInstance(param) NSAE"); }
        check(md5.newInstance(null) != engine, "newInstance distinct instances");
        Provider.Service srng = sun.getService("SecureRandom", "SHA1PRNG");
        check(srng != null && srng.newInstance(null) instanceof java.security.SecureRandomSpi,
                "SHA1PRNG service newInstance returns SecureRandomSpi (recorded)");

        // constructor null matrix -> NPE
        try { new Provider.Service(null, "T", "A", "C", null, null); check(false, "null provider"); }
        catch (NullPointerException e) { check(true, "Service(null provider) NPE"); }
        try { new Provider.Service(sun, null, "A", "C", null, null); check(false, "null type"); }
        catch (NullPointerException e) { check(true, "Service(null type) NPE"); }
        try { new Provider.Service(sun, "T", null, "C", null, null); check(false, "null algorithm"); }
        catch (NullPointerException e) { check(true, "Service(null algorithm) NPE"); }
        try { new Provider.Service(sun, "T", "A", null, null, null); check(false, "null className"); }
        catch (NullPointerException e) { check(true, "Service(null className) NPE"); }

        // supportsParameter matrix
        java.security.Key dsaKey = java.security.KeyPairGenerator.getInstance("DSA")
                .generateKeyPair().getPublic();
        java.security.Key rsaKey = java.security.KeyPairGenerator.getInstance("RSA")
                .generateKeyPair().getPublic();
        try { md5.supportsParameter(dsaKey); check(false, "supportsParameter on MD"); }
        catch (java.security.InvalidParameterException e) { check(true, "supportsParameter MD engine IPE"); }
        Map<String, String> sigAttrs = new java.util.HashMap<>();
        sigAttrs.put("SupportedKeyClasses", "java.security.interfaces.DSAPublicKey");
        Provider.Service sigSvc = new Provider.Service(sun, "Signature", "FakeSig",
                "java.lang.String", null, sigAttrs);
        check(sigSvc.supportsParameter(dsaKey), "supportsParameter matching key");
        check(!sigSvc.supportsParameter(rsaKey), "supportsParameter non-matching key");
        try { sigSvc.supportsParameter("not-a-key"); check(false, "supportsParameter non-Key"); }
        catch (java.security.InvalidParameterException e) { check(true, "supportsParameter non-Key IPE"); }

        // aliases/attributes surface in toString
        Map<String, String> attrs = new java.util.HashMap<>();
        attrs.put("SupportedKeyClasses", "java.security.Key");
        Provider.Service aliased = new Provider.Service(sun, "MessageDigest", "Aliased",
                "java.lang.String", java.util.Arrays.asList("Alias1"), attrs);
        String ts = aliased.toString();
        check(ts.contains("Aliased") && ts.contains("Alias1") && ts.contains("SupportedKeyClasses"),
                "toString contains algorithm, alias, attribute");
        check("java.security.Key".equals(aliased.getAttribute("SupportedKeyClasses")),
                "custom attribute round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
