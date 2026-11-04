/*
 * @test
 * @summary javax.swing.text.html.parser.Entity conformance: white-box structure + black-box behavior.
 * @run main EntityTest
 */

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Entity;
import javax.swing.text.html.parser.ParserDelegator;

public class EntityTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 0: legacy structure checks (preserved verbatim from the previous revision) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testLegacyStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.Entity", false, EntityTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.text.html.parser.DTDConstants"), "implements javax.swing.text.html.parser.DTDConstants");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("getType"), "method getType");
        check(names.contains("isParameter"), "method isParameter");
        check(names.contains("isGeneral"), "method isGeneral");
        check(names.contains("getData"), "method getData");
        check(names.contains("getString"), "method getString");
        check(names.contains("name2type"), "method name2type");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.Entity");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.html.parser.DTDConstants"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // static name2type converter
        check(Entity.name2type("PUBLIC") == DTDConstants.PUBLIC, "name2type PUBLIC");
        check(Entity.name2type("CDATA") == DTDConstants.CDATA, "name2type CDATA");
        check(Entity.name2type("SDATA") == DTDConstants.SDATA, "name2type SDATA");
        check(Entity.name2type("STARTTAG") == DTDConstants.STARTTAG, "name2type STARTTAG");
        check(Entity.name2type("ENDTAG") == DTDConstants.ENDTAG, "name2type ENDTAG");
        check(Entity.name2type("unknown") == DTDConstants.CDATA, "name2type unknown -> CDATA");

        // entities from the loaded html32 DTD
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");

        Entity amp = dtd.getEntity("amp");
        check(amp != null, "amp entity non-null");
        check("amp".equals(amp.getName()), "amp name");
        check(amp.getType() == DTDConstants.CDATA, "amp type CDATA");
        check(amp.isGeneral(), "amp isGeneral");
        check(!amp.isParameter(), "amp isParameter false");
        check("&".equals(amp.getString()), "amp string &");
        check(amp.getData().length == 1 && amp.getData()[0] == '&', "amp data char");

        check(">".equals(dtd.getEntity("gt").getString()), "gt string >");
        check("<".equals(dtd.getEntity("lt").getString()), "lt string <");
        check(dtd.getEntity("no-such-entity") == null, "unknown entity null");

        // constructor: general entity
        char[] xdata = new char[] { 'x' };
        Entity gen = new Entity("foo", DTDConstants.GENERAL | DTDConstants.CDATA, xdata);
        check("foo".equals(gen.getName()), "ctor name");
        check(gen.getType() == DTDConstants.CDATA, "ctor getType masks to CDATA");
        check(gen.isGeneral(), "ctor isGeneral");
        check(!gen.isParameter(), "ctor isParameter false");
        check("x".equals(gen.getString()), "ctor getString");
        check(gen.getData() == xdata, "ctor getData returns backing array");

        // constructor: parameter entity
        Entity par = new Entity("pct", DTDConstants.PARAMETER | DTDConstants.CDATA, new char[] { '%' });
        check(par.isParameter(), "parameter entity isParameter");
        check(!par.isGeneral(), "parameter entity isGeneral false");
        check(par.getType() == DTDConstants.CDATA, "parameter entity type CDATA");
        check("%".equals(par.getString()), "parameter entity string");
    }

    // ---- Part 3: deep contract coverage ----

    static void expectNPE(String label, Runnable r) {
        try { r.run(); throw new AssertionError(label + ": expected NullPointerException"); }
        catch (NullPointerException expected) { passed++; }
    }

    static void testDeepTypeMasking() {
        // getType() keeps only the low 16 bits: the GENERAL/PARAMETER/DEFAULT
        // qualifier bits are stripped, the entity-type value survives
        check(new Entity("a", DTDConstants.GENERAL | DTDConstants.CDATA, null).getType() == DTDConstants.CDATA,
              "GENERAL|CDATA -> CDATA");
        check(new Entity("b", DTDConstants.GENERAL | DTDConstants.SDATA, null).getType() == DTDConstants.SDATA,
              "GENERAL|SDATA -> SDATA");
        check(new Entity("c", DTDConstants.GENERAL | DTDConstants.PI, null).getType() == DTDConstants.PI,
              "GENERAL|PI -> PI");
        check(new Entity("d", DTDConstants.PARAMETER | DTDConstants.PI, null).getType() == DTDConstants.PI,
              "PARAMETER|PI -> PI");
        check(new Entity("e", DTDConstants.GENERAL | DTDConstants.NUMBER, null).getType() == DTDConstants.NUMBER,
              "GENERAL|NUMBER -> NUMBER");
        check(new Entity("f", DTDConstants.GENERAL | DTDConstants.PARAMETER, null).getType() == 0,
              "GENERAL|PARAMETER -> 0 (no type bits)");
        check(new Entity("g", 0, null).getType() == 0, "type 0 -> 0");
        // qualifier flags survive in isGeneral/isParameter independently of getType()
        Entity both = new Entity("h", DTDConstants.GENERAL | DTDConstants.PARAMETER | DTDConstants.SDATA, null);
        check(both.isGeneral() && both.isParameter(), "both qualifier flags settable");
        check(both.getType() == DTDConstants.SDATA, "both flags still mask to SDATA");
        Entity neither = new Entity("i", DTDConstants.CDATA, null);
        check(!neither.isGeneral() && !neither.isParameter(), "no qualifier flags by default");
        // public fields mirror the getters
        Entity amp = new Entity("amp", DTDConstants.GENERAL | DTDConstants.CDATA, new char[]{'&'});
        check("amp".equals(amp.name) && amp.name.equals(amp.getName()), "name field == getName");
        check(amp.type == (DTDConstants.GENERAL | DTDConstants.CDATA), "type field keeps full word");
        check(amp.getData() == amp.data, "getData == data field");
    }

    static void testDeepDataSemantics() {
        // null data accepted by the constructor; getData() null, getString() NPE
        Entity nullData = new Entity("nd", DTDConstants.GENERAL | DTDConstants.CDATA, null);
        check(nullData.getData() == null, "null data getData null");
        expectNPE("null data getString", new Runnable() { public void run() { nullData.getString(); } });
        // null name accepted
        Entity nullName = new Entity(null, DTDConstants.CDATA, new char[]{'x'});
        check(nullName.getName() == null, "null name getName null");
        check("x".equals(nullName.getString()), "null name data still readable");
        // the data array is live, not copied
        char[] shared = new char[]{'a'};
        Entity live = new Entity("lv", DTDConstants.GENERAL | DTDConstants.CDATA, shared);
        shared[0] = 'z';
        check("z".equals(live.getString()), "data array not defensively copied");
        check(live.getData().length == 1, "single char data");
        // Entity is NOT Serializable
        check(!java.io.Serializable.class.isAssignableFrom(Entity.class), "Entity not Serializable");
    }

    static void testDeepName2TypeMatrix() {
        check(Entity.name2type("PUBLIC") == DTDConstants.PUBLIC, "name2type PUBLIC");
        check(Entity.name2type("CDATA") == DTDConstants.CDATA, "name2type CDATA");
        check(Entity.name2type("SDATA") == DTDConstants.SDATA, "name2type SDATA");
        check(Entity.name2type("PI") == DTDConstants.PI, "name2type PI");
        check(Entity.name2type("STARTTAG") == DTDConstants.STARTTAG, "name2type STARTTAG");
        check(Entity.name2type("ENDTAG") == DTDConstants.ENDTAG, "name2type ENDTAG");
        check(Entity.name2type("MS") == DTDConstants.MS, "name2type MS");
        check(Entity.name2type("MD") == DTDConstants.MD, "name2type MD");
        check(Entity.name2type("SYSTEM") == DTDConstants.SYSTEM, "name2type SYSTEM");
        // case sensitive lookup; anything unrecognized defaults to CDATA
        check(Entity.name2type("public") == DTDConstants.CDATA, "name2type lowercase -> CDATA");
        check(Entity.name2type("GENERAL") == DTDConstants.CDATA, "name2type GENERAL -> CDATA (not an entity type)");
        check(Entity.name2type("") == DTDConstants.CDATA, "name2type(\"\") -> CDATA");
        check(Entity.name2type("zzz") == DTDConstants.CDATA, "name2type unknown -> CDATA");
        expectNPE("name2type(null)", new Runnable() { public void run() { Entity.name2type(null); } });
    }

    static void testDeepHtml32Entities() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        // named lookups decode to their replacement text
        check("quot".equals(dtd.getEntity((int) '"').getName()), "char \" -> quot");
        check("\"".equals(dtd.getEntity("quot").getString()), "quot string");
        check(dtd.getEntity("nbsp").getString().length() == 1
              && dtd.getEntity("nbsp").getString().charAt(0) == '\u00A0', "nbsp is U+00A0");
        check(dtd.getEntity("nbsp") == dtd.getEntity(0x00A0), "nbsp reachable by char key");
        check(dtd.getEntity("zzz-unknown") == null, "unknown named entity null");
    }

    public static void main(String[] args) throws Exception {
        testLegacyStructure();
        testWhiteBox();
        testBlackBox();
        testDeepTypeMasking();
        testDeepDataSemantics();
        testDeepName2TypeMatrix();
        testDeepHtml32Entities();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
