/*
 * @test
 * @summary java.io.BufferedReader API conformance: readLine, and null at EOF.
 * @run main BufferedReaderTest
 */

import java.io.BufferedReader;
import java.io.StringReader;

public class BufferedReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBufferedReader() throws Exception {
        BufferedReader br = new BufferedReader(new StringReader("a\nb\nc"));
        check("a".equals(br.readLine()), "readLine 1");
        check("b".equals(br.readLine()), "readLine 2");
        check("c".equals(br.readLine()), "readLine 3");
        check(br.readLine() == null, "readLine EOF");
    }

    public static void main(String[] args) throws Exception {
        testBufferedReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
