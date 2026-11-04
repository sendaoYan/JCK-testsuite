/*
 * @test
 * @summary java.io.FilterInputStream API conformance: read delegates to the underlying
 *          stream (constructor is protected, so use a passthrough subclass).
 * @run main FilterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;

public class FilterInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Passthrough extends FilterInputStream {
        Passthrough(java.io.InputStream in) { super(in); }
    }

    static void testFilterInputStream() throws Exception {
        FilterInputStream fis =
            new Passthrough(new ByteArrayInputStream(new byte[]{5, 6}));
        check(fis.read() == 5 && fis.read() == 6 && fis.read() == -1, "delegates read");
    }

    public static void main(String[] args) throws Exception {
        testFilterInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
