/*
 * @test
 * @summary java.net.http.UnsupportedProtocolVersionException conformance: final IOException with message.
 * @run main UnsupportedProtocolVersionExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.http.UnsupportedProtocolVersionException;

public class UnsupportedProtocolVersionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Modifier.isFinal(UnsupportedProtocolVersionException.class.getModifiers()),
                "UnsupportedProtocolVersionException is final");

        UnsupportedProtocolVersionException e =
                new UnsupportedProtocolVersionException("unsupported protocol version");
        check("unsupported protocol version".equals(e.getMessage()), "message preserved");
        check(e instanceof IOException, "is an IOException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
