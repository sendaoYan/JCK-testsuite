/*
 * @test
 * @summary javax.swing.text.html.parser.Parser conformance: white-box structure + black-box behavior.
 * @run main ParserTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.ChangedCharSetException;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Parser;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.parser.TagElement;

public class ParserTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.Parser");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.html.parser.DTDConstants"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class Capture extends Parser {
        final List<String> starts = new ArrayList<String>();
        final List<String> ends = new ArrayList<String>();
        final List<String> empties = new ArrayList<String>();
        final List<String> texts = new ArrayList<String>();
        final List<String> comments = new ArrayList<String>();
        final List<Boolean> fictional = new ArrayList<Boolean>();

        Capture(DTD dtd) { super(dtd); }

        protected void handleStartTag(TagElement tag) {
            starts.add(tag.getElement().getName());
            fictional.add(tag.fictional());
        }
        protected void handleEndTag(TagElement tag) { ends.add(tag.getElement().getName()); }
        protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException { empties.add(tag.getElement().getName()); }
        protected void handleText(char[] text) { texts.add(new String(text)); }
        protected void handleComment(char[] text) { comments.add(new String(text)); }

        int line() { return getCurrentLine(); }
        int pos() { return getCurrentPos(); }
    }

    static void testBlackBox() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");

        Capture c = new Capture(dtd);
        c.parse(new StringReader("<p>Hello</p>"));
        check(c.starts.size() == 4, "starts count == 4, got " + c.starts);
        check("html".equals(c.starts.get(0)), "start html");
        check("head".equals(c.starts.get(1)), "start head");
        check("body".equals(c.starts.get(2)), "start body");
        check("p".equals(c.starts.get(3)), "start p");
        check(c.fictional.get(0) && c.fictional.get(1) && c.fictional.get(2), "html/head/body fictional");
        check(!c.fictional.get(3), "explicit p not fictional");
        check(c.texts.contains("Hello"), "text Hello");
        check(c.ends.contains("p") && c.ends.contains("body") && c.ends.contains("html"), "end tags present");
        check(c.line() == 1, "single line input line == 1");

        Capture c2 = new Capture(dtd);
        c2.parse(new StringReader("<br>foo"));
        check(c2.empties.contains("br"), "empty tag br");
        check(c2.texts.contains("foo"), "text foo");

        Capture c3 = new Capture(dtd);
        c3.parse(new StringReader("<!-- note -->"));
        check(c3.comments.size() == 1, "one comment");
        check(c3.comments.get(0).contains("note"), "comment text note");
        check(c3.pos() >= 0, "current position non-negative");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
