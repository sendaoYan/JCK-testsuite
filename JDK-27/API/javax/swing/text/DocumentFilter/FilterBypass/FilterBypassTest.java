/*
 * @test
 * @summary javax.swing.text.DocumentFilter.FilterBypass conformance: type shape via reflection (headless-safe).
 * @run main FilterBypassTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FilterBypassTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DocumentFilter$FilterBypass", false, FilterBypassTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDocument"), "method getDocument");
        check(names.contains("remove"), "method remove");
        check(names.contains("insertString"), "method insertString");
        check(names.contains("replace"), "method replace");

        // a concrete bypass drives the document it was created for
        final javax.swing.text.PlainDocument doc = new javax.swing.text.PlainDocument();
        javax.swing.text.DocumentFilter.FilterBypass fb = new javax.swing.text.DocumentFilter.FilterBypass() {
            public javax.swing.text.Document getDocument() { return doc; }
            public void remove(int offset, int length) throws javax.swing.text.BadLocationException {
                getDocument().remove(offset, length);
            }
            public void insertString(int offset, String string, javax.swing.text.AttributeSet attr)
                    throws javax.swing.text.BadLocationException {
                getDocument().insertString(offset, string, attr);
            }
            public void replace(int offset, int length, String string, javax.swing.text.AttributeSet attr)
                    throws javax.swing.text.BadLocationException {
                ((javax.swing.text.AbstractDocument) getDocument()).replace(offset, length, string, attr);
            }
        };
        check(fb.getDocument() == doc, "getDocument returns the document");
        fb.insertString(0, "hello", null);
        check(doc.getLength() == 5, "bypass insert applied");
        fb.replace(0, 5, "bye", null);
        check("bye".equals(doc.getText(0, 3)), "bypass replace applied");
        fb.remove(0, 3);
        check(doc.getLength() == 0, "bypass remove applied");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
