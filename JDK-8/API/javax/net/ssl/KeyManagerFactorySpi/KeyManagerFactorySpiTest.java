/*
 * @test
 * @summary javax.net.ssl.KeyManagerFactorySpi conformance: white-box structure + black-box behavior.
 * @run main KeyManagerFactorySpiTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class KeyManagerFactorySpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.KeyManagerFactorySpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        class Probe extends javax.net.ssl.KeyManagerFactorySpi {
            protected void engineInit(java.security.KeyStore ks, char[] pw) {}
            protected void engineInit(javax.net.ssl.ManagerFactoryParameters p) {}
            protected javax.net.ssl.KeyManager[] engineGetKeyManagers() { return null; }
        }
        check(new Probe() != null, "subclassable");
    }
    // ---- Part 3: contract deepening (spi surface + invocation) ----
    static void testContract() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.KeyManagerFactorySpi");
        boolean protNoArg = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (k.getParameterCount() == 0) protNoArg = true;
        check(protNoArg, "no-arg constructor present for subclassing");
        // exactly 3 protected abstract engine methods
        c.getDeclaredMethod("engineInit", java.security.KeyStore.class, char[].class);
        c.getDeclaredMethod("engineInit", ManagerFactoryParameters.class);
        c.getDeclaredMethod("engineGetKeyManagers");
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 3, "exactly 3 abstract methods, found " + abstractCount);
        for (String n : new String[]{"engineInit", "engineGetKeyManagers"}) {
            boolean prot = false;
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals(n) && Modifier.isProtected(m.getModifiers())) prot = true;
            check(prot, n + " is protected in the spi");
        }
        // a probe implementation records the init sequence and serves sentinel managers
        final KeyManager sentinel = new KeyManager() {};
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        class Probe extends KeyManagerFactorySpi {
            protected void engineInit(java.security.KeyStore ks, char[] pw) { calls.add("ks:" + (pw == null)); }
            protected void engineInit(ManagerFactoryParameters p) { calls.add("mfp"); }
            protected KeyManager[] engineGetKeyManagers() { return new KeyManager[]{sentinel}; }
        }
        Probe p = new Probe();
        p.engineInit(null, null);
        p.engineInit(java.security.KeyStore.getInstance("PKCS12"), new char[]{'x'});
        p.engineInit(new ManagerFactoryParameters() {});
        check(calls.equals(java.util.Arrays.asList("ks:true", "ks:false", "mfp")),
                "engineInit overload dispatch recorded: " + calls);
        KeyManager[] got = p.engineGetKeyManagers();
        check(got.length == 1 && got[0] == sentinel, "engineGetKeyManagers returns implementation value");
        // re-init and fresh array on each call
        check(p.engineGetKeyManagers() != got, "engineGetKeyManagers may return a fresh array per call");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
