/*
 * @test
 * @summary java.security.DomainLoadStoreParameter conformance: constructor and accessors.
 * @run main DomainLoadStoreParameterTest
 */

import java.security.DomainLoadStoreParameter;
import java.security.KeyStore;
import java.net.URI;
import java.util.HashMap;

public class DomainLoadStoreParameterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        URI uri = new URI("file:///tmp/domain.cfg");
        KeyStore.ProtectionParameter pp = new KeyStore.PasswordProtection("pw".toCharArray());
        HashMap<String, KeyStore.ProtectionParameter> map = new HashMap<>();
        map.put("entry", pp);
        DomainLoadStoreParameter p = new DomainLoadStoreParameter(uri, map);
        check(uri.equals(p.getConfiguration()), "getConfiguration");
        check(map.equals(p.getProtectionParams()), "getProtectionParams");
        check(p.getProtectionParameter() == null, "getProtectionParameter null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
