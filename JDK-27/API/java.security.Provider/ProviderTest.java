/*
 * @test
 * @summary java.security.Provider conformance: getName/getVersion/getInfo/getServices via installed provider.
 * @run main ProviderTest
 */

import java.security.Provider;
import java.security.Security;

public class ProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Provider p = Security.getProviders()[0];
        check(p.getName() != null, "getName");
        check(p.getVersion() > 0, "getVersion");
        check(p.getInfo() != null, "getInfo");
        check(p.getServices() != null, "getServices");
        check(p.getServices().iterator().hasNext(), "has services");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
