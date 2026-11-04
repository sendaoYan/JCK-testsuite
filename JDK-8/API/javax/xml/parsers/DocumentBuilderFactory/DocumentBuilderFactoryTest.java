/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilderFactory conformance: factory boundary, precise exceptions, null semantics, configuration round-trip.
 * @run main DocumentBuilderFactoryTest
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentBuilderFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isFce(Thrower t) {
        try { t.run(); return false; }
        catch (FactoryConfigurationError e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isPce(Thrower t) {
        try { t.run(); return false; }
        catch (ParserConfigurationException e) { return true; }
        catch (Exception e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        // ---- factory boundary ----
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        check(f != null, "newInstance non-null");

        // explicit impl class loads via the bootstrap-aware overload
        ClassLoader cl = DocumentBuilderFactory.class.getClassLoader();
        DocumentBuilderFactory f2 = DocumentBuilderFactory.newInstance(
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", cl);
        check(f2 != null, "newInstance(className, loader) non-null");

        // ---- precise factory exceptions ----
        check(isFce(() -> DocumentBuilderFactory.newInstance("no.such.Factory", cl)),
              "bogus impl class -> FactoryConfigurationError");
        check(isFce(() -> DocumentBuilderFactory.newInstance("no.such.Factory", null)),
              "bogus impl class null loader -> FactoryConfigurationError");
        check(isFce(() -> DocumentBuilderFactory.newInstance(null, null)),
              "null factory class -> FactoryConfigurationError");

        // ---- documented defaults ----
        check(!f.isNamespaceAware(), "default namespaceAware=false");
        check(!f.isValidating(), "default validating=false");
        check(!f.isXIncludeAware(), "default xIncludeAware=false");
        check(f.getSchema() == null, "default schema=null");

        // ---- configuration round-trip ----
        f.setNamespaceAware(true);  check(f.isNamespaceAware(), "namespaceAware=true round-trip");
        f.setNamespaceAware(false); check(!f.isNamespaceAware(), "namespaceAware=false round-trip");
        f.setValidating(true);      check(f.isValidating(), "validating=true round-trip");
        f.setValidating(false);     check(!f.isValidating(), "validating=false round-trip");
        f.setXIncludeAware(true);   check(f.isXIncludeAware(), "xIncludeAware=true round-trip");
        f.setXIncludeAware(false);  check(!f.isXIncludeAware(), "xIncludeAware=false round-trip");
        f.setIgnoringElementContentWhitespace(true);
        check(f.isIgnoringElementContentWhitespace(), "ignoringWhitespace round-trip");
        f.setExpandEntityReferences(true);
        check(f.isExpandEntityReferences(), "expandEntityReferences round-trip");
        f.setIgnoringComments(true);
        check(f.isIgnoringComments(), "ignoringComments round-trip");
        f.setCoalescing(true);
        check(f.isCoalescing(), "coalescing round-trip");

        // setSchema(null) is a legal way to clear schema
        f.setSchema(null);
        check(f.getSchema() == null, "setSchema(null) clears schema");

        // ---- attribute null semantics / exceptions ----
        check(isIae(() -> f.setAttribute(null, "v")), "setAttribute(null,..) -> IAE");
        check(isNpe(() -> f.getAttribute(null)), "getAttribute(null) -> NPE");
        // unknown attribute name on the JDK impl -> IllegalArgumentException
        check(isIae(() -> f.getAttribute("http://no.such.attribute")), "getAttribute(unknown) -> IAE");
        check(isIae(() -> f.setAttribute("http://no.such.attribute", "v")), "setAttribute(unknown) -> IAE");

        // ---- feature null semantics / exceptions ----
        check(isPce(() -> f.setFeature("http://no.such/feature", true)), "setFeature(unknown) -> ParserConfigurationException");
        check(isPce(() -> f.getFeature("http://no.such/feature")), "getFeature(unknown) -> ParserConfigurationException");
        check(isNpe(() -> f.setFeature(null, true)), "setFeature(null,..) -> NPE");

        // ---- builder production ----
        DocumentBuilder b = f.newDocumentBuilder();
        check(b != null, "newDocumentBuilder non-null");
        DocumentBuilder b2 = f.newDocumentBuilder();
        check(b2 != null && b2 != b, "newDocumentBuilder returns fresh builder");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
