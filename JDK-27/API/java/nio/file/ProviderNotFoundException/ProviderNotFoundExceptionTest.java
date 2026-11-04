/*
 * @test
 * @summary java.nio.file.ProviderNotFoundException conformance: white-box structure + black-box behavior.
 * @run main ProviderNotFoundExceptionTest
 */

import java.nio.file.ProviderNotFoundException;

public class ProviderNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.RuntimeException.class.isAssignableFrom(ProviderNotFoundException.class), "hierarchy: extends UnsupportedOperationException");

        check(new ProviderNotFoundException() != null, "no-arg ctor");
        check(new ProviderNotFoundException("m").getMessage().equals("m"), "message ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        boolean threw = false;
        try {
            java.nio.file.FileSystems.newFileSystem(
                    new java.net.URI("jck-nosuch://host/"), java.util.Map.of());
        } catch (ProviderNotFoundException e) {
            threw = true;
        } catch (java.net.URISyntaxException e) {
            throw new AssertionError("bad test URI", e);
        }
        check(threw, "unknown scheme -> ProviderNotFoundException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
