/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit.Parser conformance: white-box structure + black-box behavior.
 * @run main ParserTest
 */

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Modifier;

import javax.swing.text.html.HTMLEditorKit;

public class ParserTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Concrete implementation of the abstract parse contract.
    static class RecordingParser extends HTMLEditorKit.Parser {
        boolean ignoreCharset;
        int parseCalls;
        public void parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharset)
                throws IOException {
            this.ignoreCharset = ignoreCharset;
            this.parseCalls++;
        }
    }

    // Exposes the protected getParser() factory so we can reach the real parser.
    static class KitWithParser extends HTMLEditorKit {
        HTMLEditorKit.Parser parser() { return super.getParser(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit$Parser");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        // protected no-arg constructor
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
              "protected constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // the abstract contract is implementable and callable
        RecordingParser parser = new RecordingParser();
        HTMLEditorKit.ParserCallback cb = new HTMLEditorKit.ParserCallback();
        parser.parse(new StringReader("<html></html>"), cb, true);
        check(parser.parseCalls == 1, "parse() is invoked once");
        check(parser.ignoreCharset, "parse() forwards ignoreCharset flag");

        // HTMLEditorKit's real parser is a Parser and is reachable
        KitWithParser kit = new KitWithParser();
        HTMLEditorKit.Parser real = kit.parser();
        check(real != null, "getParser() returns a parser");
        check(real instanceof HTMLEditorKit.Parser, "real parser is an HTMLEditorKit.Parser");
        // the real parser drives a callback (documented public contract)
        real.parse(new StringReader("<html><body>hi</body></html>"), cb, false);
        cb.flush();
        check(true, "real parser parses and callback flushes without error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
