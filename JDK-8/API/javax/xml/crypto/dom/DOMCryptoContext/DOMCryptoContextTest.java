/*
 * @test
 * @summary javax.xml.crypto.dom.DOMCryptoContext conformance.
 * @run main DOMCryptoContextTest
 */

import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;

public class DOMCryptoContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.crypto.dom.DOMCryptoContext");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class subclassable anonymously");
        check(javax.xml.crypto.XMLCryptoContext.class.isAssignableFrom(c), "implements XMLCryptoContext");
        check(c.getConstructors().length == 0, "only protected constructor");
        // subclass to reach the protected ctor
        DOMCryptoContext ctx = new DOMCryptoContext() { };
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element e1 = d.createElement("x");
        e1.setAttribute("Id", "i1");
        e1.setIdAttribute("Id", true);
        // id registration and resolution
        check(ctx.getElementById("i1") == null, "unregistered id null");
        ctx.setIdAttributeNS(e1, null, "Id");
        check(ctx.getElementById("i1") == e1, "registered id resolves");
        Element e2 = d.createElement("y");
        e2.setAttribute("Id", "i2");
        e2.setIdAttribute("Id", true);
        ctx.setIdAttributeNS(e2, null, "Id");
        check(ctx.getElementById("i2") == e2, "second id resolves");
        check(ctx.getElementById("i1") == e1, "first id still resolves");
        java.util.Iterator it = ctx.iterator();
        int n = 0;
        boolean sawE1 = false, sawE2 = false;
        while (it.hasNext()) {
            Object o = it.next();
            check(o instanceof java.util.Map.Entry, "iterator yields id-to-element entries");
            java.util.Map.Entry en = (java.util.Map.Entry) o;
            Element x = (Element) en.getValue();
            n++;
            if (x == e1) { sawE1 = true; check("i1".equals(en.getKey()), "entry key matches id i1"); }
            if (x == e2) sawE2 = true;
        }
        check(n == 2 && sawE1 && sawE2, "iterator walks both registered elements");
        try { it.remove(); check(false, "iterator remove should throw"); }
        catch (UnsupportedOperationException uoe) { check(true, "iterator remove UOE"); }
        // null element rejected with message
        try { ctx.setIdAttributeNS(null, null, "Id"); check(false, "null element should throw"); }
        catch (NullPointerException npe) { check("element is null".equals(npe.getMessage()), "null element message"); }
        // the general get/put map
        ctx.put("k1", "v1");
        check("v1".equals(ctx.get("k1")), "put/get round-trip");
        check(ctx.get("k2") == null, "missing key null");
        ctx.put("k1", "v2");
        check("v2".equals(ctx.get("k1")), "put overwrites");
        // namespace prefixes, base URI, properties
        check(ctx.getNamespacePrefix("urn:a", "d") == "d", "unregistered prefix yields default");
        ctx.putNamespacePrefix("urn:a", "p");
        check("p".equals(ctx.getNamespacePrefix("urn:a", "d")), "prefix round-trip");
        check(ctx.getBaseURI() == null, "baseURI default null");
        ctx.setBaseURI("http://b/");
        check("http://b/".equals(ctx.getBaseURI()), "baseURI round-trip");
        check(ctx.setProperty("p", "v") == null, "setProperty returns previous");
        check("v".equals(ctx.getProperty("p")), "property round-trip");
        // key selector slot
        check(ctx.getKeySelector() == null, "key selector default null");
        javax.xml.crypto.KeySelector ks = javax.xml.crypto.KeySelector.singletonKeySelector(
            java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic());
        ctx.setKeySelector(ks);
        check(ctx.getKeySelector() == ks, "key selector round-trip");
        // context is accepted where an XMLCryptoContext is required
        check(ctx instanceof XMLCryptoContext, "context assignable to XMLCryptoContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
