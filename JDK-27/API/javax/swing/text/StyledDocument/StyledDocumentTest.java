/*
 * @test
 * @summary javax.swing.text.StyledDocument conformance: white-box structure + black-box behavior.
 * @run main StyledDocumentTest
 */

import java.lang.reflect.Modifier;
import java.awt.Color;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StyledDocumentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledDocument");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Document"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Verify the StyledDocument contract through its standard implementation.
        StyledDocument doc = new DefaultStyledDocument();
        doc.insertString(0, "styled text", null);

        // inherited Document contract
        check(doc.getLength() == 11, "length");
        check("styled text".equals(doc.getText(0, doc.getLength())), "getText");

        // styles
        Style s = doc.addStyle("heading", null);
        check(s != null, "addStyle non-null");
        check(doc.getStyle("heading") == s, "getStyle round-trip");
        doc.removeStyle("heading");
        check(doc.getStyle("heading") == null, "removeStyle");

        // logical styles
        Style logical = doc.addStyle("logical", null);
        doc.setLogicalStyle(0, logical);
        check(doc.getLogicalStyle(0) == logical, "set/get logical style");

        // character attributes
        SimpleAttributeSet ca = new SimpleAttributeSet();
        ca.addAttribute("ck", "cv");
        doc.setCharacterAttributes(0, 6, ca, false);
        check(doc.getCharacterElement(0).getAttributes().containsAttribute("ck", "cv"),
              "setCharacterAttributes reflected in character element");
        check(doc.getCharacterElement(0).isLeaf(), "character element is leaf");

        // paragraph attributes
        SimpleAttributeSet pa = new SimpleAttributeSet();
        pa.addAttribute("pk", "pv");
        doc.setParagraphAttributes(0, doc.getLength(), pa, false);
        check(doc.getParagraphElement(0).getAttributes().containsAttribute("pk", "pv"),
              "setParagraphAttributes reflected in paragraph element");
        check("paragraph".equals(doc.getParagraphElement(0).getName()), "paragraph element name");

        // color/font resolution
        Style colored = doc.addStyle("colored", null);
        StyleConstants.setForeground(colored, Color.GREEN);
        StyleConstants.setBackground(colored, Color.YELLOW);
        StyleConstants.setFontFamily(colored, "Monospaced");
        check(doc.getForeground(colored).equals(Color.GREEN), "getForeground");
        check(doc.getBackground(colored).equals(Color.YELLOW), "getBackground");
        check("Monospaced".equals(doc.getFont(colored).getFamily()), "getFont family");

        // paragraphs via elements
        Element para = doc.getParagraphElement(0);
        check(para != null && para.getStartOffset() == 0, "paragraph element start");
        Element firstRun = doc.getCharacterElement(0);
        check(firstRun.getStartOffset() == 0 && firstRun.getEndOffset() == 6,
              "styled prefix is its own character run");
        check(doc.getCharacterElement(6).getStartOffset() == 6,
              "character element starts at attribute boundary");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
