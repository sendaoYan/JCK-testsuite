/*
 * @test
 * @summary javax.net.ssl.SSLPermission conformance: BasicPermission semantics.
 * @run main SSLPermissionTest
 */
import java.lang.reflect.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLPermission");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()), "public final class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.BasicPermission"), "extends BasicPermission");
        // JDK 24+: the permission classes are deprecated for removal
        Deprecated d = c.getAnnotation(Deprecated.class);
        check(d != null, "@Deprecated present on JDK 24+");
        check(d.forRemoval(), "@Deprecated(forRemoval=true)");

        int pubCtors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors()) if (Modifier.isPublic(k.getModifiers())) pubCtors++;
        check(pubCtors == 2, "2 public constructors (String) and (String, String actions)");

        SSLPermission p = new SSLPermission("setHostnameVerifier");
        check("setHostnameVerifier".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "single-arg ctor yields empty actions");
        check("".equals(new SSLPermission("setHostnameVerifier", "read").getActions()),
                "BasicPermission ignores the actions argument: actions stay empty");
        check(p.implies(new SSLPermission("setHostnameVerifier")), "implies same-name permission");
        check(!p.implies(new SSLPermission("setSSLFactory")), "does not imply a different name");
        SSLPermission wild = new SSLPermission("*");
        check(wild.implies(p) && wild.implies(new SSLPermission("anything")), "wildcard implies all");
        check(!p.implies(wild), "specific does not imply wildcard");
        check(p.equals(new SSLPermission("setHostnameVerifier")), "equals same name");
        check(!p.equals(new SSLPermission("other")), "not equals different name");
        check(p.hashCode() == new SSLPermission("setHostnameVerifier").hashCode(), "hashCode consistent");
        check(new SSLPermission("setHostnameVerifier").newPermissionCollection() != null,
                "newPermissionCollection non-null");
        check(expect(() -> new SSLPermission(null)).equals("NullPointerException"),
                "null name -> NullPointerException");
        check(expect(() -> new SSLPermission("")).equals("IllegalArgumentException"),
                "empty name -> IllegalArgumentException");
        check(p.toString().contains("setHostnameVerifier"), "toString contains the name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
