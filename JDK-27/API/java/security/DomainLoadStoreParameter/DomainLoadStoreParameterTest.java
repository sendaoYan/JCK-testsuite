/*
 * @test
 * @summary java.security.DomainLoadStoreParameter conformance: white-box structure + black-box behavior.
 * @run main DomainLoadStoreParameterTest
 */

import java.security.DomainLoadStoreParameter;
import java.security.KeyStore;
import java.net.URI;
import java.util.HashMap;
import java.security.*;
import java.util.*;

public class DomainLoadStoreParameterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        URI uri = new URI("file:///tmp/domain.cfg");
        KeyStore.ProtectionParameter pp = new KeyStore.PasswordProtection("pw".toCharArray());
        HashMap<String, KeyStore.ProtectionParameter> map = new HashMap<>();
        map.put("entry", pp);
        DomainLoadStoreParameter p = new DomainLoadStoreParameter(uri, map);
        check(uri.equals(p.getConfiguration()), "getConfiguration");
        check(map.equals(p.getProtectionParams()), "getProtectionParams");
        check(p.getProtectionParameter() == null, "getProtectionParameter null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.net.URI uri = java.net.URI.create("file:/tmp/jck-domains.config");
        KeyStore.ProtectionParameter pp1 = new KeyStore.PasswordProtection("pw1".toCharArray());
        KeyStore.ProtectionParameter pp2 = new KeyStore.PasswordProtection("pw2".toCharArray());
        Map<String, KeyStore.ProtectionParameter> map = new LinkedHashMap<>();
        map.put("domain1", pp1);
        map.put("domain2", pp2);
        DomainLoadStoreParameter d = new DomainLoadStoreParameter(uri, map);
        check(d.getConfiguration().equals(uri), "configuration uri");
        check(d.getProtectionParameter() == null, "keystore protection null by spec");
        Map<String, KeyStore.ProtectionParameter> out = d.getProtectionParams();
        check(out.size() == 2, "params size");
        check(out.get("domain1") == pp1 && out.get("domain2") == pp2, "params content");
        // returned map is unmodifiable
        try {
            out.put("domain3", null);
            check(false, "unmodifiable map");
        } catch (UnsupportedOperationException uoe) {
            check(true, "unmodifiable map");
        }
        check(d.getProtectionParams().size() == 2, "unchanged after mutation attempt");
        // empty map: getProtectionParameter is null
        DomainLoadStoreParameter empty = new DomainLoadStoreParameter(uri, new LinkedHashMap<>());
        check(empty.getProtectionParameter() == null, "empty protection null");
        check(empty.getProtectionParams().isEmpty(), "empty params");
        // nulls rejected
        try { new DomainLoadStoreParameter(null, map); check(false, "null uri"); }
        catch (NullPointerException npe) { check(true, "null uri NPE"); }
        try { new DomainLoadStoreParameter(uri, null); check(false, "null map"); }
        catch (NullPointerException npe) { check(true, "null map NPE"); }
        // implements KeyStore.LoadStoreParameter
        check(d instanceof KeyStore.LoadStoreParameter, "LoadStoreParameter");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
