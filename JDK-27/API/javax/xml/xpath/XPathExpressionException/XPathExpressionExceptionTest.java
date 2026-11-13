/*
 * @test
 * @summary javax.xml.xpath.XPathExpressionException conformance: white-box structure + black-box behavior.
 * @run main XPathExpressionExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathExpressionExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathExpressionException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("javax.xml.xpath.XPathException"), "superclass javax.xml.xpath.XPathException");

        javax.xml.xpath.XPathExpressionException e = new javax.xml.xpath.XPathExpressionException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.xpath.XPathExpressionException e = new javax.xml.xpath.XPathExpressionException("bad");
        check("bad".equals(e.getMessage()) && e.getCause() == null, "message ctor");
        java.lang.Throwable c = new RuntimeException("r");
        check(new javax.xml.xpath.XPathExpressionException(c).getCause() == c, "throwable ctor");

    }

    public static void main(String[] args) throws Exception {
        // 固定默认区域为英文：XPath 语法错误消息随默认 Locale 本地化（如 zh_CN 为中文），断言的是英文消息
        Locale.setDefault(Locale.ENGLISH);
        testWhiteBox();
        testBlackBox();
        testConstructors();
        testRealTrigger();        System.out.println("PASS (" + passed + " assertions)");
    }


    static void testConstructors() throws Exception {
        // (String)
        javax.xml.xpath.XPathExpressionException e1 = new javax.xml.xpath.XPathExpressionException("bad expr");
        check("bad expr".equals(e1.getMessage()), "(String) message round-trip");
        check(e1.getCause() == null, "(String) cause null");
        // (Throwable)
        RuntimeException cause = new RuntimeException("root");
        javax.xml.xpath.XPathExpressionException e2 = new javax.xml.xpath.XPathExpressionException(cause);
        check(e2.getCause() == cause, "(Throwable) cause round-trip");
        check(e2.getMessage() != null && e2.getMessage().contains("root"), "(Throwable) message derived");
        // no (String,Throwable) overload in the JDK API (checked via reflection)
        int twoArg = 0;
        for (java.lang.reflect.Constructor<?> c : javax.xml.xpath.XPathExpressionException.class.getConstructors()) {
            if (c.getParameterTypes().length == 2) twoArg++;
        }
        check(twoArg == 0, "no (String,Throwable) constructor (locked API surface)");
        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e2);
        oos.close();
        javax.xml.xpath.XPathExpressionException back = (javax.xml.xpath.XPathExpressionException)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getCause() instanceof RuntimeException, "serialization round-trip cause");
    }

    // real trigger: invalid XPath expression through the runtime
    static void testRealTrigger() throws Exception {
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        boolean saw = false;
        try { xp.compile("//a["); }
        catch (javax.xml.xpath.XPathExpressionException e) {
            saw = e.getMessage() != null && e.getMessage().contains("A location path was expected");
        }
        check(saw, "compile('//a[') real trigger with syntax-error message");
    }
}

