/*
 * @test
 * @summary javax.xml.stream.events.NotationDeclaration conformance: interface hierarchy and methods.
 * @run main NotationDeclarationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class NotationDeclarationTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.NotationDeclaration");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "has method getName");
        check(names.contains("getPublicId"), "has method getPublicId");
        check(names.contains("getSystemId"), "has method getSystemId");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (PUBLIC/SYSTEM forms via parsed DTD) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();

        // 1) parse internal subset with PUBLIC and SYSTEM notations
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE r ["
                        + "<!NOTATION gif PUBLIC '-//gif//gif'>"
                        + "<!NOTATION bin SYSTEM 'binfmt'>"
                        + "]><r/>"));
        javax.xml.stream.events.NotationDeclaration pub = null, sys = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                for (Object o : ((javax.xml.stream.events.DTD) e).getNotations()) {
                    javax.xml.stream.events.NotationDeclaration nd = (javax.xml.stream.events.NotationDeclaration) o;
                    if ("gif".equals(nd.getName())) pub = nd;
                    if ("bin".equals(nd.getName())) sys = nd;
                    check(nd.getEventType() == 14, "notation declaration code 14");
                }
            }
        }
        r.close();
        check(pub != null, "PUBLIC notation present");
        check("-//gif//gif".equals(pub.getPublicId()) && pub.getSystemId() == null, "PUBLIC notation ids");
        check(sys != null, "SYSTEM notation present");
        check("binfmt".equals(sys.getSystemId()) && sys.getPublicId() == null, "SYSTEM notation ids");

        // 2) encoded form exact
        java.io.StringWriter w = new java.io.StringWriter();
        pub.writeAsEncodedUnicode(w);
        check("<!NOTATION gif PUBLIC \"-//gif//gif\">".equals(w.toString()), "notation encoded exact");

        // 3) notation declarations are only reachable via the DTD event, never standalone
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE r [<!NOTATION n SYSTEM 's'>]><r/>"));
        boolean standalone = false;
        while (r2.hasNext()) { if (r2.nextEvent().getEventType() == 14) standalone = true; }
        r2.close();
        check(!standalone, "no standalone NOTATION_DECLARATION event");

        // 4) doc without notations: empty/null list
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE r [<!ENTITY e 'v'>]><r/>"));
        javax.xml.stream.events.DTD d = null;
        while (r3.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r3.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) d = (javax.xml.stream.events.DTD) e;
        }
        r3.close();
        check(d != null && d.getNotations() == null && d.getEntities() != null && d.getEntities().size() == 1,
                "entities-without-notations matrix");
    }
}
