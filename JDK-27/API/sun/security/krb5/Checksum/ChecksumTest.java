/*
 * @test
 * @summary sun.security.krb5.Checksum conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main ChecksumTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ChecksumTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.Checksum", false, ChecksumTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.Checksum"), "name");
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
        check(methods.contains("getBytes"), "method:getBytes");
        check(methods.contains("getType"), "method:getType");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("parse"), "method:parse");
        check(methods.contains("verifyAnyChecksum"), "method:verifyAnyChecksum");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CKSUMTYPE_CRC32"), "field:CKSUMTYPE_CRC32");
        check(fields.contains("CKSUMTYPE_DES_MAC"), "field:CKSUMTYPE_DES_MAC");
        check(fields.contains("CKSUMTYPE_DES_MAC_K"), "field:CKSUMTYPE_DES_MAC_K");
        check(fields.contains("CKSUMTYPE_HMAC_MD5_ARCFOUR"), "field:CKSUMTYPE_HMAC_MD5_ARCFOUR");
        check(fields.contains("CKSUMTYPE_HMAC_SHA1_96_AES128"), "field:CKSUMTYPE_HMAC_SHA1_96_AES128");
        check(fields.contains("CKSUMTYPE_HMAC_SHA1_96_AES256"), "field:CKSUMTYPE_HMAC_SHA1_96_AES256");
        check(fields.contains("CKSUMTYPE_HMAC_SHA1_DES3_KD"), "field:CKSUMTYPE_HMAC_SHA1_DES3_KD");
        check(fields.contains("CKSUMTYPE_HMAC_SHA256_128_AES128"), "field:CKSUMTYPE_HMAC_SHA256_128_AES128");
        check(fields.contains("CKSUMTYPE_HMAC_SHA384_192_AES256"), "field:CKSUMTYPE_HMAC_SHA384_192_AES256");
        check(fields.contains("CKSUMTYPE_NULL"), "field:CKSUMTYPE_NULL");
        check(fields.contains("CKSUMTYPE_RSA_MD4"), "field:CKSUMTYPE_RSA_MD4");
        check(fields.contains("CKSUMTYPE_RSA_MD4_DES"), "field:CKSUMTYPE_RSA_MD4_DES");
        check(fields.contains("CKSUMTYPE_RSA_MD4_DES_K"), "field:CKSUMTYPE_RSA_MD4_DES_K");
        check(fields.contains("CKSUMTYPE_RSA_MD5"), "field:CKSUMTYPE_RSA_MD5");
        check(fields.contains("CKSUMTYPE_RSA_MD5_DES"), "field:CKSUMTYPE_RSA_MD5_DES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
