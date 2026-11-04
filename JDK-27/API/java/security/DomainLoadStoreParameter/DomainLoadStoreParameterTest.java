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

        // ---- appended contract dimensions ----
        // all write channels of the unmodifiable view rejected
        try { d.getProtectionParams().remove("domain1"); check(false, "remove UOE"); }
        catch (UnsupportedOperationException e) { check(true, "remove UOE"); }
        try { d.getProtectionParams().putAll(Collections.emptyMap()); check(false, "putAll UOE"); }
        catch (UnsupportedOperationException e) { check(true, "putAll UOE"); }
        try { d.getProtectionParams().clear(); check(false, "clear UOE"); }
        catch (UnsupportedOperationException e) { check(true, "clear UOE"); }
        // snapshot semantics: source-map mutation after construction is invisible
        map.put("domain3", null);
        map.remove("domain1");
        check(d.getProtectionParams().size() == 2, "source mutation invisible");
        check(d.getProtectionParams().get("domain1") == pp1, "removed source entry still snapshotted");
        // identity equals/hashCode: no value semantics
        DomainLoadStoreParameter other = new DomainLoadStoreParameter(uri,
                new LinkedHashMap<>(java.util.Collections.singletonMap("a", pp1)));
        check(!d.equals(other), "identity equals");
        check(d.equals(d), "reflexive");
        check(d.hashCode() == d.hashCode(), "hashCode stable");
        // exactly 3 getters, class is final
        int getters = 0;
        for (java.lang.reflect.Method m : DomainLoadStoreParameter.class.getDeclaredMethods())
            if (m.getName().startsWith("get")) getters++;
        check(getters == 3, "exactly 3 getters");
        check(java.lang.reflect.Modifier.isFinal(DomainLoadStoreParameter.class.getModifiers()), "final class");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
