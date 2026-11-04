/*
 * @test
 * @summary javax.net.ssl.SNIHostName conformance: ctor validation, equality, IDN, matcher.
 * @run main SNIHostNameTest
 */
import java.lang.reflect.*;
import java.util.*;
import javax.net.ssl.*;
public class SNIHostNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SNIHostName");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("javax.net.ssl.SNIServerName"), "extends SNIServerName");
        for (String n : new String[] {"getAsciiName", "equals", "hashCode", "toString", "createSNIMatcher"}) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
        int pubCtors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors()) if (Modifier.isPublic(k.getModifiers())) pubCtors++;
        check(pubCtors == 2, "2 public constructors (String) and (byte[])");

        SNIHostName h = new SNIHostName("example.com");
        check(h.getAsciiName().equals("example.com"), "ascii name");
        check(h.getType() == 0, "type host_name");
        // getAsciiName returns the given spelling verbatim: the class does NOT lowercase it
        check(new SNIHostName("EXAMPLE.COM").getAsciiName().equals("EXAMPLE.COM"),
                "String ctor preserves the given case");
        SNIHostName hb = new SNIHostName("ab.com".getBytes("UTF-8"));
        check(hb.getAsciiName().equals("ab.com"), "byte[] ctor ascii name");
        check(Arrays.equals(hb.getEncoded(), "ab.com".getBytes("UTF-8")), "byte[] ctor encoded");
        check(hb.equals(new SNIHostName("ab.com")), "byte[] ctor equals String ctor with same host");
        // encoded defensive copy
        byte[] mut = "ab.com".getBytes("UTF-8");
        SNIHostName hd = new SNIHostName(mut);
        mut[0] = 'z';
        check(hd.getEncoded()[0] == 'a', "byte[] ctor copies the source array");
        byte[] enc = hd.getEncoded();
        enc[0] = 'z';
        check(hd.getEncoded()[0] == 'a', "getEncoded returns a fresh copy each call");

        // equals/hashCode are case-insensitive on the DNS name
        check(new SNIHostName("ab.com").equals(new SNIHostName("AB.com")), "equals case-insensitive");
        check(new SNIHostName("AB.com").hashCode() == new SNIHostName("ab.com").hashCode(),
                "hashCode case-insensitive");
        check(!new SNIHostName("ab.com").equals(new SNIHostName("xy.com")), "equals different host");
        check(!new SNIHostName("ab.com").equals("ab.com"), "equals non-SNI object");
        check(!new SNIHostName("ab.com").equals(null), "equals null false");
        check(h.toString().contains("ab.com") || h.toString().contains("example.com"),
                "toString contains the name");

        // IDN: non-ASCII hosts are converted with punycode
        SNIHostName idn = new SNIHostName("\u4f8b\u3048.jp");
        check(idn.getAsciiName().equals("xn--r8jz45g.jp"), "IDN converted to punycode");

        // constructor validation (shared by both ctors)
        check(expect(() -> new SNIHostName("")).equals("IllegalArgumentException"), "empty host rejected");
        check(expect(() -> new SNIHostName("ab.com.")).equals("IllegalArgumentException"), "trailing dot rejected");
        check(expect(() -> new SNIHostName("a b")).equals("IllegalArgumentException"), "space rejected");
        check(expect(() -> new SNIHostName("a\tb")).equals("IllegalArgumentException"), "tab rejected");
        check(expect(() -> new SNIHostName((String) null)).equals("NullPointerException"), "null String -> NPE");
        check(expect(() -> new SNIHostName((byte[]) null)).equals("NullPointerException"), "null byte[] -> NPE");
        check(expect(() -> new SNIHostName(new byte[0])).equals("IllegalArgumentException"), "empty byte[] rejected");
        // JDK 9+ rejects non-ASCII bytes in the byte[] ctor (JDK-8 stored them verbatim)
        check(expect(() -> new SNIHostName(new byte[] {0x00, 0x61})).equals("IllegalArgumentException"),
                "non-ASCII byte[] -> IllegalArgumentException (tightened after JDK 8)");

        // matcher integration
        SNIMatcher mm = SNIHostName.createSNIMatcher("Example.COM");
        check(mm.matches(new SNIHostName("example.com")), "matcher mixed-case pattern matches lower-case name");
        check(!mm.matches(new SNIHostName("other.org")), "matcher rejects other host");
        SSLParameters p = new SSLParameters();
        p.setServerNames(Collections.singletonList((SNIServerName) new SNIHostName("Example.com")));
        List<SNIServerName> got = p.getServerNames();
        check(got.size() == 1 && got.get(0) instanceof SNIHostName
                && ((SNIHostName) got.get(0)).getAsciiName().equals("Example.com"),
                "server name round trip through SSLParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
