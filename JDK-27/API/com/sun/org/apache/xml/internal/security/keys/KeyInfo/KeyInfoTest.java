/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.KeyInfo conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.keys:+open
 * @run main KeyInfoTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.KeyInfo", false, KeyInfoTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.keys.KeyInfo"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.ElementProxy"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("add"), "method:add");
        check(methods.contains("addDEREncodedKeyValue"), "method:addDEREncodedKeyValue");
        check(methods.contains("addKeyInfoReference"), "method:addKeyInfoReference");
        check(methods.contains("addKeyName"), "method:addKeyName");
        check(methods.contains("addKeyValue"), "method:addKeyValue");
        check(methods.contains("addMgmtData"), "method:addMgmtData");
        check(methods.contains("addRetrievalMethod"), "method:addRetrievalMethod");
        check(methods.contains("addStorageResolver"), "method:addStorageResolver");
        check(methods.contains("addUnknownElement"), "method:addUnknownElement");
        check(methods.contains("containsDEREncodedKeyValue"), "method:containsDEREncodedKeyValue");
        check(methods.contains("containsKeyInfoReference"), "method:containsKeyInfoReference");
        check(methods.contains("containsKeyName"), "method:containsKeyName");
        check(methods.contains("containsKeyValue"), "method:containsKeyValue");
        check(methods.contains("containsMgmtData"), "method:containsMgmtData");
        check(methods.contains("containsPGPData"), "method:containsPGPData");
        check(methods.contains("containsRetrievalMethod"), "method:containsRetrievalMethod");
        check(methods.contains("containsSPKIData"), "method:containsSPKIData");
        check(methods.contains("containsUnknownElement"), "method:containsUnknownElement");
        check(methods.contains("containsX509Data"), "method:containsX509Data");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getBaseNamespace"), "method:getBaseNamespace");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getPrivateKey"), "method:getPrivateKey");
        check(methods.contains("getPublicKey"), "method:getPublicKey");
        check(methods.contains("getSecretKey"), "method:getSecretKey");
        check(methods.contains("getX509Certificate"), "method:getX509Certificate");
        check(methods.contains("isEmpty"), "method:isEmpty");
        check(methods.contains("itemDEREncodedKeyValue"), "method:itemDEREncodedKeyValue");
        check(methods.contains("itemKeyInfoReference"), "method:itemKeyInfoReference");
        check(methods.contains("itemKeyName"), "method:itemKeyName");
        check(methods.contains("itemKeyValue"), "method:itemKeyValue");
        check(methods.contains("itemMgmtData"), "method:itemMgmtData");
        check(methods.contains("itemPGPData"), "method:itemPGPData");
        check(methods.contains("itemRetrievalMethod"), "method:itemRetrievalMethod");
        check(methods.contains("itemSPKIData"), "method:itemSPKIData");
        check(methods.contains("itemUnknownElement"), "method:itemUnknownElement");
        check(methods.contains("itemX509Data"), "method:itemX509Data");
        check(methods.contains("lengthDEREncodedKeyValue"), "method:lengthDEREncodedKeyValue");
        check(methods.contains("lengthKeyInfoReference"), "method:lengthKeyInfoReference");
        check(methods.contains("lengthKeyName"), "method:lengthKeyName");
        check(methods.contains("lengthKeyValue"), "method:lengthKeyValue");
        check(methods.contains("lengthMgmtData"), "method:lengthMgmtData");
        check(methods.contains("lengthPGPData"), "method:lengthPGPData");
        check(methods.contains("lengthRetrievalMethod"), "method:lengthRetrievalMethod");
        check(methods.contains("lengthSPKIData"), "method:lengthSPKIData");
        check(methods.contains("lengthUnknownElement"), "method:lengthUnknownElement");
        check(methods.contains("lengthX509Data"), "method:lengthX509Data");
        check(methods.contains("registerInternalKeyResolver"), "method:registerInternalKeyResolver");
        check(methods.contains("setId"), "method:setId");
        check(methods.contains("setSecureValidation"), "method:setSecureValidation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
