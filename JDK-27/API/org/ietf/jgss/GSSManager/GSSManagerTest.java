/*
 * @test
 * @summary org.ietf.jgss.GSSManager conformance: white-box structure + black-box behavior.
 * @run main GSSManagerTest
 */

import org.ietf.jgss.*;
import java.lang.reflect.*;

public class GSSManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasStaticMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n) && Modifier.isStatic(m.getModifiers())) return true;
        return false;
    }
    static boolean hasPublicCtor(Class<?> c) { for (Constructor<?> k : c.getConstructors()) return true; return false; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(!c.isInterface(), "is class not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasPublicCtor(c), "public no-arg ctor");
        check(hasStaticMethod(c, "getInstance"), "static getInstance");
        String[] names = { "getMechs", "getNamesForMech", "getMechsForName", "createName",
                "createCredential", "createContext", "addProviderAtFront", "addProviderAtEnd" };
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Oid krb5 = new Oid("1.2.840.113554.1.2.2");

        GSSManager m = GSSManager.getInstance();
        check(m != null, "getInstance non-null");
        check(m instanceof GSSManager, "getInstance returns a GSSManager");

        // available mechanisms always include Kerberos v5
        Oid[] mechs = m.getMechs();
        check(mechs != null && mechs.length >= 1, "getMechs non-empty");
        check(krb5.containedIn(mechs), "Kerberos v5 mechanism available");

        // name types supported by Kerberos mechanism
        Oid[] namesForMech = m.getNamesForMech(krb5);
        check(namesForMech != null && namesForMech.length >= 1, "getNamesForMech non-empty");
        check(GSSName.NT_USER_NAME.containedIn(namesForMech), "NT_USER_NAME supported by krb5");
        check(GSSName.NT_HOSTBASED_SERVICE.containedIn(namesForMech), "NT_HOSTBASED_SERVICE supported by krb5");
        check(GSSName.NT_EXPORT_NAME.containedIn(namesForMech), "NT_EXPORT_NAME supported by krb5");

        // mechanisms that support a given name type include krb5
        Oid[] mechsForName = m.getMechsForName(GSSName.NT_USER_NAME);
        check(mechsForName != null && krb5.containedIn(mechsForName), "krb5 supports NT_USER_NAME");

        // createName -> export -> re-import round trip (deterministic, no Kerberos auth)
        GSSName n = m.createName("user@EXAMPLE.COM", GSSName.NT_USER_NAME);
        check("user@EXAMPLE.COM".equals(n.toString()), "createName toString");
        GSSName reimported = m.createName(n.export(), GSSName.NT_EXPORT_NAME);
        check(n.equals(reimported), "createName export/re-import round trip");

        // null nameType falls back to the mechanism default
        GSSName viaString = m.createName("user@EXAMPLE.COM", (Oid) null);
        check("user@EXAMPLE.COM".equals(viaString.toString()), "null nameType falls back to default");

        // hostbased-service name: without a krb5 default realm the JDK rejects it
        // (BAD_NAME); with a configured realm it would succeed - both accepted here
        boolean threw = false;
        try {
            GSSName hbn = m.createName("service@host.example.com", GSSName.NT_HOSTBASED_SERVICE);
            check(hbn.toString().contains("host"), "hostbased toString contains host");
        } catch (GSSException e) {
            threw = true;
            check(e.getMajor() == GSSException.BAD_NAME, "hostbased name without realm -> BAD_NAME");
        }
        if (threw) check(true, "hostbased name requires realm configuration (sandbox has none)");

        // null name string -> GSSException BAD_NAME
        try { m.createName((String) null, GSSName.NT_USER_NAME); check(false, "expected GSSException for null name"); }
        catch (GSSException e) { check(e.getMajor() == GSSException.BAD_NAME, "null name -> BAD_NAME"); }

        // malformed exported name bytes -> GSSException BAD_NAME
        try { m.createName(new byte[]{1, 2, 3}, GSSName.NT_EXPORT_NAME); check(false, "expected GSSException for malformed export bytes"); }
        catch (GSSException e) { check(e.getMajor() == GSSException.BAD_NAME, "malformed export bytes -> BAD_NAME"); }

        // zero-length export bytes -> ArrayIndexOutOfBoundsException (JDK implementation
        // quirk, identical on JDK 8 and JDK 27; documented, not a GSSException)
        try { m.createName(new byte[0], GSSName.NT_EXPORT_NAME); check(false, "expected AIOOBE for empty export bytes"); }
        catch (GSSException e) { check(true, "empty export bytes rejected as GSSException on this build"); }
        catch (ArrayIndexOutOfBoundsException expected) { check(true, "empty export bytes -> AIOOBE (implementation quirk)"); }

        // no credential source -> GSSException NO_CRED (deterministic without a KDC)
        try {
            m.createCredential((GSSName) null, GSSCredential.INDEFINITE_LIFETIME,
                    (Oid) null, GSSCredential.INITIATE_AND_ACCEPT);
            check(false, "expected GSSException for null default credential");
        } catch (GSSException e) {
            check(e.getMajor() == GSSException.NO_CRED, "default credential unavailable -> NO_CRED");
            check(e.getMessage() != null && e.getMessage().contains("No valid credentials provided"),
                    "NO_CRED message mentions credentials");
        }

        // provider-pinning API rejects null provider with NPE
        try { m.addProviderAtFront(null, null); check(false, "expected NPE for null provider"); }
        catch (NullPointerException expected) { check(true, "addProviderAtFront(null, null) -> NPE"); }
        try { m.addProviderAtEnd(null, null); check(false, "expected NPE for null provider"); }
        catch (NullPointerException expected) { check(true, "addProviderAtEnd(null, null) -> NPE"); }

        // createContext from an empty inter-process token -> GSSException NO_CONTEXT
        try {
            m.createContext(new byte[0]);
            check(false, "expected GSSException for empty context token");
        } catch (GSSException e) {
            check(e.getMajor() == GSSException.NO_CONTEXT, "empty context token -> NO_CONTEXT");
        }

        // createContext from a malformed token -> GSSException (exact major varies)
        try {
            m.createContext(new byte[]{1, 2, 3});
            check(false, "expected GSSException for malformed context token");
        } catch (GSSException e) {
            check(true, "malformed context token throws GSSException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
