/*
 * @test
 * @summary javax.xml.xpath.XPathFactory conformance: factory boundary, precise exceptions, object-model support, XPath production.
 * @run main XPathFactoryTest
 */

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class XPathFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isFce(Thrower t) {
        try { t.run(); return false; }
        catch (XPathFactoryConfigurationException e) { return true; }
        catch (Exception e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        // ---- factory boundary ----
        XPathFactory f = XPathFactory.newInstance();
        check(f != null, "newInstance non-null");
        check(XPathConstants.DOM_OBJECT_MODEL.equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI),
              "DEFAULT_OBJECT_MODEL_URI == DOM_OBJECT_MODEL");
        XPathFactory f2 = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
        check(f2 != null, "newInstance(default uri) non-null");

        // ---- precise exceptions ----
        check(isNpe(() -> XPathFactory.newInstance(null)), "newInstance(null uri) -> NPE");
        check(isFce(() -> XPathFactory.newInstance("http://bogus.object.model")), "newInstance(bogus uri) -> XPathFactoryConfigurationException");

        // ---- object model support ----
        check(f.isObjectModelSupported(XPathConstants.DOM_OBJECT_MODEL), "isObjectModelSupported(DOM) true");
        check(!f.isObjectModelSupported("http://bogus.object.model"), "isObjectModelSupported(bogus) false");

        // ---- XPath production ----
        XPath xp = f.newXPath();
        check(xp != null, "newXPath non-null");
        XPath xp2 = f.newXPath();
        check(xp2 != null && xp2 != xp, "newXPath returns fresh XPath");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
