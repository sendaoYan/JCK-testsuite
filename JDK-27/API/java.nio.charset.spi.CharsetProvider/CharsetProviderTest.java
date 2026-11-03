/*
 * @test
 * @summary java.nio.charset.spi.CharsetProvider conformance: white-box structure + black-box provider behavior.
 * @run main CharsetProviderTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CharsetProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.spi.CharsetProvider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == Object.class, "superclass is Object");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor != null, "no-arg constructor present");
        check(Modifier.isProtected(ctor.getModifiers()), "constructor is protected");
    }

    static void testBlackBox() {
        CharsetProvider p = new CharsetProvider() {
            public Iterator<Charset> charsets() {
                return Arrays.asList(StandardCharsets.UTF_8, StandardCharsets.US_ASCII).iterator();
            }
            public Charset charsetForName(String charsetName) {
                if ("UTF-8".equals(charsetName)) return StandardCharsets.UTF_8;
                if ("US-ASCII".equals(charsetName)) return StandardCharsets.US_ASCII;
                return null;
            }
        };
        check(p.charsetForName("UTF-8") == StandardCharsets.UTF_8, "charsetForName UTF-8");
        check(p.charsetForName("US-ASCII") == StandardCharsets.US_ASCII, "charsetForName US-ASCII");
        check(p.charsetForName("no-such") == null, "charsetForName null for unknown");

        List<Charset> all = new ArrayList<>();
        p.charsets().forEachRemaining(all::add);
        check(all.size() == 2, "charsets yields two charsets");
        check(all.get(0) == StandardCharsets.UTF_8, "charsets first is UTF-8");
        check(all.get(1) == StandardCharsets.US_ASCII, "charsets second is US-ASCII");

        // An empty provider.
        CharsetProvider empty = new CharsetProvider() {
            public Iterator<Charset> charsets() { return Collections.emptyIterator(); }
            public Charset charsetForName(String charsetName) { return null; }
        };
        check(!empty.charsets().hasNext(), "empty charsets iterator");
        check(empty.charsetForName("anything") == null, "empty provider returns null for any name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
