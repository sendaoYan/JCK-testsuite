/*
 * @test
 * @summary javax.xml.xpath.XPathEvaluationResult conformance: white-box structure + black-box behavior.
 * @run main XPathEvaluationResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathEvaluationResultTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathEvaluationResult");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "type"), "method type");
        check(hasMethod(c, "value"), "method value");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.xpath.XPathEvaluationResult a = new javax.xml.xpath.XPathEvaluationResult(){
            public javax.xml.xpath.XPathEvaluationResult.XPathResultType type() { return null; }
            public java.lang.Object value() { return null; }
        };
        check(a != null, "anon impl of XPathEvaluationResult");
        check(a.type() == null, "type() returns fixed default");
        check(a.value() == null, "value() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealResults();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    // Real XPathEvaluationResult objects produced by the runtime.
    static void testRealResults() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        org.w3c.dom.Document d = dbf.newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root><a>1</a><a>2</a></root>".getBytes("UTF-8")));
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();

        javax.xml.xpath.XPathEvaluationResult<?> num = xp.evaluateExpression("count(//a)", d);
        check(num.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NUMBER,
              "NUMBER type for count()");
        check(num.value() instanceof Double && ((Double) num.value()).doubleValue() == 2.0,
              "NUMBER value Double 2.0");

        javax.xml.xpath.XPathEvaluationResult<?> bool = xp.evaluateExpression("count(//a) = 2", d);
        check(bool.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.BOOLEAN
              && Boolean.TRUE.equals(bool.value()), "BOOLEAN type+value");

        javax.xml.xpath.XPathEvaluationResult<?> str = xp.evaluateExpression("string(/root/a[1])", d);
        check(str.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.STRING
              && "1".equals(str.value()), "STRING type+value");

        javax.xml.xpath.XPathEvaluationResult<?> sel = xp.evaluateExpression("//a", d);
        check(sel.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODESET,
              "NODESET type for XPath 1.0 node selection (locked actual)");
        check(sel.value() instanceof javax.xml.xpath.XPathNodes, "NODESET value is XPathNodes");

        // constant expressions without a document context
        javax.xml.xpath.XPathEvaluationResult<?> konst = xp.evaluateExpression("1+1", (Object) null);
        check(konst.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NUMBER
              && ((Double) konst.value()).doubleValue() == 2.0,
              "constant expression generic result");

        // typed class form consistency with generic form
        String typed = xp.evaluateExpression("string(/root/a[1])", d, String.class);
        check("1".equals(typed) && "1".equals(str.value()),
              "typed String.class equals generic STRING value");
    }
}
