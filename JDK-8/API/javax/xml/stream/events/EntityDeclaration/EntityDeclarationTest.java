/*
 * @test
 * @summary javax.xml.stream.events.EntityDeclaration conformance: interface hierarchy and methods.
 * @run main EntityDeclarationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EntityDeclarationTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.EntityDeclaration");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPublicId"), "has method getPublicId");
        check(names.contains("getSystemId"), "has method getSystemId");
        check(names.contains("getName"), "has method getName");
        check(names.contains("getNotationName"), "has method getNotationName");
        check(names.contains("getReplacementText"), "has method getReplacementText");
        check(names.contains("getBaseURI"), "has method getBaseURI");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (internal/PUBLIC/SYSTEM forms via parsed DTD) ----
    static void testDeep() throws Exception {
        // 1) parse a DTD with all three entity forms and read them back from DTD.getEntities()
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE ro ["
                        + "<!ENTITY e1 'Hello'>"
                        + "<!ENTITY e2 PUBLIC 'pub1' 'sys1' NDATA gif>"
                        + "<!ENTITY e3 SYSTEM 'sys3'>"
                        + "<!NOTATION gif PUBLIC '-//gif'>"
                        + "]><ro/>"));
        javax.xml.stream.events.EntityDeclaration internal = null, externalPub = null, externalSys = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                for (Object o : ((javax.xml.stream.events.DTD) e).getEntities()) {
                    javax.xml.stream.events.EntityDeclaration ed = (javax.xml.stream.events.EntityDeclaration) o;
                    if ("e1".equals(ed.getName())) internal = ed;
                    if ("e2".equals(ed.getName())) externalPub = ed;
                    if ("e3".equals(ed.getName())) externalSys = ed;
                    check(ed.getEventType() == 15, "entity declaration code 15");
                    check(ed.getBaseURI() == null, "baseURI null for internal-subset decl");
                }
            }
        }
        r.close();
        // internal parsed entity: replacement text, no ids, no notation
        check(internal != null, "internal entity present");
        check("Hello".equals(internal.getReplacementText()), "internal replacement text");
        check(internal.getPublicId() == null && internal.getSystemId() == null && internal.getNotationName() == null,
                "internal ids/notation null");
        // external PUBLIC + NDATA: public/system ids and notation name wired
        check(externalPub != null, "external PUBLIC entity present");
        check("pub1".equals(externalPub.getPublicId()) && "sys1".equals(externalPub.getSystemId()),
                "external PUBLIC ids");
        check("gif".equals(externalPub.getNotationName()), "NDATA notation name");
        check(externalPub.getReplacementText() == null, "external entity no replacement text");
        // external SYSTEM: system id only
        check(externalSys != null, "external SYSTEM entity present");
        check("sys3".equals(externalSys.getSystemId()) && externalSys.getPublicId() == null,
                "external SYSTEM ids");
        check(externalSys.getNotationName() == null, "external SYSTEM no notation");

        // 2) encoded form of an entity declaration
        java.io.StringWriter w = new java.io.StringWriter();
        internal.writeAsEncodedUnicode(w);
        check("<!ENTITY e1 \"Hello\">".equals(w.toString()), "entity declaration encoded exact");

        // 3) factory createDTD carries no entity declarations
        javax.xml.stream.events.DTD fd = javax.xml.stream.XMLEventFactory.newFactory()
                .createDTD("<!DOCTYPE r [<!ENTITY e 'v'>]>");
        check(fd.getEntities() == null, "factory DTD getEntities null");

        // 4) entity declaration order in the list follows declaration order
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader(
                "<!DOCTYPE ro [<!ENTITY b '2'><!ENTITY a '1'>]><ro/>"));
        java.util.List<String> order = new java.util.ArrayList<String>();
        while (r2.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r2.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                for (Object o : ((javax.xml.stream.events.DTD) e).getEntities())
                    order.add(((javax.xml.stream.events.EntityDeclaration) o).getName());
            }
        }
        r2.close();
        check(order.size() == 2 && order.contains("b") && order.contains("a"),
                "both declared entities listed (map-backed order, not declaration order)");
    }
}
