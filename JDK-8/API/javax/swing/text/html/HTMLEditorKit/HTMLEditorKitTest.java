/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit conformance: action table,
 *          constants, cursors, lifecycle, read/write/insertHTML contracts and
 *          negative cases (headless-safe).
 * @run main HTMLEditorKitTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class HTMLEditorKitTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit", false, HTMLEditorKitTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContentType"), "method getContentType");
        check(names.contains("getViewFactory"), "method getViewFactory");
        check(names.contains("createDefaultDocument"), "method createDefaultDocument");
        check(names.contains("read"), "method read");
        check(names.contains("insertHTML"), "method insertHTML");
        check(names.contains("write"), "method write");
        check(names.contains("install"), "method install");
    }

    // ---- Part 2: constants ----
    static void testConstants() {
        check("default.css".equals(HTMLEditorKit.DEFAULT_CSS), "DEFAULT_CSS");
        check("html-bold-action".equals(HTMLEditorKit.BOLD_ACTION), "BOLD_ACTION");
        check("html-color-action".equals(HTMLEditorKit.COLOR_ACTION), "COLOR_ACTION");
        check("html-font-bigger".equals(HTMLEditorKit.FONT_CHANGE_BIGGER), "FONT_CHANGE_BIGGER");
        check("html-para-indent-left".equals(HTMLEditorKit.PARA_INDENT_LEFT), "PARA_INDENT_LEFT");
        check("html-image-align-top".equals(HTMLEditorKit.IMG_ALIGN_TOP), "IMG_ALIGN_TOP");
        check("_implied_".equals(HTMLEditorKit.ParserCallback.IMPLIED), "ParserCallback.IMPLIED");
    }

    // ---- Part 3: action table ----
    static void testActions() {
        Action[] acts = kit.getActions();
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

    // ---- Part 4: lifecycle and accessors ----
    static void testLifecycle() throws Exception {
        check("text/html".equals(kit.getContentType()), "content type");
        ViewFactory vf = kit.getViewFactory();
        check(vf != null && "javax.swing.text.html.HTMLEditorKit$HTMLFactory".equals(vf.getClass().getName()),
              "view factory is HTMLFactory");
        Document doc = kit.createDefaultDocument();
        check(doc instanceof HTMLDocument, "default document is HTMLDocument");
        check(doc.getLength() == 0, "default document empty");

        HTMLEditorKit clone = (HTMLEditorKit) kit.clone();
        check(clone != kit, "clone is a distinct instance");
        check("text/html".equals(clone.getContentType()), "clone keeps content type");

        check(kit.getStyleSheet() != null, "default style sheet non-null");
        StyleSheet mine = new StyleSheet();
        kit.setStyleSheet(mine);
        check(kit.getStyleSheet() == mine, "setStyleSheet identity round-trip");
        kit.setStyleSheet(new StyleSheet());
        check(kit.getStyleSheet() != mine, "second setStyleSheet replaces");

        check(kit.getInputAttributes() != null, "input attributes non-null");
        check(kit.isAutoFormSubmission() == true, "auto form submission default");
        kit.setAutoFormSubmission(false);
        check(kit.isAutoFormSubmission() == false, "setAutoFormSubmission round-trip");
        kit.setAutoFormSubmission(true);

        check("Default Cursor".equals(kit.getDefaultCursor().getName()), "default cursor name");
        check("Hand Cursor".equals(kit.getLinkCursor().getName()), "link cursor name");
        java.awt.Cursor hand = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR);
        kit.setDefaultCursor(hand);
        kit.setLinkCursor(hand);
        check(kit.getDefaultCursor() == hand, "setDefaultCursor round-trip");
        check(kit.getLinkCursor() == hand, "setLinkCursor round-trip");

        JEditorPane ep = new JEditorPane();
        boolean ok = true;
        try { kit.install(ep); kit.deinstall(ep); } catch (Throwable t) { ok = false; }
        check(ok, "install/deinstall on a JEditorPane tolerated");
    }

    // ---- Part 5: read / write / insertHTML contracts ----
    static void testReadWriteContracts() throws Exception {
        HTMLDocument dd = (HTMLDocument) kit.createDefaultDocument();

        boolean ok1 = true;
        try { kit.read((java.io.Reader) null, dd, 0); } catch (Throwable t) { ok1 = false; }
        check(ok1, "read(null reader) tolerated: parser handles it via its error handler");

        boolean npe2 = false;
        try { kit.read(new StringReader("<p>x</p>"), null, 0); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "read(null doc) NPE");

        boolean ble = false;
        try { kit.read(new StringReader("<p>x</p>"), dd, 1000); }
        catch (BadLocationException e) { ble = true; }
        check(ble, "read past end BadLocationException");

        boolean npe3 = false;
        try { kit.write((Writer) null, dd, 0, 0); } catch (NullPointerException e) { npe3 = true; }
        check(npe3, "write(null writer) NPE");

        boolean npe4 = false;
        try { kit.write(new StringWriter(), null, 0, 0); } catch (NullPointerException e) { npe4 = true; }
        check(npe4, "write(null doc) NPE");

        boolean wrote = true;
        try {
            StringWriter sw = new StringWriter();
            kit.write(sw, dd, 0, dd.getLength());
        } catch (Throwable t) { wrote = false; }
        check(wrote, "write of empty document tolerated");

        boolean npe5 = false;
        try { kit.insertHTML(null, 0, "<b>b</b>", 0, 0, HTML.Tag.B); }
        catch (NullPointerException e) { npe5 = true; }
        check(npe5, "insertHTML(null doc) NPE");

        boolean npe6 = false;
        try { kit.insertHTML(dd, 0, null, 0, 0, HTML.Tag.B); }
        catch (NullPointerException e) { npe6 = true; }
        check(npe6, "insertHTML(null html) NPE");

        boolean npe7 = false;
        try { kit.install(null); } catch (NullPointerException e) { npe7 = true; }
        check(npe7, "install(null) NPE");

        boolean npe8 = false;
        try { kit.deinstall(null); } catch (NullPointerException e) { npe8 = true; }
        check(npe8, "deinstall(null) NPE");

        // positive insertHTML: inline content inserted at an arbitrary offset
        HTMLDocument d7 = load("<html><body><p>one</p><p>two</p></body></html>");
        Element body = d7.getDefaultRootElement().getElement(1);
        check("one".equals(d7.getText(body.getElement(0).getStartOffset(), 3)), "body starts with one");
        kit.insertHTML(d7, 3, "<b>bold</b>", 0, 0, HTML.Tag.B);
        String t = d7.getText(0, d7.getLength());
        check(t.equals("\nonbolde\ntwo"), "insertHTML splices bold into the first paragraph");
    }

    // ---- Part 6: LinkController serialization ----
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
        Locale.setDefault(Locale.ENGLISH);
        testShape();
        testConstants();
        testActions();
        testLifecycle();
        testReadWriteContracts();
        testLinkController();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
