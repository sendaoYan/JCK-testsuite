/*
 * @test
 * @summary javax.swing.JPasswordField conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JPasswordFieldTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.swing.JPasswordField;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JPasswordFieldTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JPasswordField");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JTextField"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default construction: empty text, echo char set by L&F.
        JPasswordField pf = new JPasswordField();
        check("".equals(pf.getText()), "default text empty");
        check(pf.getColumns() == 0, "default columns 0");
        check(pf.getEchoChar() != 0, "default echo char set");
        check(pf.echoCharIsSet(), "default echoCharIsSet");
        check("PasswordFieldUI".equals(pf.getUIClassID()), "getUIClassID PasswordFieldUI");
        check(pf.getDocument() instanceof PlainDocument, "default document PlainDocument");
        check(Arrays.equals(new char[0], pf.getPassword()), "default password empty");

        // Text constructor roundtrip through getText/getPassword.
        JPasswordField pf1 = new JPasswordField("secret");
        check("secret".equals(pf1.getText()), "JPasswordField(String) text");
        check(Arrays.equals(new char[]{'s','e','c','r','e','t'}, pf1.getPassword()), "getPassword matches");

        // Column constructor variants.
        JPasswordField pf2 = new JPasswordField(15);
        check(pf2.getColumns() == 15, "JPasswordField(int) columns");
        check("".equals(pf2.getText()), "JPasswordField(int) empty text");
        JPasswordField pf3 = new JPasswordField("pw", 8);
        check("pw".equals(pf3.getText()), "JPasswordField(String,int) text");
        check(pf3.getColumns() == 8, "JPasswordField(String,int) columns");

        // Document constructor honors supplied model.
        Document d = new PlainDocument();
        JPasswordField pf4 = new JPasswordField(d, "docpw", 4);
        check(pf4.getDocument() == d, "custom document preserved");
        check("docpw".equals(pf4.getText()), "document ctor text");
        check(pf4.getColumns() == 4, "document ctor columns");

        // setText/getText + caret chain.
        check(pf.getCaretPosition() == 0, "caret starts at 0");
        pf.setText("newpass");
        check("newpass".equals(pf.getText()), "setText/getText");
        check(Arrays.equals(new char[]{'n','e','w','p','a','s','s'}, pf.getPassword()), "getPassword after setText");
        check(pf.getCaretPosition() >= 0 && pf.getCaretPosition() <= 7, "caret within bounds after setText");
        pf.setCaretPosition(4);
        check(pf.getCaretPosition() == 4, "setCaretPosition/getCaretPosition");

        // getText(int,int) portion.
        check("pass".equals(pf.getText(3, 4)), "getText(off,len)");

        // Echo char roundtrip.
        pf.setEchoChar('$');
        check(pf.getEchoChar() == '$', "setEchoChar/getEchoChar");
        check(pf.echoCharIsSet(), "echoCharIsSet after setEchoChar('$')");
        pf.setEchoChar((char) 0);
        check(pf.getEchoChar() == 0, "setEchoChar(0) clears echo char");
        check(!pf.echoCharIsSet(), "echoCharIsSet false after clear");
        pf.setEchoChar('*');
        check(pf.getEchoChar() == '*', "echo char restored");

        // Columns roundtrip.
        pf3.setColumns(12);
        check(pf3.getColumns() == 12, "setColumns/getColumns");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JPasswordFieldTest::testWhiteBox);
        runOnEDT(JPasswordFieldTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
