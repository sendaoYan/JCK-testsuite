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
    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
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

        // ---- JDK-19+ factory methods ----
        check(XPathFactory.newDefaultInstance() != null, "newDefaultInstance non-null");
        XPathFactory fp = XPathFactory.newDefaultInstance();
        check(isIae(() -> fp.setProperty("javax.xml.xpath.XPathFactory:unknown", "value")), "setProperty(unknown) -> IAE");
        check(isIae(() -> fp.getProperty("javax.xml.xpath.XPathFactory:unknown")), "getProperty(unknown) -> IAE");

        testConstantsAndFeatures();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static void testConstantsAndFeatures() throws Exception {
        // constants (locked actual values)
        check("javax.xml.xpath.XPathFactory".equals(XPathFactory.DEFAULT_PROPERTY_NAME),
              "DEFAULT_PROPERTY_NAME exact value");
        check("http://java.sun.com/jaxp/xpath/dom".equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI),
              "DEFAULT_OBJECT_MODEL_URI exact value");

        // FEATURE_SECURE_PROCESSING round-trip
        XPathFactory f = XPathFactory.newInstance();
        String FSP = javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
        f.setFeature(FSP, true);
        check(f.getFeature(FSP), "setFeature(secure,true)/getFeature round-trip");
        f.setFeature(FSP, false);
        check(!f.getFeature(FSP), "setFeature(secure,false)/getFeature round-trip");
        check(isFce(() -> f.setFeature("http://bogus/f", true)),
              "setFeature(unknown) -> XPathFactoryConfigurationException");
        check(isFce(() -> f.getFeature("http://bogus/f")),
              "getFeature(unknown) -> XPathFactoryConfigurationException");
        check(isNpe(() -> f.getFeature(null)), "getFeature(null) -> NPE");

        // JDK-19+ setProperty/getProperty round-trip on a supported jdk.xml.* property
        XPathFactory fp2 = XPathFactory.newDefaultInstance();
        fp2.setProperty("jdk.xml.xpathExprGrpLimit", "50");
        check("50".equals(fp2.getProperty("jdk.xml.xpathExprGrpLimit")),
              "setProperty/getProperty round-trip (jdk.xml.xpathExprGrpLimit)");
        check(isNpe(() -> fp2.setProperty(null, "v")), "setProperty(null name) -> NPE");

        // explicit default object model == implicit newInstance()
        XPathFactory fA = XPathFactory.newInstance(XPathConstants.DOM_OBJECT_MODEL);
        XPathFactory fB = XPathFactory.newInstance();
        check(fA != null && fB != null
              && fA.isObjectModelSupported(XPathConstants.DOM_OBJECT_MODEL)
              && fB.isObjectModelSupported(XPathConstants.DOM_OBJECT_MODEL),
              "newInstance(DOM) and newInstance() both support DOM object model");

        // resolvers installed on the factory are inherited by every newXPath()
        javax.xml.xpath.XPathVariableResolver vr = v -> Integer.valueOf(42);
        f.setXPathVariableResolver(vr);
        XPath xpv = f.newXPath();
        check(xpv.getXPathVariableResolver() == vr, "factory variable resolver inherited");
        javax.xml.xpath.XPathFunctionResolver fr = (qn, arity) -> null;
        f.setXPathFunctionResolver(fr);
        XPath xpf = f.newXPath();
        check(xpf.getXPathFunctionResolver() == fr, "factory function resolver inherited");

        // resolver-carrying factory produces a working closed loop
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(new java.io.ByteArrayInputStream("<r/>".getBytes("UTF-8")));
        Double res = (Double) xpv.evaluate("$x+1", d, XPathConstants.NUMBER);
        check(res != null && res.doubleValue() == 43.0, "factory resolver closed loop: $x+1 == 43.0");

        // newInstance(uri, className, loader)
        XPathFactory fc = XPathFactory.newInstance(
            XPathFactory.DEFAULT_OBJECT_MODEL_URI,
            "com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl",
            Thread.currentThread().getContextClassLoader());
        check(fc != null && fc.isObjectModelSupported(XPathConstants.DOM_OBJECT_MODEL),
              "newInstance(uri, className, loader) works");

        // JDK-27 surface: newDefaultInstance/setProperty/getProperty present
        check(hasMethod(XPathFactory.class, "newDefaultInstance"), "JDK-27: newDefaultInstance present");
        check(hasMethod(XPathFactory.class, "setProperty"), "JDK-27: setProperty present");
        check(hasMethod(XPathFactory.class, "getProperty"), "JDK-27: getProperty present");
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
}
