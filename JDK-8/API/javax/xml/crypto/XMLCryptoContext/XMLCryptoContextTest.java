/*
 * @test
 * @summary javax.xml.crypto.XMLCryptoContext conformance.
 * @run main XMLCryptoContextTest
 */

public class XMLCryptoContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.XMLCryptoContext");
        check(c.isInterface(), "is interface");
        check(c.getInterfaces().length == 0, "extends nothing");
        String[] expected = { "getBaseURI", "setBaseURI", "getKeySelector", "setKeySelector",
            "getURIDereferencer", "setURIDereferencer", "getNamespacePrefix", "putNamespacePrefix",
            "getDefaultNamespacePrefix", "setDefaultNamespacePrefix", "setProperty", "getProperty",
            "get", "put" };
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String e : expected) check(names.contains(e), "declares " + e);
        // both DOM context types implement the full surface
        javax.xml.crypto.dom.DOMCryptoContext dom = new javax.xml.crypto.dom.DOMCryptoContext() { };
        javax.xml.crypto.XMLCryptoContext ctx = dom;
        // baseURI
        check(ctx.getBaseURI() == null, "baseURI default null");
        ctx.setBaseURI("http://ctx/");
        check("http://ctx/".equals(ctx.getBaseURI()), "baseURI round-trip");
        // key selector slot
        check(ctx.getKeySelector() == null, "key selector default null");
        javax.xml.crypto.KeySelector ks = javax.xml.crypto.KeySelector.singletonKeySelector(
            java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic());
        ctx.setKeySelector(ks);
        check(ctx.getKeySelector() == ks, "key selector round-trip");
        // dereferencer slot
        check(ctx.getURIDereferencer() == null, "dereferencer default null");
        javax.xml.crypto.URIDereferencer ud = new javax.xml.crypto.URIDereferencer() {
            public javax.xml.crypto.Data dereference(javax.xml.crypto.URIReference r,
                    javax.xml.crypto.XMLCryptoContext x) { return null; }
        };
        ctx.setURIDereferencer(ud);
        check(ctx.getURIDereferencer() == ud, "dereferencer round-trip");
        // prefix management
        check(ctx.getDefaultNamespacePrefix() == null, "default prefix null initially");
        ctx.setDefaultNamespacePrefix("p");
        check("p".equals(ctx.getDefaultNamespacePrefix()), "default prefix round-trip");
        check(ctx.putNamespacePrefix("urn:n", "a") == null, "first registration returns null");
        check("a".equals(ctx.putNamespacePrefix("urn:n", "b")), "re-registration returns previous");
        check("b".equals(ctx.getNamespacePrefix("urn:n", "z")), "registered prefix wins");
        check("z".equals(ctx.getNamespacePrefix("urn:miss", "z")), "unregistered falls back to default arg");
        // properties
        check(ctx.setProperty("k", "v1") == null, "first setProperty returns null");
        check("v1".equals(ctx.setProperty("k", "v2")), "setProperty returns previous");
        check("v2".equals(ctx.getProperty("k")), "getProperty round-trip");
        check(ctx.getProperty("none") == null, "missing property null");
        // general map
        ctx.put("m", "n");
        check("n".equals(ctx.get("m")), "put/get round-trip");
        check(ctx.get("none") == null, "missing mapping null");
        // the dsig context subtypes remain assignable
        check(new javax.xml.crypto.dsig.dom.DOMSignContext(
            java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPrivate(),
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("r"))
            instanceof javax.xml.crypto.XMLCryptoContext, "DOMSignContext is an XMLCryptoContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
