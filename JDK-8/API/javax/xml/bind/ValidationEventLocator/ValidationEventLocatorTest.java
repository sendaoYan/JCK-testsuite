/*
 * @test
 * @summary javax.xml.bind.ValidationEventLocator conformance: getter signatures, default state and real locator channel.
 * @run main ValidationEventLocatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ValidationEventLocatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.xml.bind.annotation.XmlRootElement
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Person {
        @javax.xml.bind.annotation.XmlElement String name;
        @javax.xml.bind.annotation.XmlElement int age;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.ValidationEventLocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getURL"), "has method getURL");
        check(names.contains("getOffset"), "has method getOffset");
        check(names.contains("getLineNumber"), "has method getLineNumber");
        check(names.contains("getColumnNumber"), "has method getColumnNumber");
        check(names.contains("getObject"), "has method getObject");
        check(names.contains("getNode"), "has method getNode");
        testSignatures(c);
        testDefaultsAndRoundTrip();
        testRealLocator();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testSignatures(Class<?> c) throws Exception {
        Method[] dm = c.getDeclaredMethods();
        check(dm.length == 6, "exactly 6 declared methods");
        java.util.Map<String, Class<?>> rets = new java.util.HashMap<String, Class<?>>();
        for (Method m : dm) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    m.getName() + " is public abstract");
            rets.put(m.getName(), m.getReturnType());
        }
        check(rets.get("getURL") == java.net.URL.class, "getURL() returns URL");
        check(rets.get("getOffset") == int.class, "getOffset() returns int");
        check(rets.get("getLineNumber") == int.class, "getLineNumber() returns int");
        check(rets.get("getColumnNumber") == int.class, "getColumnNumber() returns int");
        check(rets.get("getObject") == Object.class, "getObject() returns Object");
        check(rets.get("getNode") == org.w3c.dom.Node.class, "getNode() returns org.w3c.dom.Node");
        check(c.getInterfaces().length == 0, "extends no other interface");
    }

    // concrete impl read back through the INTERFACE type: full matrix
    static void testDefaultsAndRoundTrip() throws Exception {
        javax.xml.bind.ValidationEventLocator l = new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        check(l.getURL() == null, "default url null");
        check(l.getOffset() == -1, "default offset == -1 (locked, not 0)");
        check(l.getLineNumber() == -1, "default line == -1 (locked, not 0)");
        check(l.getColumnNumber() == -1, "default column == -1 (locked, not 0)");
        check(l.getObject() == null, "default object null");
        check(l.getNode() == null, "default node null");

        javax.xml.bind.helpers.ValidationEventLocatorImpl impl =
                (javax.xml.bind.helpers.ValidationEventLocatorImpl) l;
        java.net.URL url = new java.net.URL("http://example.com/loc.xml");
        impl.setURL(url);
        check(l.getURL() == url, "setURL/getURL round trip via interface");
        impl.setOffset(11);
        check(l.getOffset() == 11, "setOffset/getOffset round trip");
        impl.setLineNumber(21);
        check(l.getLineNumber() == 21, "setLineNumber/getLineNumber round trip");
        impl.setColumnNumber(31);
        check(l.getColumnNumber() == 31, "setColumnNumber/getColumnNumber round trip");
        Object o = new Object();
        impl.setObject(o);
        check(l.getObject() == o, "setObject/getObject round trip");
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        org.w3c.dom.Node n = doc.createElement("n");
        impl.setNode(n);
        check(l.getNode() == n, "setNode/getNode round trip");
    }

    // real channel: locator delivered with a real unmarshal validation event
    static void testRealLocator() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
        java.util.List<javax.xml.bind.ValidationEventLocator> locs =
                new java.util.ArrayList<javax.xml.bind.ValidationEventLocator>();
        u.setEventHandler(new javax.xml.bind.ValidationEventHandler() {
            public boolean handleEvent(javax.xml.bind.ValidationEvent e) {
                locs.add(e.getLocator());
                return true;
            }
        });
        Object r = u.unmarshal(new java.io.StringReader(
                "<person><name>alice</name><age>30</age><extra>x</extra></person>"));
        check(r instanceof Person, "unmarshal completes under tolerant handler");
        check(locs.size() == 1, "one locator delivered");
        javax.xml.bind.ValidationEventLocator loc = locs.get(0);
        check(loc != null, "locator non-null");
        check(loc.getLineNumber() == 1, "real locator line == 1 (single-line xml)");
        check(loc.getColumnNumber() == 47, "real locator column == 47 (position of <extra>, locked)");
        check(loc.getOffset() == -1, "real locator offset == -1");
        check(loc.getURL() == null, "real locator url null (Reader source)");
        check(loc.getObject() == null, "real locator object null");
        check(loc.getNode() == null, "real locator node null");
    }
}
