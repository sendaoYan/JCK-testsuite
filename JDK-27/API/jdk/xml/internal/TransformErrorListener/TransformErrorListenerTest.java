/*
 * @test
 * @summary jdk.xml.internal.TransformErrorListener conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main TransformErrorListenerTest
 */
public class TransformErrorListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.TransformErrorListener", false, TransformErrorListenerTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.TransformErrorListener"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("javax.xml.transform.ErrorListener"), "if-javax.xml.transform.ErrorListener");
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
        Class<?> c = Class.forName("jdk.xml.internal.TransformErrorListener", false, TransformErrorListenerTest.class.getClassLoader());
        Object lsnr = c.getDeclaredConstructor().newInstance();
        check(lsnr instanceof javax.xml.transform.ErrorListener, "instance-is-ErrorListener");
        javax.xml.transform.ErrorListener el = (javax.xml.transform.ErrorListener) lsnr;
        // contract: warning is a no-op, error and fatalError rethrow the same instance
        el.warning(new javax.xml.transform.TransformerException("w"));
        check(true, "warning-noop");
        javax.xml.transform.TransformerException e1 = new javax.xml.transform.TransformerException("e");
        try { el.error(e1); check(false, "error-rethrows"); }
        catch (javax.xml.transform.TransformerException e) { check(e == e1, "error-identity"); }
        javax.xml.transform.TransformerException e2 = new javax.xml.transform.TransformerException("f");
        try { el.fatalError(e2); check(false, "fatal-rethrows"); }
        catch (javax.xml.transform.TransformerException e) { check(e == e2, "fatal-identity"); }
        // message/locator are preserved through the rethrow
        javax.xml.transform.TransformerException located =
                new javax.xml.transform.TransformerException("loc", new javax.xml.transform.SourceLocator() {
                    public String getPublicId() { return "pub"; }
                    public String getSystemId() { return "sys"; }
                    public int getLineNumber() { return 7; }
                    public int getColumnNumber() { return 9; }
                });
        try { el.error(located); check(false, "located-rethrows"); }
        catch (javax.xml.transform.TransformerException e) {
            check(e.getMessage().equals("loc"), "located-message");
            check(e.getLocator().getLineNumber() == 7 && e.getLocator().getColumnNumber() == 9, "located-position");
        }
        // real XSLT linkage 1: stylesheet compile error with the listener installed on the factory
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        tf.setErrorListener(el);
        boolean threw = false;
        try {
            tf.newTemplates(new javax.xml.transform.stream.StreamSource(
                    new java.io.StringReader("<not-a-stylesheet/>")));
        } catch (Throwable t) { threw = true; }
        check(threw, "compile-error-propagates");
        // real XSLT linkage 2: a valid stylesheet still compiles and transforms with the listener
        javax.xml.transform.Templates okT = tf.newTemplates(new javax.xml.transform.stream.StreamSource(
                new java.io.StringReader("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                        + "<xsl:output method=\"text\"/>"
                        + "<xsl:template match=\"/\"><out><xsl:value-of select=\"count(//*)\"/></out></xsl:template></xsl:stylesheet>")));
        java.io.StringWriter sw = new java.io.StringWriter();
        okT.newTransformer().transform(
                new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<r><a/><b/></r>")),
                new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().equals("3"), "clean-transform-count");
        // real XSLT linkage 3: transform-time error surfaces through the listener as TransformerException
        // (system-property with a non-XSLT name is rejected at runtime on this JDK)
        javax.xml.transform.Transformer t3 = javax.xml.transform.TransformerFactory.newInstance().newTransformer(
                new javax.xml.transform.stream.StreamSource(new java.io.StringReader(
                        "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                                + "<xsl:template match=\"/\"><xsl:value-of select=\"system-property('dxml27.nonxslt')\"/></xsl:template></xsl:stylesheet>")));
        t3.setErrorListener(el);
        try {
            t3.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<r/>")),
                    new javax.xml.transform.stream.StreamResult(new java.io.StringWriter()));
            check(false, "runtime-error-propagates");
        } catch (javax.xml.transform.TransformerException te) {
            check(true, "runtime-error-propagates");
            check(te.getCause() instanceof RuntimeException, "runtime-error-cause-RTE");
        }
        check(tf.getErrorListener() == el, "listener-installed-roundtrip");
        // reflective calls behave identically: warning swallows, error/fatalError rethrow
        java.lang.reflect.Method mw = c.getMethod("warning", javax.xml.transform.TransformerException.class);
        mw.invoke(lsnr, new javax.xml.transform.TransformerException("rw"));
        check(true, "reflect-warning-noop");
        try { c.getMethod("error", javax.xml.transform.TransformerException.class).invoke(lsnr, e1); check(false, "reflect-error-rethrow"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() == e1, "reflect-error-identity"); }
        try { c.getMethod("fatalError", javax.xml.transform.TransformerException.class).invoke(lsnr, e2); check(false, "reflect-fatal-rethrow"); }
        catch (java.lang.reflect.InvocationTargetException ite) { check(ite.getCause() == e2, "reflect-fatal-identity"); }
        // a nested cause survives the rethrow untouched
        javax.xml.transform.TransformerException nested =
                new javax.xml.transform.TransformerException("outer", new IllegalStateException("inner"));
        try { el.fatalError(nested); check(false, "nested-rethrow"); }
        catch (javax.xml.transform.TransformerException e) {
            check(e.getCause() instanceof IllegalStateException && e.getCause().getMessage().equals("inner"), "nested-cause-preserved");
        }
        // a second listener instance is distinct and the compile error also fires through newTransformer
        check(c.getDeclaredConstructor().newInstance() != lsnr, "distinct-instances");
        boolean threwNT = false;
        try {
            tf.newTransformer(new javax.xml.transform.stream.StreamSource(
                    new java.io.StringReader("<also-not-a-stylesheet/>")));
        } catch (Throwable t) { threwNT = true; }
        check(threwNT, "newTransformer-compile-error-propagates");
        // transformer built from templates keeps its own listener slot
        javax.xml.transform.Transformer tFromTpl = okT.newTransformer();
        tFromTpl.setErrorListener(el);
        check(tFromTpl.getErrorListener() == el, "templates-transformer-listener-roundtrip");
    }
}
