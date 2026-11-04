/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.SectionElement conformance: type shape via reflection (headless-safe).
 * @run main SectionElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SectionElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$SectionElement", false, SectionElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$BranchElement"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");

        // the default root of a DefaultStyledDocument is a SectionElement
        javax.swing.text.DefaultStyledDocument d = new javax.swing.text.DefaultStyledDocument();
        javax.swing.text.Element root = d.getDefaultRootElement();
        check(root.getClass().getName().equals("javax.swing.text.DefaultStyledDocument$SectionElement"),
                "root is a SectionElement");
        check(root.getName().equals("section"), "section name");
        check(!root.isLeaf(), "section is a branch");
        check(root.getElementCount() == 1, "empty section has one paragraph");
        d.insertString(0, "ab\ncd", null);
        check(root.getStartOffset() == 0 && root.getEndOffset() == 6, "section spans [0,6) after insert");
        check(root.getElementCount() == 2, "section tracks paragraphs");
        check(root.getElement(0).getName().equals("paragraph"), "section children are paragraphs");
        check(root.getDocument() == d, "section documents back");
        check(root.getParentElement() == null, "section has no parent element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
