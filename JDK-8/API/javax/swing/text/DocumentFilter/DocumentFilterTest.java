/*
 * @test
 * @summary javax.swing.text.DocumentFilter conformance: default insert/remove/replace delegation to FilterBypass with exact arguments (headless-safe).
 * @run main DocumentFilterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.DocumentFilter;

public class DocumentFilterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    /** Records every mutation routed through the bypass. */
    static class RecordingBypass extends DocumentFilter.FilterBypass {
        String log = "";
        public javax.swing.text.Document getDocument() { return null; }
        public void remove(int offset, int length) { log += "remove:" + offset + ":" + length + ";"; }
        public void insertString(int offset, String string, AttributeSet attr) {
            log += "insert:" + offset + ":" + string + ":" + attr + ";";
        }
        public void replace(int offset, int length, String string, AttributeSet attr) {
            log += "replace:" + offset + ":" + length + ":" + string + ":" + attr + ";";
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DocumentFilter", false, DocumentFilterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("remove"), "method remove");
        check(names.contains("insertString"), "method insertString");
        check(names.contains("replace"), "method replace");

        Class<?> fb = Class.forName("javax.swing.text.DocumentFilter$FilterBypass", false, DocumentFilterTest.class.getClassLoader());
        check(Modifier.isAbstract(fb.getModifiers()), "FilterBypass abstract");

        // default behaviour: everything is routed verbatim to the bypass
        DocumentFilter plain = new DocumentFilter();
        RecordingBypass rb = new RecordingBypass();
        javax.swing.text.SimpleAttributeSet attrs = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setBold(attrs, true);
        plain.insertString(rb, 3, "abc", attrs);
        check(rb.log.equals("insert:3:abc:" + attrs + ";"), "insertString delegates verbatim, got [" + rb.log + "]");
        plain.remove(rb, 1, 2);
        check(rb.log.endsWith("remove:1:2;"), "remove delegates verbatim");
        plain.replace(rb, 0, 4, "Z", null);
        check(rb.log.endsWith("replace:0:4:Z:null;"), "replace delegates verbatim");

        // an overriding filter decides what reaches the bypass
        DocumentFilter upper = new DocumentFilter() {
            public void insertString(FilterBypass fb2, int offset, String string, AttributeSet attr)
                    throws javax.swing.text.BadLocationException {
                fb2.insertString(offset, string.toUpperCase(), attr);
            }
            public void remove(FilterBypass fb2, int offset, int length)
                    throws javax.swing.text.BadLocationException {
                // swallow all removals
            }
        };
        RecordingBypass rb2 = new RecordingBypass();
        upper.insertString(rb2, 0, "keep", null);
        check(rb2.log.equals("insert:0:KEEP:null;"), "override rewrites the text en route");
        upper.remove(rb2, 0, 2);
        check(rb2.log.equals("insert:0:KEEP:null;"), "blocking override forwards nothing");
        upper.replace(rb2, 0, 1, "y", null); // not overridden: default routing
        check(rb2.log.endsWith("replace:0:1:y:null;"), "non-overridden replace still routes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
