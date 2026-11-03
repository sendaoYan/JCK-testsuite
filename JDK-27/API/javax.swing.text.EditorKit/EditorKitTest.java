/*
 * @test
 * @summary javax.swing.text.EditorKit conformance: white-box structure + black-box behavior.
 * @run main EditorKitTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;
import javax.swing.JEditorPane;

public class EditorKitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.EditorKit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        DefaultEditorKit kit = new DefaultEditorKit();
        check(kit instanceof EditorKit, "DefaultEditorKit is EditorKit");
        check("text/plain".equals(kit.getContentType()), "content type");
        check(kit.createDefaultDocument() instanceof Document, "default document is Document");
        check(kit.getActions().length > 0, "actions non-empty");

        // clone() contract
        Object clone = kit.clone();
        check(clone != null && clone != kit, "clone distinct");
        check(clone instanceof DefaultEditorKit, "clone type");
        check("text/plain".equals(((DefaultEditorKit) clone).getContentType()), "clone content type");

        // install / deinstall are no-ops on DefaultEditorKit
        JEditorPane pane = new JEditorPane();
        kit.install(pane);
        kit.deinstall(pane);
        check(true, "install/deinstall no-op");

        // EditorKit interface contract exercised through a reference
        EditorKit ek = kit;
        Document doc = ek.createDefaultDocument();
        ek.read(new StringReader("hi"), doc, 0);
        check("hi".equals(doc.getText(0, doc.getLength())), "read via EditorKit");
        StringWriter sw = new StringWriter();
        ek.write(sw, doc, 0, doc.getLength());
        check("hi".equals(sw.toString()), "write via EditorKit");

        // A second concrete EditorKit
        StyledEditorKit sek = new StyledEditorKit();
        check(sek instanceof EditorKit, "StyledEditorKit is EditorKit");
        check("text/plain".equals(sek.getContentType()), "styled content type");
        check(sek.getViewFactory() != null, "styled view factory non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
