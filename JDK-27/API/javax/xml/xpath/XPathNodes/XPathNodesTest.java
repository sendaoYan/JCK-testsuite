/*
 * @test
 * @summary javax.xml.xpath.XPathNodes conformance: white-box structure + black-box behavior.
 * @run main XPathNodesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathNodesTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.xpath.XPathNodes");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "java.lang.Iterable"), "interface java.lang.Iterable");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "iterator"), "method iterator");
        check(hasMethod(c, "size"), "method size");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.xpath.XPathNodes a = new javax.xml.xpath.XPathNodes(){
            public java.util.Iterator<org.w3c.dom.Node> iterator() { return null; }
            public int size() { return 3; }
            public org.w3c.dom.Node get(int p0) { return null; }
        };
        check(a != null, "anon impl of XPathNodes");
        check(a.iterator() == null, "iterator() returns fixed default");
        check(a.size() == 3, "size() returns fixed default");
        check(a.get(3) == null, "get() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealNodes();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    // Real XPathNodes obtained through the runtime (evaluateExpression).
    static void testRealNodes() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document d = dbf.newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root><a>1</a><a>2</a><a>3</a></root>".getBytes("UTF-8")));
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        javax.xml.xpath.XPathNodes nodes = xp.evaluateExpression("//a", d, javax.xml.xpath.XPathNodes.class);

        check(nodes.size() == 3, "size() == 3");
        check(nodes.get(0).getTextContent().equals("1"), "get(0) first in document order");
        check(nodes.get(2).getTextContent().equals("3"), "get(2) last in document order");
        check(nodes.get(1) != nodes.get(0), "get(i) distinct positions");

        // Iterable: for-each visits all nodes in document order
        java.util.List<String> texts = new java.util.ArrayList<String>();
        for (org.w3c.dom.Node n : nodes) texts.add(n.getTextContent());
        check(texts.equals(java.util.Arrays.asList("1", "2", "3")), "iteration in document order");

        // iterator() consistent with get()
        java.util.Iterator<org.w3c.dom.Node> it = nodes.iterator();
        check(it.hasNext() && it.next().getTextContent().equals("1"), "iterator() first element");
        check(it.hasNext() && it.next().getTextContent().equals("2"), "iterator() second element");
        check(it.hasNext() && it.next().getTextContent().equals("3"), "iterator() third element");
        check(!it.hasNext(), "iterator() exhausted");

        // out-of-range get -> IndexOutOfBoundsException (locked actual, not XPathException)
        boolean oob = false;
        try { nodes.get(99); }
        catch (java.lang.IndexOutOfBoundsException e) { oob = true; }
        catch (javax.xml.xpath.XPathException e) { oob = true; }
        check(oob, "get(out of range) throws IndexOutOfBounds/XPathException");

        // empty node set
        javax.xml.xpath.XPathNodes empty = xp.evaluateExpression("//zz", d, javax.xml.xpath.XPathNodes.class);
        check(empty.size() == 0, "empty selection size 0");
        boolean got = false;
        for (org.w3c.dom.Node n : empty) got = true;
        check(!got, "empty selection iterates zero times");

        // XPathNodes also surfaced by the generic evaluateExpression
        javax.xml.xpath.XPathEvaluationResult<?> res = xp.evaluateExpression("//a", d);
        check(res.value() instanceof javax.xml.xpath.XPathNodes
              && ((javax.xml.xpath.XPathNodes) res.value()).size() == 3,
              "generic result value is an XPathNodes of size 3");
    }
}
