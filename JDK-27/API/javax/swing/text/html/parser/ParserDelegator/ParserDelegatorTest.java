/*
 * @test
 * @summary javax.swing.text.html.parser.ParserDelegator conformance: white-box structure + black-box behavior.
 * @run main ParserDelegatorTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class ParserDelegatorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.ParserDelegator");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.HTMLEditorKit$Parser"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class CB extends HTMLEditorKit.ParserCallback {
        final List<String> starts = new ArrayList<String>();
        final List<String> ends = new ArrayList<String>();
        final List<String> simples = new ArrayList<String>();
        final List<String> texts = new ArrayList<String>();
        final List<String> comments = new ArrayList<String>();
        final List<Boolean> implied = new ArrayList<Boolean>();
        String eol;

        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            starts.add(t.toString());
            implied.add(Boolean.TRUE.equals(a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED)));
        }
        public void handleEndTag(HTML.Tag t, int pos) { ends.add(t.toString()); }
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) { simples.add(t.toString()); }
        public void handleText(char[] data, int pos) { texts.add(new String(data)); }
        public void handleComment(char[] data, int pos) { comments.add(new String(data)); }
        public void handleEndOfLineString(String e) { eol = e; }
    }

    static void testBlackBox() throws Exception {
        ParserDelegator pd = new ParserDelegator();

        CB cb = new CB();
        pd.parse(new StringReader("<p>Hello</p>"), cb, false);
        check(cb.starts.contains("html") && cb.starts.contains("head") && cb.starts.contains("body") && cb.starts.contains("p"), "start tags html/head/body/p");
        check(cb.ends.contains("p") && cb.ends.contains("body") && cb.ends.contains("html"), "end tags");
        check(cb.texts.contains("Hello"), "text Hello");
        check("\n".equals(cb.eol), "end-of-line string newline");

        CB cb2 = new CB();
        pd.parse(new StringReader("<br>foo"), cb2, false);
        check(cb2.simples.contains("br"), "simple tag br");
        check(cb2.texts.contains("foo"), "text foo");

        CB cb3 = new CB();
        pd.parse(new StringReader("<!-- note -->"), cb3, false);
        check(cb3.comments.size() == 1 && cb3.comments.get(0).contains("note"), "comment note");

        CB cb4 = new CB();
        pd.parse(new StringReader("<p>Hi</p>"), cb4, true);
        check(cb4.starts.contains("p") && cb4.texts.contains("Hi"), "ignoreCharSet=true still parses");

        // each parse() call starts a fresh DocumentParser; results are independent
        CB cb5 = new CB();
        pd.parse(new StringReader("<i>em</i>"), cb5, false);
        check(cb5.starts.contains("i") && cb5.texts.contains("em"), "fresh parse independent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
