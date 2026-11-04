/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.KeyInfo conformance: class hierarchy and members.
 * @run main KeyInfoTest
 */
public class KeyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.KeyInfo", false, KeyInfoTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setSecureValidation"), "method setSecureValidation");
        check(mn.contains("setId"), "method setId");
        check(mn.contains("getId"), "method getId");
        check(mn.contains("addKeyName"), "method addKeyName");
        check(mn.contains("add"), "method add");
        check(mn.contains("addKeyValue"), "method addKeyValue");
        check(mn.contains("addMgmtData"), "method addMgmtData");
        check(mn.contains("addRetrievalMethod"), "method addRetrievalMethod");
        check(mn.contains("addDEREncodedKeyValue"), "method addDEREncodedKeyValue");
        check(mn.contains("addKeyInfoReference"), "method addKeyInfoReference");
        check(mn.contains("addUnknownElement"), "method addUnknownElement");
        check(mn.contains("lengthKeyName"), "method lengthKeyName");
        check(mn.contains("lengthKeyValue"), "method lengthKeyValue");
        check(mn.contains("lengthMgmtData"), "method lengthMgmtData");
        check(mn.contains("lengthPGPData"), "method lengthPGPData");
        check(mn.contains("lengthRetrievalMethod"), "method lengthRetrievalMethod");
        check(mn.contains("lengthSPKIData"), "method lengthSPKIData");
        check(mn.contains("lengthX509Data"), "method lengthX509Data");
        check(mn.contains("lengthDEREncodedKeyValue"), "method lengthDEREncodedKeyValue");
        check(mn.contains("lengthKeyInfoReference"), "method lengthKeyInfoReference");
        check(mn.contains("lengthUnknownElement"), "method lengthUnknownElement");
        check(mn.contains("itemKeyName"), "method itemKeyName");
        check(mn.contains("itemKeyValue"), "method itemKeyValue");
        check(mn.contains("itemMgmtData"), "method itemMgmtData");
        check(mn.contains("itemPGPData"), "method itemPGPData");
        check(mn.contains("itemRetrievalMethod"), "method itemRetrievalMethod");
        check(mn.contains("itemSPKIData"), "method itemSPKIData");
        check(mn.contains("itemX509Data"), "method itemX509Data");
        check(mn.contains("itemDEREncodedKeyValue"), "method itemDEREncodedKeyValue");
        check(mn.contains("itemKeyInfoReference"), "method itemKeyInfoReference");
        check(mn.contains("itemUnknownElement"), "method itemUnknownElement");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("containsKeyName"), "method containsKeyName");
        check(mn.contains("containsKeyValue"), "method containsKeyValue");
        check(mn.contains("containsMgmtData"), "method containsMgmtData");
        check(mn.contains("containsPGPData"), "method containsPGPData");
        check(mn.contains("containsRetrievalMethod"), "method containsRetrievalMethod");
        check(mn.contains("containsSPKIData"), "method containsSPKIData");
        check(mn.contains("containsUnknownElement"), "method containsUnknownElement");
        check(mn.contains("containsX509Data"), "method containsX509Data");
        check(mn.contains("containsDEREncodedKeyValue"), "method containsDEREncodedKeyValue");
        check(mn.contains("containsKeyInfoReference"), "method containsKeyInfoReference");
        check(mn.contains("getPublicKey"), "method getPublicKey");
        check(mn.contains("getPublicKeyFromStaticResolvers"), "method getPublicKeyFromStaticResolvers");
        check(mn.contains("getPublicKeyFromInternalResolvers"), "method getPublicKeyFromInternalResolvers");
        check(mn.contains("getX509Certificate"), "method getX509Certificate");
        check(mn.contains("getX509CertificateFromStaticResolvers"), "method getX509CertificateFromStaticResolvers");
        check(mn.contains("getX509CertificateFromInternalResolvers"), "method getX509CertificateFromInternalResolvers");
        check(mn.contains("getSecretKey"), "method getSecretKey");
        check(mn.contains("getSecretKeyFromStaticResolvers"), "method getSecretKeyFromStaticResolvers");
        check(mn.contains("getSecretKeyFromInternalResolvers"), "method getSecretKeyFromInternalResolvers");
        check(mn.contains("getPrivateKey"), "method getPrivateKey");
        check(mn.contains("getPrivateKeyFromStaticResolvers"), "method getPrivateKeyFromStaticResolvers");
        check(mn.contains("getPrivateKeyFromInternalResolvers"), "method getPrivateKeyFromInternalResolvers");
        check(mn.contains("registerInternalKeyResolver"), "method registerInternalKeyResolver");
        check(mn.contains("lengthInternalKeyResolver"), "method lengthInternalKeyResolver");
        check(mn.contains("itemInternalKeyResolver"), "method itemInternalKeyResolver");
        check(mn.contains("addStorageResolver"), "method addStorageResolver");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
