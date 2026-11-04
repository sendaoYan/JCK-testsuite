/*
 * @test
 * @summary javax.net.ssl.TrustManagerFactorySpi conformance: white-box structure + black-box behavior.
 * @run main TrustManagerFactorySpiTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class TrustManagerFactorySpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.TrustManagerFactorySpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        class Probe extends javax.net.ssl.TrustManagerFactorySpi {
            protected void engineInit(java.security.KeyStore ks) {}
            protected void engineInit(javax.net.ssl.ManagerFactoryParameters p) {}
            protected javax.net.ssl.TrustManager[] engineGetTrustManagers() { return null; }
        }
        check(new Probe() != null, "subclassable");
    }
    // ---- Part 3: contract deepening (spi surface + invocation) ----
    static void testContract() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.TrustManagerFactorySpi");
        boolean noArg = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (k.getParameterCount() == 0) noArg = true;
        check(noArg, "no-arg constructor present for subclassing");
        // exactly 3 protected abstract engine methods (two 1-arg engineInit + engineGetTrustManagers)
        c.getDeclaredMethod("engineInit", java.security.KeyStore.class);
        c.getDeclaredMethod("engineInit", ManagerFactoryParameters.class);
        c.getDeclaredMethod("engineGetTrustManagers");
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 3, "exactly 3 abstract methods, found " + abstractCount);
        // a probe implementation records the init sequence and serves sentinel managers
        final TrustManager sentinel = new TrustManager() {};
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        class Probe extends TrustManagerFactorySpi {
            protected void engineInit(java.security.KeyStore ks) { calls.add("ks"); }
            protected void engineInit(ManagerFactoryParameters p) { calls.add("mfp"); }
            protected TrustManager[] engineGetTrustManagers() { return new TrustManager[]{sentinel}; }
        }
        Probe p = new Probe();
        p.engineInit((java.security.KeyStore) null);
        p.engineInit(new ManagerFactoryParameters() {});
        check(calls.equals(java.util.Arrays.asList("ks", "mfp")),
                "engineInit overload dispatch recorded: " + calls);
        TrustManager[] got = p.engineGetTrustManagers();
        check(got.length == 1 && got[0] == sentinel, "engineGetTrustManagers returns implementation value");
        check(p.engineGetTrustManagers() != got, "engineGetTrustManagers may return a fresh array per call");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
