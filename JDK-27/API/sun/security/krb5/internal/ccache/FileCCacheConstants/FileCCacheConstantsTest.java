/*
 * @test
 * @summary sun.security.krb5.internal.ccache.FileCCacheConstants conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.ccache:+open
 * @run main FileCCacheConstantsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FileCCacheConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ccache.FileCCacheConstants", false, FileCCacheConstantsTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ccache.FileCCacheConstants"), "name");
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
        check(fields.contains("FCC_TAG_DELTATIME"), "field:FCC_TAG_DELTATIME");
        check(fields.contains("KRB5_FCC_FVNO_1"), "field:KRB5_FCC_FVNO_1");
        check(fields.contains("KRB5_FCC_FVNO_2"), "field:KRB5_FCC_FVNO_2");
        check(fields.contains("KRB5_FCC_FVNO_3"), "field:KRB5_FCC_FVNO_3");
        check(fields.contains("KRB5_FCC_FVNO_4"), "field:KRB5_FCC_FVNO_4");
        check(fields.contains("KRB5_NT_UNKNOWN"), "field:KRB5_NT_UNKNOWN");
        check(fields.contains("TKT_FLG_FORWARDABLE"), "field:TKT_FLG_FORWARDABLE");
        check(fields.contains("TKT_FLG_FORWARDED"), "field:TKT_FLG_FORWARDED");
        check(fields.contains("TKT_FLG_HW_AUTH"), "field:TKT_FLG_HW_AUTH");
        check(fields.contains("TKT_FLG_INITIAL"), "field:TKT_FLG_INITIAL");
        check(fields.contains("TKT_FLG_INVALID"), "field:TKT_FLG_INVALID");
        check(fields.contains("TKT_FLG_MAY_POSTDATE"), "field:TKT_FLG_MAY_POSTDATE");
        check(fields.contains("TKT_FLG_POSTDATED"), "field:TKT_FLG_POSTDATED");
        check(fields.contains("TKT_FLG_PRE_AUTH"), "field:TKT_FLG_PRE_AUTH");
        check(fields.contains("TKT_FLG_PROXIABLE"), "field:TKT_FLG_PROXIABLE");
        check(fields.contains("TKT_FLG_PROXY"), "field:TKT_FLG_PROXY");
        check(fields.contains("TKT_FLG_RENEWABLE"), "field:TKT_FLG_RENEWABLE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
