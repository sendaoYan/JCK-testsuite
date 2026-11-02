/*
 * @test
 * @summary sun.security.krb5.internal.crypto.KeyUsage conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto:+open
 * @run main KeyUsageTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyUsageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.KeyUsage", false, KeyUsageTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.KeyUsage"), "name");
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
        check(methods.contains("isValid"), "method:isValid");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("KU_AD_KDC_ISSUED_CKSUM"), "field:KU_AD_KDC_ISSUED_CKSUM");
        check(fields.contains("KU_AP_REQ_AUTHENTICATOR"), "field:KU_AP_REQ_AUTHENTICATOR");
        check(fields.contains("KU_AS_REQ"), "field:KU_AS_REQ");
        check(fields.contains("KU_AUTHENTICATOR_CKSUM"), "field:KU_AUTHENTICATOR_CKSUM");
        check(fields.contains("KU_ENC_AP_REP_PART"), "field:KU_ENC_AP_REP_PART");
        check(fields.contains("KU_ENC_AS_REP_PART"), "field:KU_ENC_AS_REP_PART");
        check(fields.contains("KU_ENC_KRB_CRED_PART"), "field:KU_ENC_KRB_CRED_PART");
        check(fields.contains("KU_ENC_KRB_PRIV_PART"), "field:KU_ENC_KRB_PRIV_PART");
        check(fields.contains("KU_ENC_TGS_REP_PART_SESSKEY"), "field:KU_ENC_TGS_REP_PART_SESSKEY");
        check(fields.contains("KU_ENC_TGS_REP_PART_SUBKEY"), "field:KU_ENC_TGS_REP_PART_SUBKEY");
        check(fields.contains("KU_KRB_SAFE_CKSUM"), "field:KU_KRB_SAFE_CKSUM");
        check(fields.contains("KU_PA_ENC_TS"), "field:KU_PA_ENC_TS");
        check(fields.contains("KU_PA_FOR_USER_ENC_CKSUM"), "field:KU_PA_FOR_USER_ENC_CKSUM");
        check(fields.contains("KU_PA_TGS_REQ_AUTHENTICATOR"), "field:KU_PA_TGS_REQ_AUTHENTICATOR");
        check(fields.contains("KU_PA_TGS_REQ_CKSUM"), "field:KU_PA_TGS_REQ_CKSUM");
        check(fields.contains("KU_TGS_REQ_AUTH_DATA_SESSKEY"), "field:KU_TGS_REQ_AUTH_DATA_SESSKEY");
        check(fields.contains("KU_TGS_REQ_AUTH_DATA_SUBKEY"), "field:KU_TGS_REQ_AUTH_DATA_SUBKEY");
        check(fields.contains("KU_TICKET"), "field:KU_TICKET");
        check(fields.contains("KU_UNKNOWN"), "field:KU_UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
