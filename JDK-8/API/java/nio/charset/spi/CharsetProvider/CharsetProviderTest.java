/*
 * @test
 * @summary java.nio.charset.spi.CharsetProvider conformance: charsets()/charsetForName().
 * @run main CharsetProviderTest
 */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;

public class CharsetProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharsetProvider() {
        CharsetProvider p = new CharsetProvider() {
            public Iterator<Charset> charsets() {
                return Collections.singletonList(StandardCharsets.UTF_8).iterator();
            }
            public Charset charsetForName(String charsetName) {
                if ("UTF-8".equals(charsetName)) return StandardCharsets.UTF_8;
                return null;
            }
        };
        check(p.charsetForName("UTF-8") == StandardCharsets.UTF_8, "charsetForName UTF-8");
        check(p.charsetForName("no-such") == null, "charsetForName null for unknown");
        Iterator<Charset> it = p.charsets();
        check(it.hasNext() && it.next() == StandardCharsets.UTF_8, "charsets iterator");
    }

    public static void main(String[] args) {
        testCharsetProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
