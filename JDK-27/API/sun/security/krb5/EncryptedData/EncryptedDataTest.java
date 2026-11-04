/*
 * @test
 * @summary sun.security.krb5.EncryptedData conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main EncryptedDataTest
 */
import java.lang.reflect.*;
import java.util.*;
public class EncryptedDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.EncryptedData", false, EncryptedDataTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.EncryptedData"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.Cloneable"), "iface:java.lang.Cloneable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asn1Encode"), "method:asn1Encode");
        check(methods.contains("clone"), "method:clone");
        check(methods.contains("decrypt"), "method:decrypt");
        check(methods.contains("getBytes"), "method:getBytes");
        check(methods.contains("getEType"), "method:getEType");
        check(methods.contains("getKeyVersionNumber"), "method:getKeyVersionNumber");
        check(methods.contains("parse"), "method:parse");
        check(methods.contains("reset"), "method:reset");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ETYPE_AES128_CTS_HMAC_SHA1_96"), "field:ETYPE_AES128_CTS_HMAC_SHA1_96");
        check(fields.contains("ETYPE_AES128_CTS_HMAC_SHA256_128"), "field:ETYPE_AES128_CTS_HMAC_SHA256_128");
        check(fields.contains("ETYPE_AES256_CTS_HMAC_SHA1_96"), "field:ETYPE_AES256_CTS_HMAC_SHA1_96");
        check(fields.contains("ETYPE_AES256_CTS_HMAC_SHA384_192"), "field:ETYPE_AES256_CTS_HMAC_SHA384_192");
        check(fields.contains("ETYPE_ARCFOUR_HMAC"), "field:ETYPE_ARCFOUR_HMAC");
        check(fields.contains("ETYPE_ARCFOUR_HMAC_EXP"), "field:ETYPE_ARCFOUR_HMAC_EXP");
        check(fields.contains("ETYPE_DES3_CBC_HMAC_SHA1_KD"), "field:ETYPE_DES3_CBC_HMAC_SHA1_KD");
        check(fields.contains("ETYPE_DES_CBC_CRC"), "field:ETYPE_DES_CBC_CRC");
        check(fields.contains("ETYPE_DES_CBC_MD4"), "field:ETYPE_DES_CBC_MD4");
        check(fields.contains("ETYPE_DES_CBC_MD5"), "field:ETYPE_DES_CBC_MD5");
        check(fields.contains("ETYPE_NULL"), "field:ETYPE_NULL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
