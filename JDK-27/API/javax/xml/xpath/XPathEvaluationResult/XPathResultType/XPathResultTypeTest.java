/*
 * @test
 * @summary javax.xml.xpath.XPathEvaluationResult.XPathResultType conformance: white-box structure + black-box behavior.
 * @run main XPathResultTypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathResultTypeTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathEvaluationResult$XPathResultType");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == java.lang.Enum.class, "superclass Enum");
        check(hasMethod(c, "getQNameType"), "method getQNameType");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "ANY"), "field ANY");
        check(hasField(c, "BOOLEAN"), "field BOOLEAN");
        check(hasField(c, "NODE"), "field NODE");
        check(hasField(c, "NODESET"), "field NODESET");
        check(hasField(c, "NUMBER"), "field NUMBER");
        check(hasField(c, "STRING"), "field STRING");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 6, "6 enum constants");
        check(hasField(c, "ANY"), "enum constant ANY");
        check(hasField(c, "BOOLEAN"), "enum constant BOOLEAN");
        check(hasField(c, "NODE"), "enum constant NODE");
        check(hasField(c, "NODESET"), "enum constant NODESET");
        check(hasField(c, "NUMBER"), "enum constant NUMBER");
        check(hasField(c, "STRING"), "enum constant STRING");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.ANY != null, "ANY");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.BOOLEAN != null, "BOOLEAN");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.NUMBER != null, "NUMBER");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.STRING != null, "STRING");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODESET != null, "NODESET");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODE != null, "NODE");
        check("BOOLEAN".equals(javax.xml.xpath.XPathEvaluationResult.XPathResultType.BOOLEAN.toString()), "toString");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.valueOf("NUMBER") == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NUMBER, "valueOf");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.values().length == 6, "six enum constants");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMapping();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static void testMapping() {
        javax.xml.xpath.XPathEvaluationResult.XPathResultType[] vals =
            javax.xml.xpath.XPathEvaluationResult.XPathResultType.values();
        // enum order: ANY, BOOLEAN, NUMBER, STRING, NODESET, NODE (javadoc order)
        check(vals.length == 6, "six constants");
        check(vals[0] == javax.xml.xpath.XPathEvaluationResult.XPathResultType.ANY, "ordinal 0 is ANY");

        // getQNameType: QName mapping for supported class types; null otherwise (locked actual)
        javax.xml.namespace.QName qnStr =
            javax.xml.xpath.XPathEvaluationResult.XPathResultType.getQNameType(String.class);
        check(qnStr != null && "STRING".equals(qnStr.getLocalPart()),
              "getQNameType(String.class) local STRING");
        check(qnStr.getNamespaceURI() != null && !qnStr.getNamespaceURI().isEmpty(),
              "getQNameType(String.class) namespace non-empty");
        javax.xml.namespace.QName qnBool =
            javax.xml.xpath.XPathEvaluationResult.XPathResultType.getQNameType(Boolean.class);
        check(qnBool != null && "BOOLEAN".equals(qnBool.getLocalPart()),
              "getQNameType(Boolean.class) local BOOLEAN");
        javax.xml.namespace.QName qnNum =
            javax.xml.xpath.XPathEvaluationResult.XPathResultType.getQNameType(Double.class);
        check(qnNum != null && "NUMBER".equals(qnNum.getLocalPart()),
              "getQNameType(Double.class) local NUMBER");
        javax.xml.namespace.QName qnNode =
            javax.xml.xpath.XPathEvaluationResult.XPathResultType.getQNameType(org.w3c.dom.Node.class);
        check(qnNode != null && "NODE".equals(qnNode.getLocalPart()),
              "getQNameType(Node.class) local NODE");
        javax.xml.namespace.QName qnNodes =
            javax.xml.xpath.XPathEvaluationResult.XPathResultType.getQNameType(javax.xml.xpath.XPathNodes.class);
        check(qnNodes != null && "NODESET".equals(qnNodes.getLocalPart()),
              "getQNameType(XPathNodes.class) local NODESET");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.getQNameType(java.util.List.class) == null,
              "getQNameType(unsupported class) null");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.getQNameType(null) == null,
              "getQNameType(null) null");

        // valueOf round-trip for each name; bogus -> IllegalArgumentException
        for (javax.xml.xpath.XPathEvaluationResult.XPathResultType rt : vals) {
            check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.valueOf(rt.name()) == rt,
                  "valueOf round-trip " + rt.name());
        }
        boolean bogus = false;
        try { javax.xml.xpath.XPathEvaluationResult.XPathResultType.valueOf("BOGUS"); }
        catch (IllegalArgumentException e) { bogus = true; }
        check(bogus, "valueOf(BOGUS) -> IllegalArgumentException");

        // correlation with generic evaluateExpression types
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.NUMBER.name()
                  .equals(javax.xml.xpath.XPathConstants.NUMBER.getLocalPart()),
              "NUMBER enum name matches XPathConstants.NUMBER localPart");
        check(javax.xml.xpath.XPathEvaluationResult.XPathResultType.STRING.name()
                  .equals(javax.xml.xpath.XPathConstants.STRING.getLocalPart()),
              "STRING enum name matches XPathConstants.STRING localPart");
    }
}
