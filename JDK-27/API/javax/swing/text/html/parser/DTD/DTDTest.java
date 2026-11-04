/*
 * @test
 * @summary javax.swing.text.html.parser.DTD conformance: white-box structure + black-box behavior.
 * @run main DTDTest
 */

import java.lang.reflect.Modifier;

import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.Entity;
import javax.swing.text.html.parser.ParserDelegator;

public class DTDTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.DTD");
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
        check(DTD.FILE_VERSION == 1, "FILE_VERSION == 1");

        // Loading the default DTD populates the html32 entry.
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        check(dtd != null, "html32 non-null");
        check("html32".equals(dtd.getName()), "html32 getName");
        check("html32".equals(dtd.toString()), "html32 toString");

        // getDTD lowercases the requested name and reuses the cached instance.
        DTD upper = DTD.getDTD("HTML32");
        check(upper == dtd, "getDTD(HTML32) reuses cached html32");
        check("html32".equals(upper.getName()), "uppercase normalized to lowercase name");

        // A DTD requested without a bundled archive is still a usable empty DTD.
        DTD d401 = DTD.getDTD("html401");
        check(d401 != null, "html401 non-null");
        check("html401".equals(d401.getName()), "html401 getName");
        check(d401.body != null && d401.html != null && d401.p != null && d401.title != null, "html401 has core element fields");

        // element lookups: by name, by index, self-consistency
        Element body = dtd.getElement("body");
        check(body != null, "body element non-null");
        check("body".equals(body.getName()), "body name");
        check(body.getType() == DTDConstants.MODEL, "body type MODEL");
        check(!body.isEmpty(), "body not empty");
        check(body.getContent() != null, "body has content model");
        check(body.getIndex() >= 0, "body index non-negative");
        check(dtd.getElement(body.getIndex()) == body, "getElement(index) round-trips to body");

        Element br = dtd.getElement("br");
        check(br != null, "br element non-null");
        check(br.isEmpty(), "br empty");
        check(br.getType() == DTDConstants.EMPTY, "br type EMPTY");

        // unknown element is auto-created and retrievable
        Element nv = dtd.getElement("never-defined-element");
        check(nv != null, "getElement unknown auto-creates");
        check("never-defined-element".equals(nv.getName()), "unknown element name");

        // entity lookups: named, character index, unknown
        Entity amp = dtd.getEntity("amp");
        check(amp != null, "amp entity non-null");
        check("amp".equals(amp.getName()), "amp name");
        check("&".equals(amp.getString()), "amp string");
        check(amp.isGeneral(), "amp general entity");
        check(!amp.isParameter(), "amp not parameter entity");
        check(dtd.getEntity((int) '&') == amp, "getEntity(int) maps char to amp entity");
        check(dtd.getEntity("no-such-entity") == null, "unknown entity null");

        // defineEntity: first call creates, second call returns the same instance
        char[] zdata = new char[] { 'z' };
        Entity ze = dtd.defineEntity("myent", DTDConstants.GENERAL | DTDConstants.CDATA, zdata);
        check(ze != null, "defineEntity non-null");
        check("myent".equals(ze.getName()), "defineEntity name");
        check("z".equals(ze.getString()), "defineEntity string");
        check(dtd.defineEntity("myent", DTDConstants.GENERAL | DTDConstants.CDATA, zdata) == ze, "defineEntity idempotent");
        check(dtd.getEntity("myent") == ze, "getEntity returns defined entity");

        // defineElement: configures an element and reports its shape
        Element custom = dtd.defineElement("customelem", DTDConstants.EMPTY, false, false, null, null, null, null);
        check(custom != null, "defineElement non-null");
        check("customelem".equals(custom.getName()), "defineElement name");
        check(custom.getType() == DTDConstants.EMPTY, "defineElement type EMPTY");
        check(custom.isEmpty(), "defineElement isEmpty");
        check(!custom.omitStart() && !custom.omitEnd(), "defineElement explicit start/end tags");

        // defineAttributes installs an attribute list onto an element
        AttributeList al = new AttributeList("width");
        dtd.defineAttributes("customelem", al);
        check(dtd.getElement("customelem").getAttribute("width") == al, "defineAttributes installs attribute");
    }

    // ---- Part 3: deep contract coverage ----

    static void expectNPE(String label, Runnable r) {
        try { r.run(); throw new AssertionError(label + ": expected NullPointerException"); }
        catch (NullPointerException expected) { passed++; }
    }
    static void expectAIOOBE(String label, Runnable r) {
        try { r.run(); throw new AssertionError(label + ": expected ArrayIndexOutOfBoundsException"); }
        catch (ArrayIndexOutOfBoundsException expected) { passed++; }
    }

    static final class ProbeDTD extends DTD {
        ProbeDTD(String name) { super(name); }
    }

    static void testDeepCacheContract() throws Exception {
        // getDTD alone does NOT populate the cache: two lookups of a never-registered
        // name return independent fresh DTDs
        DTD x1 = DTD.getDTD("probe-deep-x");
        DTD x2 = DTD.getDTD("probe-deep-x");
        check(x1 != x2, "getDTD without putDTDHash does not cache");
        check("probe-deep-x".equals(x1.getName()) && "probe-deep-x".equals(x2.getName()),
              "both fresh DTDs carry the requested name");
        // putDTDHash registers an instance; getDTD then returns that same instance
        ProbeDTD mine = new ProbeDTD("probe-deep-y");
        DTD.putDTDHash("probe-deep-y", mine);
        check(DTD.getDTD("probe-deep-y") == mine, "putDTDHash then getDTD returns same instance");
        check(DTD.getDTD("PROBE-DEEP-Y") == mine, "getDTD lowercases before lookup");
        // a later putDTDHash under the same name replaces the registration
        ProbeDTD other = new ProbeDTD("probe-deep-y");
        DTD.putDTDHash("probe-deep-y", other);
        check(DTD.getDTD("probe-deep-y") == other, "second putDTDHash replaces registration");
        // putDTDHash itself stores verbatim (no case folding); since getDTD always
        // lowercases its argument, a mixed-case registration is unreachable via getDTD
        ProbeDTD mixed = new ProbeDTD("Probe-Deep-Mixed");
        DTD.putDTDHash("Probe-Deep-Mixed", mixed);
        check(DTD.getDTD("Probe-Deep-Mixed") != mixed, "getDTD lowercases: exact-case key unreachable");
        check(DTD.getDTD("probe-deep-mixed") != mixed, "lowercased lookup also misses");
        // null keys are rejected
        expectNPE("getDTD(null)", new Runnable() { public void run() { try { DTD.getDTD(null); } catch (java.io.IOException e) { throw new RuntimeException(e); } } });
        expectNPE("putDTDHash(null,dtd)", new Runnable() { public void run() { DTD.putDTDHash(null, new ProbeDTD("z")); } });
    }

    static void testDeepFreshDTD() throws Exception {
        // null name is accepted by the protected constructor (exposed via a subclass)
        ProbeDTD nullNamed = new ProbeDTD(null);
        check(nullNamed.getName() == null, "null name getName null");
        check(nullNamed.toString() == null, "null name toString null");
        // a freshly constructed DTD contains exactly the 15 predefined elements,
        // in declaration order (field initializers first, then "unknown")
        ProbeDTD fresh = new ProbeDTD("probe-fresh");
        java.util.List<String> names = new java.util.ArrayList<String>();
        for (Element e : fresh.elements) names.add(e.getName());
        java.util.List<String> expected = java.util.Arrays.asList(
            "#pcdata", "html", "meta", "base", "isindex", "head", "body",
            "applet", "param", "p", "title", "style", "link", "script", "unknown");
        check(names.equals(expected), "fresh DTD predefined elements, got " + names);
        check(fresh.elements.size() == 15, "fresh DTD element count 15");
        check(fresh.html != null && fresh.body != null && fresh.p != null && fresh.title != null
              && fresh.pcdata != null && fresh.head != null, "shortcut fields populated");
        check(fresh.getElement("html") == fresh.html, "getElement returns the shortcut instance");
        // three predefined whitespace entities
        check(fresh.getEntity("#RE") != null && "\r".equals(fresh.getEntity("#RE").getString()),
              "#RE is CR");
        check(fresh.getEntity("#RS") != null && "\n".equals(fresh.getEntity("#RS").getString()),
              "#RS is LF");
        check(fresh.getEntity("#SPACE") != null && " ".equals(fresh.getEntity("#SPACE").getString()),
              "#SPACE is space");
        check(fresh.entityHash.size() == 3, "fresh DTD has exactly 3 entities");
        // the predefined "unknown" element is EMPTY with omittable end tag
        Element unknown = fresh.getElement("unknown");
        check(unknown.getType() == DTDConstants.EMPTY, "unknown element type EMPTY");
        check(unknown.isEmpty(), "unknown element isEmpty");
        check(!unknown.omitStart() && unknown.omitEnd(), "unknown element omitStart/omitEnd");
        check(fresh.getElement("unknown") == unknown, "unknown element cached on second lookup");
        // indexes of a fresh DTD are 0..14 following the declaration order
        for (int k = 0; k < fresh.elements.size(); k++) {
            check(fresh.getElement(k) == fresh.elements.get(k), "getElement(" + k + ") matches vector");
        }
    }

    static void testDeepHtml32() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        // the archived html32 DTD holds 88 elements; entity table is fully populated
        check(dtd.elements.size() == 88, "html32 element count 88, got " + dtd.elements.size());
        check(dtd.elementHash.size() == 88, "html32 elementHash size 88");
        check(dtd.entityHash.size() >= 200, "html32 entity table populated");
        // character-keyed entities
        check(dtd.getEntity((int) '&') != null && "amp".equals(dtd.getEntity((int) '&').getName()),
              "char & -> amp");
        check(dtd.getEntity((int) '<') != null && "lt".equals(dtd.getEntity((int) '<').getName()),
              "char < -> lt");
        check(dtd.getEntity((int) '>') != null && "gt".equals(dtd.getEntity((int) '>').getName()),
              "char > -> gt");
        check(dtd.getEntity((int) '"') != null && "quot".equals(dtd.getEntity((int) '"').getName()),
              "char \" -> quot");
        check(dtd.getEntity(0x00A0) != null && "nbsp".equals(dtd.getEntity(0x00A0).getName()),
              "char U+00A0 -> nbsp");
        check(dtd.getEntity(-1) == null, "getEntity(-1) null (no exception)");
        check(dtd.getEntity(0xFFFFFF) == null, "getEntity(unknown char) null");
        // name lookup is case sensitive: "BODY" is NOT "body"
        Element body = dtd.getElement("body");
        Element upper = dtd.getElement("BODY");
        check(upper != body, "getElement is case sensitive");
        check("BODY".equals(upper.getName()), "auto-created BODY keeps its own name");
        check(dtd.getElement("BODY") == upper, "auto-created element cached");
        // index lookup round-trips and vector order agrees
        check(dtd.getElement(body.getIndex()) == body, "index lookup round-trip body");
        check(dtd.elements.get(body.getIndex()) == body, "elements vector agrees with index");
        // body/html/p indexes are the archived ones
        check(body.getIndex() == 6, "body index 6");
        check(dtd.getElement("html").getIndex() == 1, "html index 1");
        check(dtd.getElement("p").getIndex() == 9, "p index 9");
        // field/getter coherence on html32 elements
        check(body.name.equals("body") && body.type == DTDConstants.MODEL, "body fields");
        check(body.oStart && body.omitStart(), "body start tag omittable");
        check(body.oEnd && body.omitEnd(), "body end tag omittable");
        // field/getter coherence for the attribute list head
        Element p = dtd.getElement("p");
        check(p.atts == p.getAttributes(), "p atts field equals getAttributes()");
        check(p.getAttribute("align") == p.atts, "p getAttribute(align) is the list head");
    }

    static void testDeepDefine() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        // defineEntity on an existing name returns the original entity, data NOT replaced
        char[] first = new char[]{'1'};
        char[] second = new char[]{'2'};
        Entity e1 = dtd.defineEntity("probe-defent", DTDConstants.GENERAL | DTDConstants.CDATA, first);
        Entity e2 = dtd.defineEntity("probe-defent", DTDConstants.GENERAL | DTDConstants.CDATA, second);
        check(e1 == e2, "defineEntity twice returns same instance");
        check("1".equals(e1.getString()), "original data kept on re-define");
        check(dtd.getEntity("probe-defent") == e1, "defined entity visible via getEntity");
        // the public defEntity builds a single-char entity
        Entity de = dtd.defEntity("probe-defent2", DTDConstants.GENERAL | DTDConstants.CDATA, 65);
        check(de != null && "A".equals(de.getString()), "defEntity char 65 -> 'A'");
        check(de.isGeneral() && !de.isParameter(), "defEntity general entity");
        check(dtd.defEntity("probe-defent2", DTDConstants.GENERAL, 66) == de,
              "defEntity twice returns same instance");
        // defineElement on a new name creates and registers the element
        Element made = dtd.defineElement("probe-made", DTDConstants.MODEL, true, false,
                                         null, null, null, null);
        check(made != null && "probe-made".equals(made.getName()), "defineElement new element");
        check(dtd.getElement("probe-made") == made, "defined element registered");
        check(made.getType() == DTDConstants.MODEL, "defined element type");
        check(made.omitStart() && !made.omitEnd(), "defined element omit flags");
        check(!made.isEmpty(), "MODEL element not empty");
        // defineElement on an EXISTING name mutates and returns the same instance
        Element again = dtd.defineElement("probe-made", DTDConstants.ANY, false, true,
                                          null, null, null, null);
        check(again == made, "defineElement existing returns same instance");
        check(made.getType() == DTDConstants.ANY, "re-defined type ANY");
        check(!made.omitStart() && made.omitEnd(), "re-defined omit flags");
        check(made.getContent() == null, "re-defined content null");
        // defineAttributes installs an attribute list, auto-creating the element by name
        AttributeList al = new AttributeList("probe-attr");
        dtd.defineAttributes("probe-attr-holder", al);
        Element holder = dtd.getElement("probe-attr-holder");
        check(holder.getAttribute("probe-attr") == al, "defineAttributes installs list");
        check(holder.atts == al, "atts field exposes the same list head");
        // defineAttributes on an existing element prepends nothing: replaces the head
        dtd.defineAttributes("probe-attr-holder", null);
        check(dtd.getElement("probe-attr-holder").getAttributes() == null, "defineAttributes(null) clears");
        dtd.defineAttributes("probe-attr-holder", al);
        check(dtd.getElement("probe-attr-holder").getAttributes() == al, "re-installed");
        // negatives: null keys
        expectNPE("defineEntity(null,..)", new Runnable() { public void run() {
            try { DTD.getDTD("html32").defineEntity(null, DTDConstants.GENERAL, new char[]{'a'}); }
            catch (java.io.IOException e) { throw new RuntimeException(e); } } });
        expectNPE("defineElement(null,..)", new Runnable() { public void run() {
            try { DTD.getDTD("html32").defineElement(null, DTDConstants.EMPTY, false, true, null, null, null, null); }
            catch (java.io.IOException e) { throw new RuntimeException(e); } } });
        expectNPE("defineAttributes(null,..)", new Runnable() { public void run() {
            try { DTD.getDTD("html32").defineAttributes(null, new AttributeList("w")); }
            catch (java.io.IOException e) { throw new RuntimeException(e); } } });
    }

    static void testDeepNegatives() throws Exception {
        new ParserDelegator();
        final DTD dtd = DTD.getDTD("html32");
        expectNPE("getEntity(null)", new Runnable() { public void run() { dtd.getEntity((String) null); } });
        expectNPE("getElement(null)", new Runnable() { public void run() { dtd.getElement((String) null); } });
        expectAIOOBE("getElement(-1)", new Runnable() { public void run() { dtd.getElement(-1); } });
        expectAIOOBE("getElement(size)", new Runnable() { public void run() { dtd.getElement(dtd.elements.size()); } });
        expectAIOOBE("getElement(999999)", new Runnable() { public void run() { dtd.getElement(999999); } });
        expectNPE("read(null)", new Runnable() { public void run() {
            try { dtd.read(null); } catch (java.io.IOException e) { throw new RuntimeException(e); } } });
        // DTD itself is not Serializable, but implements DTDConstants
        check(!(java.io.Serializable.class.isAssignableFrom(DTD.class)), "DTD not Serializable");
        check(dtd instanceof DTDConstants, "DTD instanceof DTDConstants");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        // deep tests first: testBlackBox creates two extra elements ("never-defined-element",
        // "customelem") whose presence the pristine-count assertion must not see
        testDeepCacheContract();
        testDeepFreshDTD();
        testDeepHtml32();
        testDeepDefine();
        testDeepNegatives();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
