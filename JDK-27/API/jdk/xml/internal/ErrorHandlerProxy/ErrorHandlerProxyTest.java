/*
 * @test
 * @summary jdk.xml.internal.ErrorHandlerProxy conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main ErrorHandlerProxyTest
 */
public class ErrorHandlerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.ErrorHandlerProxy", false, ErrorHandlerProxyTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.ErrorHandlerProxy"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("org.xml.sax.ErrorHandler"), "if-org.xml.sax.ErrorHandler");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 3, "method-count");
        check(md.contains("error"), "method-error");
        check(md.contains("fatalError"), "method-fatalError");
        check(md.contains("warning"), "method-warning");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.ErrorHandlerProxy", false, ErrorHandlerProxyTest.class.getClassLoader());
        Object proxy = c.getDeclaredConstructor().newInstance();
        check(proxy instanceof org.xml.sax.ErrorHandler, "instance-is-ErrorHandler");
        org.xml.sax.ErrorHandler eh = (org.xml.sax.ErrorHandler) proxy;
        // contract: warning/error/fatalError rethrow the SAME exception instance
        org.xml.sax.SAXParseException w = new org.xml.sax.SAXParseException("warn", "pub", "sys", 1, 2);
        try { eh.warning(w); check(false, "warning-rethrow"); }
        catch (org.xml.sax.SAXParseException e) { check(e == w, "warning-identity"); }
        org.xml.sax.SAXParseException er = new org.xml.sax.SAXParseException("err", "pub", "sys", 3, 4);
        try { eh.error(er); check(false, "error-rethrow"); }
        catch (org.xml.sax.SAXParseException e) { check(e == er, "error-identity"); }
        org.xml.sax.SAXParseException fe = new org.xml.sax.SAXParseException("fatal", "pub", "sys", 5, 6);
        try { eh.fatalError(fe); check(false, "fatal-rethrow"); }
        catch (org.xml.sax.SAXParseException e) { check(e == fe, "fatal-identity"); }
        // reflective contract identical to interface contract
        try { c.getMethod("warning", org.xml.sax.SAXParseException.class).invoke(proxy, w); check(false, "reflect-warning-rethrow"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() == w, "reflect-warning-identity"); }
        // real XML linkage 1: fatal error path, malformed document, proxy installed on DocumentBuilder
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(eh);
        try {
            db.parse(new java.io.ByteArrayInputStream("<root>".getBytes("UTF-8")));
            check(false, "fatal-parse-throws");
        } catch (org.xml.sax.SAXParseException e) {
            check(e.getMessage().contains("XML document structures must start and end within the same entity"),
                    "fatal-parse-message");
        }
        // real XML linkage 2: recoverable validity error is promoted to failure by error() rethrow
        javax.xml.parsers.DocumentBuilderFactory vdbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        vdbf.setValidating(true);
        javax.xml.parsers.DocumentBuilder vdb = vdbf.newDocumentBuilder();
        vdb.setErrorHandler(eh);
        String vxml = "<?xml version='1.0'?>\n<!DOCTYPE root [<!ELEMENT root EMPTY>]>\n<root>content</root>";
        try {
            vdb.parse(new java.io.ByteArrayInputStream(vxml.getBytes("UTF-8")));
            check(false, "validity-error-throws");
        } catch (org.xml.sax.SAXParseException e) {
            check(e.getMessage().contains("The content of element type \"root\" must match \"EMPTY\""),
                    "validity-error-message");
        }
        // same document with the default handler: error is recoverable, parse succeeds
        javax.xml.parsers.DocumentBuilder dflt = vdbf.newDocumentBuilder();
        org.w3c.dom.Document doc = dflt.parse(new java.io.ByteArrayInputStream(vxml.getBytes("UTF-8")));
        check(doc.getDocumentElement().getTextContent().equals("content"), "default-handler-recovers");
        // real XML linkage 3: well-formed document parses cleanly with the proxy installed
        javax.xml.parsers.DocumentBuilder ok = dbf.newDocumentBuilder();
        ok.setErrorHandler(eh);
        org.w3c.dom.Document okdoc = ok.parse(new java.io.ByteArrayInputStream(
                "<?xml version='1.0'?><r><c>text</c></r>".getBytes("UTF-8")));
        check(okdoc.getDocumentElement().getTagName().equals("r"), "wellformed-parse-ok");
        check(okdoc.getDocumentElement().getTextContent().equals("text"), "wellformed-text-ok");
        // a second proxy instance is distinct; installing it on a builder is accepted
        Object proxy2 = c.getDeclaredConstructor().newInstance();
        check(proxy2 != proxy && proxy2.equals(proxy2), "distinct-instances");
        javax.xml.parsers.DocumentBuilder db2 = dbf.newDocumentBuilder();
        db2.setErrorHandler((org.xml.sax.ErrorHandler) proxy2);
        check(true, "second-instance-installable");
        // reflective error/fatalError rethrow the same instance too
        try { c.getMethod("error", org.xml.sax.SAXParseException.class).invoke(proxy, er); check(false, "reflect-error-rethrow"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() == er, "reflect-error-identity"); }
        try { c.getMethod("fatalError", org.xml.sax.SAXParseException.class).invoke(proxy, fe); check(false, "reflect-fatal-rethrow"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() == fe, "reflect-fatal-identity"); }
        // locator state rides along on the identical instance
        try { eh.warning(w); check(false, "warning-rethrow-2"); }
        catch (org.xml.sax.SAXParseException e) {
            check(e.getPublicId().equals("pub") && e.getSystemId().equals("sys"), "warning-locator-preserved");
            check(e.getLineNumber() == 1 && e.getColumnNumber() == 2, "warning-position-preserved");
        }
        // clearing the handler restores default behavior: well-formed parse succeeds
        db2.setErrorHandler(null);
        db2.parse(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")));
        check(true, "null-handler-parse-ok");
    }
}
