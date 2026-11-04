/*
 * @test
 * @summary java.security.KeyStore.LoadStoreParameter conformance: white-box structure + black-box behavior.
 * @run main LoadStoreParameterTest
 */

import java.lang.reflect.*;

import java.util.*;
import java.security.KeyStore;


public class LoadStoreParameterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$LoadStoreParameter");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"getProtectionParameter"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Implemented by DomainLoadStoreParameter; getProtectionParameter contract
        java.net.URI cfg = java.net.URI.create("file:/tmp/jck-domain.cfg");
        java.util.Map<String, KeyStore.ProtectionParameter> map = new LinkedHashMap<>();
        map.put("keystore", new KeyStore.PasswordProtection("pw".toCharArray()));
        KeyStore.LoadStoreParameter lsp = new java.security.DomainLoadStoreParameter(cfg, map);
        check(lsp instanceof KeyStore.LoadStoreParameter, "implements LSP");
        check(lsp.getProtectionParameter() == null, "null protection for the keystore entry");
        // anonymous implementation returns its own parameter
        KeyStore.ProtectionParameter mine = new KeyStore.ProtectionParameter() { };
        class SimpleLSP implements KeyStore.LoadStoreParameter {
            public KeyStore.ProtectionParameter getProtectionParameter() { return mine; }
        }
        check(new SimpleLSP().getProtectionParameter() == mine, "custom LSP parameter");
        // load via LoadStoreParameter (null password) initializes an empty store
        class NullPwLSP implements KeyStore.LoadStoreParameter {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection(null);
            }
        }
        KeyStore fresh = KeyStore.getInstance("PKCS12");
        fresh.load(new NullPwLSP());
        check(fresh.size() == 0, "load via LoadStoreParameter");
        // PKCS12 does not support store(LoadStoreParameter) - default engineStore throws
        try {
            fresh.store(new NullPwLSP());
            check(false, "store via LSP throws");
        } catch (UnsupportedOperationException uoe) {
            check(true, "store via LSP UnsupportedOperationException");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$LoadStoreParameter");

        // declares exactly one method returning ProtectionParameter
        Method[] own = c.getDeclaredMethods();
        check(own.length == 1, "exactly one declared method");
        check("getProtectionParameter".equals(own[0].getName()), "sole method name");
        check(KeyStore.ProtectionParameter.class.equals(own[0].getReturnType()),
                "returns ProtectionParameter");

        // JDK-9+: JKS also accepts load(LSP) through engineLoad(null, protection)
        // (JDK-8 threw UnsupportedOperationException here)
        KeyStore jks = KeyStore.getInstance("JKS");
        jks.load(new KeyStore.LoadStoreParameter() {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection("pw".toCharArray());
            }
        });
        check(jks.size() == 0, "JKS load(LSP) empty store (JDK-9+)");
        KeyStore jks2 = KeyStore.getInstance("JKS");
        jks2.load(new KeyStore.LoadStoreParameter() {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection(null);
            }
        });
        check(jks2.size() == 0, "JKS load(LSP) null password empty store");

        // store(LSP) remains unsupported for stream-based formats on both sides
        boolean threw = false;
        try { jks.store(new KeyStore.LoadStoreParameter() {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection("pw".toCharArray());
            } }); }
        catch (UnsupportedOperationException uoe) { threw = true; }
        check(threw, "JKS store(LSP) UOE");

        // DomainLoadStoreParameter surface: configuration URI + per-keystore map snapshot
        java.net.URI cfg = java.net.URI.create("file:/tmp/jck-lsp-deep.cfg");
        java.util.Map<String, KeyStore.ProtectionParameter> map = new LinkedHashMap<>();
        map.put("keystore", new KeyStore.PasswordProtection("pw".toCharArray()));
        java.security.DomainLoadStoreParameter d =
                new java.security.DomainLoadStoreParameter(cfg, map);
        check(c.isInstance(d), "DomainLoadStoreParameter implements LSP");
        check(d.getConfiguration().equals(cfg), "configuration URI round-trip");
        check(d.getProtectionParameter() == null, "domain-level protection null");
        check(d.getProtectionParams().keySet().equals(map.keySet()),
                "protection params snapshot keys");
        map.clear();
        check(d.getProtectionParams().size() == 1, "protection params defensive copy");

        // JCEKS mirrors JKS: load(LSP) accepted (JDK-9+), store(LSP) rejected
        KeyStore jce = KeyStore.getInstance("JCEKS");
        jce.load(new KeyStore.LoadStoreParameter() {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection("pw".toCharArray());
            } });
        check(jce.size() == 0, "JCEKS load(LSP) empty store (JDK-9+)");
        boolean jceStoreRejected = false;
        try { jce.store(new KeyStore.LoadStoreParameter() {
            public KeyStore.ProtectionParameter getProtectionParameter() {
                return new KeyStore.PasswordProtection("pw".toCharArray());
            } }); }
        catch (UnsupportedOperationException uoe) { jceStoreRejected = true; }
        check(jceStoreRejected, "JCEKS store(LSP) UOE");

        // the snapshot view is not just copied but unmodifiable
        boolean dPutRejected = false;
        try { d.getProtectionParams().put("x", null); }
        catch (UnsupportedOperationException uoe) { dPutRejected = true; }
        check(dPutRejected, "protection params view unmodifiable");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
