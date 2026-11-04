/*
 * @test
 * @summary javax.swing.text.html.parser.DocumentParser conformance: white-box structure + black-box behavior.
 * @run main DocumentParserTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.ParserDelegator;

public class DocumentParserTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.DocumentParser");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.parser.Parser"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
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
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        DocumentParser dp = new DocumentParser(dtd);

        CB cb = new CB();
        dp.parse(new StringReader("<p>Hello <b>world</b></p>"), cb, false);
        check(cb.starts.size() == 5, "start tags count == 5, got " + cb.starts);
        check("html".equals(cb.starts.get(0)), "start html");
        check("head".equals(cb.starts.get(1)), "start head");
        check("body".equals(cb.starts.get(2)), "start body");
        check("p".equals(cb.starts.get(3)), "start p");
        check("b".equals(cb.starts.get(4)), "start b");
        check(cb.implied.get(0) && cb.implied.get(1) && cb.implied.get(2), "html/head/body implied");
        check(!cb.implied.get(3), "explicit p not implied");
        check(cb.ends.contains("b") && cb.ends.contains("p") && cb.ends.contains("body") && cb.ends.contains("html"), "end tags present");
        check(cb.texts.size() == 2, "two text chunks");
        check("Hello ".equals(cb.texts.get(0)), "text Hello ");
        check("world".equals(cb.texts.get(1)), "text world");
        check("\n".equals(cb.eol), "end-of-line string newline");

        CB cb2 = new CB();
        dp.parse(new StringReader("<br>foo"), cb2, false);
        check(cb2.simples.contains("br"), "simple tag br");
        check(cb2.texts.contains("foo"), "text foo");

        CB cb3 = new CB();
        dp.parse(new StringReader("<!-- note -->"), cb3, false);
        check(cb3.comments.size() == 1, "one comment");
        check(cb3.comments.get(0).contains("note"), "comment text note");

        CB cb4 = new CB();
        dp.parse(new StringReader("<p>Hi</p>"), cb4, true);
        check(cb4.starts.contains("p"), "ignoreCharSet=true still emits p");
        check(cb4.texts.contains("Hi"), "ignoreCharSet=true still emits text");
        check("\n".equals(cb4.eol), "ignoreCharSet eol newline");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
