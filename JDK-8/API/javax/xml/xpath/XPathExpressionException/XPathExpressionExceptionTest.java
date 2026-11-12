/*
 * @test
 * @summary javax.xml.xpath.XPathExpressionException conformance: exception hierarchy and constructors.
 * @run main XPathExpressionExceptionTest
 */

import java.util.Locale;

public class XPathExpressionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        check(javax.xml.xpath.XPathExpressionException.class.getSuperclass().getName().equals("javax.xml.xpath.XPathException"), "extends javax.xml.xpath.XPathException");
        testConstructors();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

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
