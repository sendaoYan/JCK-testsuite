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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
