/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit conformance: white-box structure + black-box behavior.
 * @run main HTMLEditorKitTest
 */

import java.awt.Cursor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;

import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class HTMLEditorKitTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit"),
              "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"),
              "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();

        // content type / document / view factory
        check("text/html".equals(kit.getContentType()), "content type is text/html");
        Document doc = kit.createDefaultDocument();
        check(doc instanceof HTMLDocument, "createDefaultDocument returns HTMLDocument");
        check(kit.getViewFactory() != null, "view factory non-null");
        check(kit.getViewFactory() instanceof javax.swing.text.html.HTMLEditorKit.HTMLFactory,
              "view factory is HTMLFactory");

        // style sheet + cursors
        StyleSheet ss = kit.getStyleSheet();
        check(ss != null, "style sheet non-null");
        check(kit.getDefaultCursor() != null, "default cursor non-null");
        check(kit.getDefaultCursor().getType() == Cursor.DEFAULT_CURSOR,
              "default cursor is DEFAULT_CURSOR");
        check(kit.getLinkCursor().getType() == Cursor.HAND_CURSOR,
              "link cursor is HAND_CURSOR");

        // read: parse HTML into a document
        HTMLDocument hdoc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader("<html><body><b>bold</b></body></html>"), hdoc, 0);
        String text = hdoc.getText(0, hdoc.getLength());
        check(text.contains("bold"), "read() parses and stores text");
        check(hdoc.getLength() > 0, "document non-empty after read");

        // write: serialize the document to a writer
        StringWriter out = new StringWriter();
        kit.write(out, hdoc, 0, hdoc.getLength());
        String html = out.toString();
        check(html.contains("<html>"), "write() emits <html>");
        check(html.contains("<b>bold</b>"), "write() emits bold text");

        // actions
        Action[] actions = kit.getActions();
        check(actions.length > 0, "getActions non-empty");
        int htmlTextActions = 0;
        for (Action a : actions) {
            if (a instanceof javax.swing.text.html.HTMLEditorKit.HTMLTextAction) {
                htmlTextActions++;
            }
        }
        check(htmlTextActions > 0, "getActions contains HTMLTextAction instances");

        // input attributes
        check(kit.getInputAttributes() != null, "input attributes non-null");

        // auto form submission (instance state, no global side effects)
        check(kit.isAutoFormSubmission(), "auto form submission default true");
        kit.setAutoFormSubmission(false);
        check(!kit.isAutoFormSubmission(), "setAutoFormSubmission(false) roundtrip");
        kit.setAutoFormSubmission(true);
        check(kit.isAutoFormSubmission(), "setAutoFormSubmission(true) roundtrip");

        // style sheet set/get roundtrip
        StyleSheet replacement = new StyleSheet();
        kit.setStyleSheet(replacement);
        check(kit.getStyleSheet() == replacement, "setStyleSheet/getStyleSheet roundtrip");

        // cursor set/get roundtrip
        Cursor custom = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        kit.setDefaultCursor(custom);
        check(kit.getDefaultCursor() == custom, "setDefaultCursor/getDefaultCursor roundtrip");
        kit.setLinkCursor(custom);
        check(kit.getLinkCursor() == custom, "setLinkCursor/getLinkCursor roundtrip");

        // clone
        HTMLEditorKit clone = (HTMLEditorKit) kit.clone();
        check(clone != deepKit, "clone is a distinct instance");
        check(clone instanceof HTMLEditorKit, "clone is an HTMLEditorKit");
        check("text/html".equals(clone.getContentType()), "clone keeps content type");
    }

    static HTMLEditorKit deepKit = new HTMLEditorKit();

    static HTMLDocument deepLoad(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) deepKit.createDefaultDocument();
        deepKit.read(new StringReader(html), d, 0);
        return d;
    }

    static void testConstants() {
        check("default.css".equals(HTMLEditorKit.DEFAULT_CSS), "DEFAULT_CSS");
        check("html-bold-action".equals(HTMLEditorKit.BOLD_ACTION), "BOLD_ACTION");
        check("html-color-action".equals(HTMLEditorKit.COLOR_ACTION), "COLOR_ACTION");
        check("html-font-bigger".equals(HTMLEditorKit.FONT_CHANGE_BIGGER), "FONT_CHANGE_BIGGER");
        check("html-para-indent-left".equals(HTMLEditorKit.PARA_INDENT_LEFT), "PARA_INDENT_LEFT");
        check("html-image-align-top".equals(HTMLEditorKit.IMG_ALIGN_TOP), "IMG_ALIGN_TOP");
        check("_implied_".equals(HTMLEditorKit.ParserCallback.IMPLIED), "ParserCallback.IMPLIED");
    }

    static void testActions() {
        Action[] acts = deepKit.getActions();
        boolean jdk8 = System.getProperty("java.version").startsWith("1.8");
        check(acts.length == (jdk8 ? 85 : 87),
              "action count " + acts.length + " (" + (jdk8 ? "JDK-8" : "JDK-9+") + ")");
        Action[] defActs = new DefaultEditorKit().getActions();
        check(defActs.length == (jdk8 ? 55 : 57), "DefaultEditorKit action count");
        Set<String> defNames = new HashSet<String>();
        for (Action a : defActs) defNames.add(String.valueOf(a.getValue(Action.NAME)));
        Set<String> htmlNames = new HashSet<String>();
        int extra = 0;
        for (Action a : acts) {
            String n = String.valueOf(a.getValue(Action.NAME));
            htmlNames.add(n);
            if (!defNames.contains(n)) extra++;
        }
        check(htmlNames.size() == acts.length, "action names unique");
        check(extra == 30, "exactly 30 html-specific actions beyond DefaultEditorKit");
        for (String n : defNames) check(htmlNames.contains(n), "default action " + n + " included");
        // the html-only action names are identical across JDK-8 and JDK-9+
        String[] htmlOnly = {
            "InsertHR", "InsertOrderedList", "InsertOrderedListItem", "InsertPre",
            "InsertTable", "InsertTableDataCell", "InsertTableRow", "InsertUnorderedList",
            "InsertUnorderedListItem", "activate-link-action", "center-justify", "font-bold",
            "font-family-Monospaced", "font-family-SansSerif", "font-family-Serif",
            "font-italic", "font-size-10", "font-size-12", "font-size-14", "font-size-16",
            "font-size-18", "font-size-24", "font-size-36", "font-size-48", "font-size-8",
            "font-underline", "left-justify", "next-link-action", "previous-link-action",
            "right-justify",
        };
        Set<String> only = new HashSet<String>(htmlNames);
        only.removeAll(defNames);
        check(only.size() == htmlOnly.length, "html-only action set size 30");
        for (String n : htmlOnly) check(only.contains(n), "html-only action " + n);
    }

    static void testLifecycle() throws Exception {
        check("text/html".equals(deepKit.getContentType()), "content type");
        ViewFactory vf = deepKit.getViewFactory();
        check(vf != null && "javax.swing.text.html.HTMLEditorKit$HTMLFactory".equals(vf.getClass().getName()),
              "view factory is HTMLFactory");
        Document doc = deepKit.createDefaultDocument();
        check(doc instanceof HTMLDocument, "default document is HTMLDocument");
        check(doc.getLength() == 0, "default document empty");

        HTMLEditorKit clone = (HTMLEditorKit) deepKit.clone();
        check(clone != deepKit, "clone is a distinct instance");
        check("text/html".equals(clone.getContentType()), "clone keeps content type");

        check(deepKit.getStyleSheet() != null, "default style sheet non-null");
        StyleSheet mine = new StyleSheet();
        deepKit.setStyleSheet(mine);
        check(deepKit.getStyleSheet() == mine, "setStyleSheet identity round-trip");
        deepKit.setStyleSheet(new StyleSheet());
        check(deepKit.getStyleSheet() != mine, "second setStyleSheet replaces");

        check(deepKit.getInputAttributes() != null, "input attributes non-null");
        check(deepKit.isAutoFormSubmission() == true, "auto form submission default");
        deepKit.setAutoFormSubmission(false);
        check(deepKit.isAutoFormSubmission() == false, "setAutoFormSubmission round-trip");
        deepKit.setAutoFormSubmission(true);

        // 光标名通过 Toolkit.getProperty 本地化（中文 Windows 上不是
        // "Default Cursor"/"Hand Cursor"）；实现用的就是这两个预定义光标，
        // 因此与同类型的预定义光标名字比较，语义仍是“默认/链接光标类型正确”。
        check(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR).getName()
                  .equals(deepKit.getDefaultCursor().getName()), "default cursor name");
        check(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR).getName()
                  .equals(deepKit.getLinkCursor().getName()), "link cursor name");
        java.awt.Cursor hand = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR);
        deepKit.setDefaultCursor(hand);
        deepKit.setLinkCursor(hand);
        check(deepKit.getDefaultCursor() == hand, "setDefaultCursor round-trip");
        check(deepKit.getLinkCursor() == hand, "setLinkCursor round-trip");

        JEditorPane ep = new JEditorPane();
        boolean ok = true;
        try { deepKit.install(ep); deepKit.deinstall(ep); } catch (Throwable t) { ok = false; }
        check(ok, "install/deinstall on a JEditorPane tolerated");
    }

    static void testReadWriteContracts() throws Exception {
        HTMLDocument dd = (HTMLDocument) deepKit.createDefaultDocument();

        boolean ok1 = true;
        try { deepKit.read((java.io.Reader) null, dd, 0); } catch (Throwable t) { ok1 = false; }
        check(ok1, "read(null reader) tolerated: parser handles it via its error handler");

        boolean npe2 = false;
        try { deepKit.read(new StringReader("<p>x</p>"), null, 0); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "read(null doc) NPE");

        boolean ble = false;
        try { deepKit.read(new StringReader("<p>x</p>"), dd, 1000); }
        catch (BadLocationException e) { ble = true; }
        check(ble, "read past end BadLocationException");

        boolean npe3 = false;
        try { deepKit.write((Writer) null, dd, 0, 0); } catch (NullPointerException e) { npe3 = true; }
        check(npe3, "write(null writer) NPE");

        boolean npe4 = false;
        try { deepKit.write(new StringWriter(), null, 0, 0); } catch (NullPointerException e) { npe4 = true; }
        check(npe4, "write(null doc) NPE");

        boolean wrote = true;
        try {
            StringWriter sw = new StringWriter();
            deepKit.write(sw, dd, 0, dd.getLength());
        } catch (Throwable t) { wrote = false; }
        check(wrote, "write of empty document tolerated");

        boolean npe5 = false;
        try { deepKit.insertHTML(null, 0, "<b>b</b>", 0, 0, HTML.Tag.B); }
        catch (NullPointerException e) { npe5 = true; }
        check(npe5, "insertHTML(null doc) NPE");

        boolean npe6 = false;
        try { deepKit.insertHTML(dd, 0, null, 0, 0, HTML.Tag.B); }
        catch (NullPointerException e) { npe6 = true; }
        check(npe6, "insertHTML(null html) NPE");

        boolean npe7 = false;
        try { deepKit.install(null); } catch (NullPointerException e) { npe7 = true; }
        check(npe7, "install(null) NPE");

        boolean npe8 = false;
        try { deepKit.deinstall(null); } catch (NullPointerException e) { npe8 = true; }
        check(npe8, "deinstall(null) NPE");

        // positive insertHTML: inline content inserted at an arbitrary offset
        HTMLDocument d7 = deepLoad("<html><body><p>one</p><p>two</p></body></html>");
        Element body = d7.getDefaultRootElement().getElement(1);
        check("one".equals(d7.getText(body.getElement(0).getStartOffset(), 3)), "body starts with one");
        deepKit.insertHTML(d7, 3, "<b>bold</b>", 0, 0, HTML.Tag.B);
        String t = d7.getText(0, d7.getLength());
        check(t.equals("\nonbolde\ntwo"), "insertHTML splices bold into the first paragraph");
    }

    static void testLinkController() throws Exception {
        Class<?> lc = Class.forName("javax.swing.text.html.HTMLEditorKit$LinkController");
        check(ObjectStreamClass.lookup(lc).getSerialVersionUID() == 1338530902363040739L,
              "LinkController serialVersionUID stable");
        Object controller = lc.getDeclaredConstructor().newInstance();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(controller);
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == controller.getClass(), "LinkController serialization round-trip");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstants();
        testActions();
        testLifecycle();
        testReadWriteContracts();
        testLinkController();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
