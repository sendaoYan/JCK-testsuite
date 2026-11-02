/*
 * @test
 * @summary sun.security.krb5.internal.ktab.KeyTabConstants conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ktab:+open
 * @run main KeyTabConstantsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyTabConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ktab.KeyTabConstants", false, KeyTabConstantsTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ktab.KeyTabConstants"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("KRB5_KT_VNO"), "field:KRB5_KT_VNO");
        check(fields.contains("KRB5_KT_VNO_1"), "field:KRB5_KT_VNO_1");
        check(fields.contains("keySize"), "field:keySize");
        check(fields.contains("keyTypeSize"), "field:keyTypeSize");
        check(fields.contains("keyVersionSize"), "field:keyVersionSize");
        check(fields.contains("principalComponentSize"), "field:principalComponentSize");
        check(fields.contains("principalSize"), "field:principalSize");
        check(fields.contains("principalTypeSize"), "field:principalTypeSize");
        check(fields.contains("realmSize"), "field:realmSize");
        check(fields.contains("timestampSize"), "field:timestampSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
