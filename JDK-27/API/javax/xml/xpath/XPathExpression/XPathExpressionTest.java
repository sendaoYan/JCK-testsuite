/*
 * @test
 * @summary javax.xml.xpath.XPathExpression conformance: white-box structure + black-box behavior.
 * @run main XPathExpressionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathExpressionTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathExpression");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "evaluate"), "method evaluate");
        check(hasMethod(c, "evaluateExpression"), "method evaluateExpression");

        javax.xml.xpath.XPathExpression e = javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("1+1");
        check("2".equals(e.evaluate((Object) null)), "evaluate");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.parse(new java.io.ByteArrayInputStream("<root><item id='a'>one</item><item id='b'>two</item></root>".getBytes("UTF-8")));
        javax.xml.xpath.XPathExpression e = javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("/root/item[2]");
        check("two".equals(e.evaluate(d)), "evaluate(document) string");
        check("two".equals(e.evaluate(new org.xml.sax.InputSource(new java.io.StringReader("<root><item id='a'>one</item><item id='b'>two</item></root>")))), "evaluate(InputSource) string");
        javax.xml.xpath.XPathExpression cnt = javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("count(/root/item)");
        check("2".equals(cnt.evaluate(d)), "count evaluate");
        org.w3c.dom.NodeList nl = (org.w3c.dom.NodeList) javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate("/root/item", d, javax.xml.xpath.XPathConstants.NODESET);
        check(nl.getLength() == 2, "nodeset length");
        check("item".equals(nl.item(0).getNodeName()), "nodeset node name");
        boolean ok = false;
        try { javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("//[bad"); }
        catch (javax.xml.xpath.XPathExpressionException ex) { ok = true; }
        check(ok, "invalid expression -> XPathExpressionException");
        javax.xml.xpath.XPathEvaluationResult<?> res = javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluateExpression("/root/item[1]/text()", d);
        check(res.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODESET, "evaluateExpression result type");
        javax.xml.xpath.XPathNodes nodes = (javax.xml.xpath.XPathNodes) res.value();
        check(nodes.size() == 1 && "one".equals(nodes.get(0).getNodeValue()), "evaluateExpression value");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
