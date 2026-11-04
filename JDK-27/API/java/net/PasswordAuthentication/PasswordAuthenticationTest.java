/*
 * @test
 * @summary java.net.PasswordAuthentication conformance: userName/password copy.
 * @run main PasswordAuthenticationTest
 */

import java.net.PasswordAuthentication;

public class PasswordAuthenticationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void checkEq(char[] a, char[] b, String m) {
        if (a == null || b == null || a.length != b.length) { check(false, m); return; }
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) { check(false, m); return; }
        passed++;
    }

    // ---- original white-box assertions (preserved verbatim) ----
    static void testOriginalWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.PasswordAuthentication");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getConstructor(String.class, char[].class) != null, "ctor(String,char[])");
        check(c.getMethod("getUserName") != null, "getUserName");
        check(c.getMethod("getPassword") != null, "getPassword");
    }

    // ---- original black-box assertions (preserved verbatim) ----
    static void testOriginalBlackBox() {
        char[] pw = { 's', 'e', 'c', 'r', 'e', 't' };
        PasswordAuthentication pa = new PasswordAuthentication("user", pw);
        check(pa.getUserName().equals("user"), "userName");
        check(java.util.Arrays.equals(pa.getPassword(), "secret".toCharArray()), "password content");

        // constructor clones the caller's array (documented)
        pw[0] = 'X';
        check(java.util.Arrays.equals(pa.getPassword(), "secret".toCharArray()), "constructor clones input array");

        // getPassword returns the internal array reference (documented)
        check(pa.getPassword() == pa.getPassword(), "getPassword returns internal reference");
    }

    // ---- white-box structure (javap -public, JDK-27) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.PasswordAuthentication");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getConstructors().length == 1, "single public ctor");
        check(c.getConstructor(String.class, char[].class) != null, "ctor(String, char[])");
        check(c.getMethod("getUserName").getReturnType() == String.class, "getUserName returns String");
        check(c.getMethod("getPassword").getReturnType() == char[].class, "getPassword returns char[]");
        // NOT Serializable on JDK-27 (probed)
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
    }

    // ---- constructor defensive copy of the caller's array (documented + probed) ----
    static void testConstructorCopiesInput() {
        char[] pw = "secret".toCharArray();
        PasswordAuthentication pa = new PasswordAuthentication("alice", pw);
        pw[0] = 'X';
        checkEq(pa.getPassword(), "secret".toCharArray(), "ctor clones input array");
        // zero-length input is fine
        PasswordAuthentication empty = new PasswordAuthentication("bob", new char[0]);
        check(empty.getPassword().length == 0, "empty password allowed");
        check("bob".equals(empty.getUserName()), "user name for empty pw");
        // single char
        PasswordAuthentication one = new PasswordAuthentication("carl", new char[]{'z'});
        checkEq(one.getPassword(), new char[]{'z'}, "single char password");
        // long array
        char[] longPw = new char[1000];
        for (int i = 0; i < longPw.length; i++) longPw[i] = (char) ('a' + (i % 26));
        PasswordAuthentication big = new PasswordAuthentication("dora", longPw);
        checkEq(big.getPassword(), longPw, "1000-char password round-trips");
        // unicode content survives
        char[] uni = {'P', 'a', 's', 's', 'w', 'o', 'r', 't', 'ä', '中'};
        PasswordAuthentication u = new PasswordAuthentication("üser", uni);
        checkEq(u.getPassword(), uni, "unicode password content");
        check("üser".equals(u.getUserName()), "unicode user name");
    }

    // ---- getPassword hands out the internal reference (documented; caller must clone) ----
    static void testGetPasswordReferenceSemantics() {
        PasswordAuthentication pa = new PasswordAuthentication("eve", "live".toCharArray());
        char[] c1 = pa.getPassword();
        char[] c2 = pa.getPassword();
        check(c1 == c2, "getPassword returns the same reference each call (JDK behavior)");
        checkEq(c1, "live".toCharArray(), "content intact");
        c1[0] = 'L';
        checkEq(pa.getPassword(), "Live".toCharArray(), "mutating the returned array affects the holder");
        checkEq(pa.getPassword(), "Live".toCharArray(), "mutation is stable");
        // documented mitigation: caller clones
        char[] safe = pa.getPassword().clone();
        safe[0] = 'X';
        checkEq(pa.getPassword(), "Live".toCharArray(), "cloning the result isolates the holder");
        // getUserName returns the same String every call (interned field)
        check(pa.getUserName() == pa.getUserName(), "getUserName stable reference");
    }

    // ---- user name matrix (probed: null and empty user names are accepted) ----
    static void testUserNameMatrix() {
        check(new PasswordAuthentication(null, "p".toCharArray()).getUserName() == null, "null user accepted, returned null");
        check("".equals(new PasswordAuthentication("", "p".toCharArray()).getUserName()), "empty user accepted");
        check("user@example.com".equals(new PasswordAuthentication("user@example.com", "p".toCharArray()).getUserName()),
            "user name with at-sign");
        check("  spaced  ".equals(new PasswordAuthentication("  spaced  ", "p".toCharArray()).getUserName()),
            "user name keeps whitespace");
        checkEq(new PasswordAuthentication(null, "p".toCharArray()).getPassword(), "p".toCharArray(),
            "password intact under null user");
        PasswordAuthentication unicode = new PasswordAuthentication("äöü", "p".toCharArray());
        check("äöü".equals(unicode.getUserName()), "non-ASCII user name");
    }

    // ---- error paths (probed: null password -> NPE; helpful message on JDK-27) ----
    static void testErrors() {
        try {
            new PasswordAuthentication("u", null);
            check(false, "null password must throw");
        } catch (NullPointerException e) {
            check(String.valueOf(e.getMessage()).contains("password"), "null password message mentions password");
        }
        try {
            new PasswordAuthentication(null, null);
            check(false, "all-null must throw");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    // ---- object semantics: no equals/hashCode override, identity semantics ----
    static void testObjectSemantics() {
        PasswordAuthentication a = new PasswordAuthentication("same", "pw".toCharArray());
        PasswordAuthentication b = new PasswordAuthentication("same", "pw".toCharArray());
        check(!a.equals(b), "no value equality: distinct instances unequal");
        check(a.equals(a), "identity equals");
        check(!a.equals(null), "not equal to null");
        check(!a.equals("same"), "not equal to String");
        check(a.hashCode() == a.hashCode(), "hashCode stable within instance");
        String s = a.toString();
        check(s != null && s.contains("PasswordAuthentication"), "toString mentions class");
    }

    // ---- use as an Authenticator result (round-trip through the callback) ----
    static void testInAuthenticatorFlow() throws Exception {
        final PasswordAuthentication pa = new PasswordAuthentication("flow-user", "flow-pw".toCharArray());
        java.net.Authenticator a = new java.net.Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() { return pa; }
        };
        java.net.Authenticator.setDefault(a);
        try {
            PasswordAuthentication r = java.net.Authenticator.requestPasswordAuthentication(
                java.net.InetAddress.getByName("127.0.0.1"), 80, "http", "p", "s");
            check(r == pa, "same instance flows back");
            check("flow-user".equals(r.getUserName()), "user name through flow");
            checkEq(r.getPassword(), "flow-pw".toCharArray(), "password through flow");
        } finally {
            java.net.Authenticator.setDefault(null);
        }
    }

    // ---- user x password combination matrix ----
    static void testCombinationMatrix() {
        String[] users = {"u1", "", "long-user-name-over-thirty-two-characters", "ünicode", null};
        char[][] pws = {"x".toCharArray(), new char[0], "äöü中".toCharArray(), "0123456789abcdef".toCharArray()};
        for (String u : users) {
            for (char[] pw : pws) {
                PasswordAuthentication pa = new PasswordAuthentication(u, pw);
                check(pa.getUserName() == u, "matrix user identity kept: " + u);
                checkEq(pa.getPassword(), pw, "matrix password content: len=" + pw.length);
                check(pa.getPassword().length == pw.length, "matrix length: " + pw.length);
            }
        }
        // mutating each non-empty caller array after construction never leaks in
        for (char[] pw : pws) {
            if (pw.length == 0) continue;
            char[] expected = pw.clone();
            PasswordAuthentication pa = new PasswordAuthentication("guard", pw);
            pw[0] = '!';
            check(pa.getPassword()[0] != '!' || expected[0] == '!', "no aliasing to caller array");
            checkEq(pa.getPassword(), expected, "stored copy unaffected");
        }
    }

    public static void main(String[] args) throws Exception {
        testOriginalWhiteBox();
        testOriginalBlackBox();
        testWhiteBox();
        testConstructorCopiesInput();
        testGetPasswordReferenceSemantics();
        testUserNameMatrix();
        testErrors();
        testObjectSemantics();
        testInAuthenticatorFlow();
        testCombinationMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
