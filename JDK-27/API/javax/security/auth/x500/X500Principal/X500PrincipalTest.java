/*
 * @test
 * @summary javax.security.auth.x500.X500Principal conformance: white-box structure + black-box behavior.
 * @run main X500PrincipalTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.x500.X500Principal;

public class X500PrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.x500.X500Principal");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(java.security.Principal.class.isAssignableFrom(c), "implements Principal");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        X500Principal p = new X500Principal("CN=Alice, O=Org");
        check("CN=Alice,O=Org".equals(p.getName()), "getName is RFC2253");
        check("CN=Alice,O=Org".equals(p.getName(X500Principal.RFC2253)), "getName RFC2253");
        check("CN=Alice, O=Org".equals(p.getName(X500Principal.RFC1779)), "getName RFC1779");
        check("cn=alice,o=org".equals(p.getName(X500Principal.CANONICAL)), "getName CANONICAL");
        check("CN=Alice, O=Org".equals(p.toString()), "toString");

        // formats are case-insensitive
        check("cn=alice,o=org".equals(p.getName("canonical")), "canonical format case-insensitive");
        check("CN=Alice,O=Org".equals(p.getName("rfc2253")), "rfc2253 format case-insensitive");

        // encoding round-trips
        byte[] enc = p.getEncoded();
        check(enc.length > 0, "getEncoded non-empty");
        X500Principal fromBytes = new X500Principal(enc);
        check(fromBytes.equals(p), "byte[] ctor round-trip equals");
        check(new X500Principal(p.getEncoded()).getName().equals("CN=Alice,O=Org"), "byte[] ctor name");

        // getEncoded returns a clone
        byte[] enc2 = p.getEncoded();
        check(enc != enc2, "getEncoded returns a clone");
        enc2[0] = 0;
        check("CN=Alice,O=Org".equals(p.getName()), "mutating clone does not affect principal");

        // InputStream ctor round-trip
        X500Principal fromStream = new X500Principal(new ByteArrayInputStream(p.getEncoded()));
        check(fromStream.equals(p), "InputStream ctor round-trip equals");

        // (String, Map) ctor with empty map behaves like String ctor
        X500Principal withMap = new X500Principal("CN=Alice, O=Org", Collections.<String, String>emptyMap());
        check(withMap.equals(p), "String+empty-map ctor equals String ctor");

        // equals / hashCode: canonical comparison ignores input case
        X500Principal lower = new X500Principal("cn=alice, o=org");
        check(p.equals(lower), "equals ignores input case");
        check(p.hashCode() == lower.hashCode(), "hashCode equal for canonical-equal names");
        check(!p.equals(new X500Principal("CN=Bob, O=Org")), "different names not equal");
        check(!p.equals("CN=Alice, O=Org"), "not equal to non-principal");

        // exception paths
        boolean iaeFmt = false;
        try { p.getName(null); } catch (IllegalArgumentException e) { iaeFmt = true; }
        check(iaeFmt, "getName(null format) throws IllegalArgumentException");
        boolean iaeFmt2 = false;
        try { p.getName("BOGUS"); } catch (IllegalArgumentException e) { iaeFmt2 = true; }
        check(iaeFmt2, "getName(bogus format) throws IllegalArgumentException");
        boolean npeOidMap = false;
        try { p.getName(X500Principal.RFC2253, null); } catch (NullPointerException e) { npeOidMap = true; }
        check(npeOidMap, "getName(format, null oidMap) throws NullPointerException");
        boolean npeName = false;
        try { new X500Principal((String) null); } catch (NullPointerException e) { npeName = true; }
        check(npeName, "null String name throws NullPointerException");
        boolean iaeName = false;
        try { new X500Principal("not a valid dn !!"); } catch (IllegalArgumentException e) { iaeName = true; }
        check(iaeName, "invalid name throws IllegalArgumentException");
        boolean iaeMap = false;
        try { new X500Principal("CN=Alice", (Map<String, String>) null); } catch (NullPointerException e) { iaeMap = true; }
        check(iaeMap, "null keywordMap throws NullPointerException");

        // keywordMap supplies custom attribute keyword -> OID
        Map<String, String> kw = new HashMap<>();
        kw.put("FOO", "1.2.3.4.5.6.7.8.9.10.11");
        X500Principal custom = new X500Principal("FOO=Bar, CN=Alice", kw);
        check(custom.getName(X500Principal.RFC1779).contains("OID.1.2.3.4.5.6.7.8.9.10.11"), "custom keyword maps to OID in RFC1779 name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
