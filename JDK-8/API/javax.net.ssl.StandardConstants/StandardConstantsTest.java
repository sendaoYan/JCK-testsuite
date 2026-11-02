/*
 * @test
 * @summary javax.net.ssl.StandardConstants conformance: SNI_HOST_NAME constant.
 * @run main StandardConstantsTest
 */

public class StandardConstantsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(javax.net.ssl.StandardConstants.SNI_HOST_NAME == 0, "SNI_HOST_NAME == 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
