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
        System.out.println("PASS (" + passed + " assertions)");
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
