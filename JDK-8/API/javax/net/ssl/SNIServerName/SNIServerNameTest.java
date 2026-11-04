/*
 * @test
 * @summary javax.net.ssl.SNIServerName conformance: type/encoded contract + equality.
 * @run main SNIServerNameTest
 */
import java.lang.reflect.*;
import java.util.*;
import javax.net.ssl.*;
public class SNIServerNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    static class Fixed extends SNIServerName {
        Fixed(int type, byte[] encoded) { super(type, encoded); }
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SNIServerName");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isProtected(k.getModifiers()) && k.getParameterCount() == 2) protCtor = true;
        check(protCtor, "protected (int, byte[]) constructor");
        for (String n : new String[] {"getType", "getEncoded", "equals", "hashCode", "toString"}) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }

        byte[] host = "ab.com".getBytes("UTF-8");
        Fixed n0 = new Fixed(0, host);
        check(n0.getType() == 0, "getType");
        check(Arrays.equals(n0.getEncoded(), host), "getEncoded content");
        Fixed custom = new Fixed(131, new byte[] {1, 2, 3});
        check(custom.getType() == 131, "non-host type preserved");

        // encoded array is defensive on both ends
        byte[] mut = {1, 2, 3};
        Fixed defended = new Fixed(5, mut);
        mut[0] = 99;
        check(defended.getEncoded()[0] == 1, "constructor copies the source array");
        byte[] got = defended.getEncoded();
        got[0] = 99;
        check(defended.getEncoded()[0] == 1, "getEncoded returns a fresh copy each call");

        // equality: same type + same bytes, only within the same concrete class shape
        Fixed a = new Fixed(0, host), b = new Fixed(0, "ab.com".getBytes("UTF-8"));
        check(a.equals(b) && b.equals(a), "same type + same encoded are equal");
        check(a.hashCode() == b.hashCode(), "equal names share hashCode");
        check(!a.equals(new Fixed(1, host)), "different type not equal");
        check(!a.equals(new Fixed(0, "xy.com".getBytes("UTF-8"))), "different encoded not equal");
        // actual JDK behavior: SNIServerName.equals(null) throws NPE (no null guard);
        // recorded as-is on both JDK-8 and JDK-27
        check(expect(() -> { a.equals(null); return "ok"; }).equals("NullPointerException"),
                "equals(null) -> NullPointerException (unguarded)");
        check(!a.equals("ab.com"), "equals(non-SNIServerName) false");
        check(a.equals(a), "reflexive");
        // equality is class-sensitive: a plain SNIServerName never equals an SNIHostName with identical data
        check(!a.equals(new SNIHostName("ab.com")) && !new SNIHostName("ab.com").equals(a),
                "cross-subclass equality is refused even with identical type + bytes");

        // empty encoded is legal; the standard SNIHostName rejects it in its own ctor
        Fixed empty = new Fixed(7, new byte[0]);
        check(empty.getEncoded() != null && empty.getEncoded().length == 0, "empty encoded preserved");

        // constructor validation
        check(expect(() -> new Fixed(-1, new byte[2])).equals("IllegalArgumentException"),
                "negative type -> IllegalArgumentException");
        check(expect(() -> new Fixed(256, new byte[2])).equals("IllegalArgumentException"),
                "type 256 -> IllegalArgumentException");
        check(expect(() -> new Fixed(0, null)).equals("NullPointerException"),
                "null encoded -> NullPointerException");

        // toString renders the type index and the encoded bytes in hex
        String s = a.toString();
        check(s.contains("0") && s.contains("61:62:2E:63:6F:6D"), "toString contains type and hex bytes: " + s);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
