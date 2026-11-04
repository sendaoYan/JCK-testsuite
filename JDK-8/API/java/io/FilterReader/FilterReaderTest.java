/*
 * @test
 * @summary java.io.FilterReader API conformance: filters a Reader (via subclass; ctor is protected).
 * @run main FilterReaderTest
 */

import java.io.FilterReader;
import java.io.Reader;
import java.io.StringReader;

public class FilterReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Passthrough extends FilterReader {
        Passthrough(Reader in) {
            super(in);
        }
    }

    static void testFilterReader() throws Exception {
        Passthrough r = new Passthrough(new StringReader("abc"));
        check(r.read() == 'a', "read first");
        check(r.read() == 'b', "read second");
        check(r.read() == 'c', "read third");
        check(r.read() == -1, "read EOF");
    }

    public static void main(String[] args) throws Exception {
        testFilterReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
