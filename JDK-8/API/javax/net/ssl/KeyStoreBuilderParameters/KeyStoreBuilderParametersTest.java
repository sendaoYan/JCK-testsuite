/*
 * @test
 * @summary javax.net.ssl.KeyStoreBuilderParameters conformance: white-box structure + black-box behavior.
 * @run main KeyStoreBuilderParametersTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class KeyStoreBuilderParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.KeyStoreBuilderParameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Arrays.asList(Class.forName("javax.net.ssl.KeyStoreBuilderParameters").getInterfaces()).contains(Class.forName("javax.net.ssl.ManagerFactoryParameters")), "extends javax.net.ssl.ManagerFactoryParameters");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.security.KeyStore.Builder b = java.security.KeyStore.Builder.newInstance("PKCS12", null, new java.security.KeyStore.PasswordProtection("x".toCharArray()));
        javax.net.ssl.KeyStoreBuilderParameters kbp = new javax.net.ssl.KeyStoreBuilderParameters(b);
        java.util.List<java.security.KeyStore.Builder> l = kbp.getParameters();
        check(l.size() == 1 && l.get(0) == b, "single builder roundtrip");
        check(expect(() -> { l.add(b); return "ok"; }).equals("UnsupportedOperationException"), "list unmodifiable");
        java.util.List<java.security.KeyStore.Builder> two = new java.util.ArrayList<>(java.util.Arrays.asList(b, b));
        javax.net.ssl.KeyStoreBuilderParameters kbp2 = new javax.net.ssl.KeyStoreBuilderParameters(two);
        check(kbp2.getParameters().size() == 2, "list ctor size");
        check(expect(() -> { new javax.net.ssl.KeyStoreBuilderParameters((java.security.KeyStore.Builder) null); return "ok"; }).equals("NullPointerException"), "null NPE");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
