/*
 * @test
 * @summary java.io.StreamTokenizer API conformance: number and word token types.
 * @run main StreamTokenizerTest
 */

import java.io.StreamTokenizer;
import java.io.StringReader;

public class StreamTokenizerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStreamTokenizer() throws Exception {
        StreamTokenizer st = new StreamTokenizer(new StringReader("42 hello 3.14"));
        check(st.nextToken() == StreamTokenizer.TT_NUMBER && st.nval == 42.0,
              "number 42");
        check(st.nextToken() == StreamTokenizer.TT_WORD && "hello".equals(st.sval),
              "word hello");
        check(st.nextToken() == StreamTokenizer.TT_NUMBER && st.nval == 3.14,
              "number 3.14");
        check(st.nextToken() == StreamTokenizer.TT_EOF, "EOF");
    }

    public static void main(String[] args) throws Exception {
        testStreamTokenizer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
