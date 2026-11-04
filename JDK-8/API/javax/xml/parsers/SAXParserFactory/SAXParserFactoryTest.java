/*
 * @test
 * @summary javax.xml.parsers.SAXParserFactory conformance: factory boundary, precise exceptions, null semantics, feature round-trip.
 * @run main SAXParserFactoryTest
 */

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SAXParserFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isFce(Thrower t) {
        try { t.run(); return false; }
        catch (FactoryConfigurationError e) { return true; }
        catch (Exception e) { return false; }
    }
    // any declared/undocumented exception from setFeature/getFeature
    static boolean throwsException(Thrower t) {
        try { t.run(); return false; }
        catch (Exception e) { return true; }
    }

    public static void main(String[] args) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        check(f != null, "newInstance non-null");

        ClassLoader cl = SAXParserFactory.class.getClassLoader();
        SAXParserFactory f2 = SAXParserFactory.newInstance(
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", cl);
        check(f2 != null, "newInstance(className, loader) non-null");

        check(isFce(() -> SAXParserFactory.newInstance("no.such.Factory", cl)),
              "bogus impl class -> FactoryConfigurationError");
        check(isFce(() -> SAXParserFactory.newInstance(null, null)),
              "null factory class -> FactoryConfigurationError");

        // documented defaults
        check(!f.isNamespaceAware(), "default namespaceAware=false");
        check(!f.isValidating(), "default validating=false");
        check(!f.isXIncludeAware(), "default xIncludeAware=false");
        check(f.getSchema() == null, "default schema=null");

        // round-trip
        f.setNamespaceAware(true);  check(f.isNamespaceAware(), "namespaceAware=true");
        f.setNamespaceAware(false); check(!f.isNamespaceAware(), "namespaceAware=false");
        f.setValidating(true);      check(f.isValidating(), "validating=true");
        f.setValidating(false);     check(!f.isValidating(), "validating=false");
        f.setXIncludeAware(true);   check(f.isXIncludeAware(), "xIncludeAware=true");
        f.setXIncludeAware(false);  check(!f.isXIncludeAware(), "xIncludeAware=false");
        f.setSchema(null);          check(f.getSchema() == null, "setSchema(null)");

        // unknown feature -> exception
        check(throwsException(() -> f.setFeature("http://no.such/feature", true)), "setFeature(unknown) throws");
        check(throwsException(() -> f.getFeature("http://no.such/feature")), "getFeature(unknown) throws");

        // parser production
        SAXParser p = f.newSAXParser();
        check(p != null, "newSAXParser non-null");
        check(p.isNamespaceAware() == f.isNamespaceAware(), "parser mirrors factory namespaceAware");
        check(p.isValidating() == f.isValidating(), "parser mirrors factory validating");
        SAXParser p2 = f.newSAXParser();
        check(p2 != null && p2 != p, "newSAXParser returns fresh parser");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
