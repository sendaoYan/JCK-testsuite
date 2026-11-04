/*
 * @test
 * @summary javax.swing.text.ElementIterator conformance: depth-first traversal, first/current/next/previous/depth, clone independence, null handling (headless-safe).
 * @run main ElementIteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.PlainDocument;

public class ElementIteratorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ElementIterator", false, ElementIteratorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("clone"), "method clone");
        check(names.contains("first"), "method first");
        check(names.contains("depth"), "method depth");
        check(names.contains("current"), "method current");
        check(names.contains("next"), "method next");
        check(names.contains("previous"), "method previous");

        testDocumentIteration();
        testNavigation();
        testClone();
        testNullsAndExhaustion();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testDocumentIteration() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "ab\ncd\nef", null);
        ElementIterator it = new ElementIterator(d);
        StringBuilder sb = new StringBuilder();
        Element e;
        while ((e = it.next()) != null) {
            sb.append(e.getName()).append("@").append(e.getStartOffset()).append(" ");
        }
        check(sb.toString().equals("paragraph@0 content@0 content@3 content@6 "),
                "depth-first document order, got [" + sb + "]");
        check(it.next() == null, "iteration stays exhausted");
        check(it.current() == null, "current null when exhausted");

        // starting from an element instead of a document iterates that subtree
        Element root = d.getDefaultRootElement();
        ElementIterator sub = new ElementIterator(root);
        sb = new StringBuilder();
        while ((e = sub.next()) != null) sb.append(e.getName()).append(" ");
        check(sb.toString().equals("paragraph content content content "), "element-rooted iteration");

        // styled documents walk the full three-level tree
        DefaultStyledDocument sd = new DefaultStyledDocument();
        sd.insertString(0, "one", null);
        ElementIterator sit = new ElementIterator(sd);
        sb = new StringBuilder();
        while ((e = sit.next()) != null) sb.append(e.getName()).append(" ");
        check(sb.toString().equals("section paragraph content "), "styled tree order");
    }

    static void testNavigation() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "ab\ncd", null);
        Element root = d.getDefaultRootElement();
        ElementIterator it = new ElementIterator(d);
        check(it.first() == root, "first is the default root");
        check(it.current() == root, "current after first");
        check(it.depth() == 1, "depth of root is 1");
        Element n1 = it.next();
        check(n1 == root.getElement(0), "next descends to first child");
        check(it.current() == n1, "current tracks next");
        check(it.depth() == 1, "depth of a leaf is 1");
        check(it.previous() == root, "previous returns the parent");
        check(it.current() == n1, "previous does not move current");
        // after previous the walk continues with the following sibling
        Element r2 = it.next();
        check(r2 == root.getElement(1), "next after previous resumes traversal");
        check(it.current() == r2, "current tracks next again");
        // first() restarts the walk
        check(it.first() == root, "first restarts");
        check(it.next() == root.getElement(0), "next after restart");
    }

    static void testClone() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "a\nb\nc", null);
        ElementIterator it = new ElementIterator(d);
        it.next(); it.next(); it.next(); // positioned on the first leaf
        ElementIterator clone = (ElementIterator) it.clone();
        check(clone != it, "clone is a distinct object");
        check(clone.current() == it.current(), "clone starts at the same position");
        // advancing the clone does not disturb the original
        Element origCurrent = it.current();
        clone.next();
        check(it.current() == origCurrent, "original position unaffected by clone advance");
        Element a = it.next();
        Element b = clone.previous();
        check(a != null && b != null, "both iterators still navigable");
    }

    static void testNullsAndExhaustion() throws Exception {
        ElementIterator empty = new ElementIterator(new PlainDocument());
        check(empty.first() != null, "empty document still has a root");
        check(empty.next() != null, "root has a line child");
        ElementIterator nulled = new ElementIterator((Element) null);
        check(nulled.first() == null, "element-null first null");
        check(nulled.next() == null, "element-null next null");
        check(nulled.current() == null, "element-null current null");
        check(nulled.previous() == null, "element-null previous null");
        check(nulled.depth() == 0, "element-null depth 0");
        try {
            new ElementIterator((Document) null);
            check(false, "document-null should throw NPE");
        } catch (NullPointerException e) {
            check(true, "document-null NPE");
        }
    }
}
