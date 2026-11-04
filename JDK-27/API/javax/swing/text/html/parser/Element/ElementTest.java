/*
 * @test
 * @summary javax.swing.text.html.parser.Element conformance: white-box structure + black-box behavior.
 * @run main ElementTest
 */

import java.lang.reflect.Modifier;
import java.util.Enumeration;

import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.ParserDelegator;

public class ElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.Element");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.html.parser.DTDConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static String join(Enumeration<?> e) {
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) sb.append(e.nextElement()).append('|');
        return sb.toString();
    }

    static void testBlackBox() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");

        // p: model element with an enumerated align attribute
        Element p = dtd.getElement("p");
        check("p".equals(p.getName()), "p name");
        check("p".equals(p.toString()), "p toString");
        check(p.getType() == DTDConstants.MODEL, "p type MODEL");
        check(!p.isEmpty(), "p not empty");
        check(!p.omitStart(), "p start tag required");
        check(p.omitEnd(), "p end tag omittable");
        check(p.getContent() != null, "p has content model");

        // br: EMPTY element with no content model
        Element br = dtd.getElement("br");
        check(br.isEmpty(), "br empty");
        check(br.getType() == DTDConstants.EMPTY, "br type EMPTY");
        check(br.getContent() == null, "br content model null");

        // attribute lookup by name
        AttributeList align = p.getAttribute("align");
        check(align != null, "p align attribute");
        check("align".equals(align.getName()), "align name");
        check(align.getType() == DTDConstants.NMTOKEN, "align type NMTOKEN");
        check(align.getModifier() == DTDConstants.IMPLIED, "align modifier IMPLIED");
        check(align.getValue() == null, "align default value null");
        check("left|center|right|".equals(join(align.getValues())), "align enumerated values");

        // attribute lookup by value (reverse lookup)
        check(p.getAttributeByValue("center") == align, "getAttributeByValue center");
        check(p.getAttributeByValue("left") == align, "getAttributeByValue left");
        check(p.getAttributeByValue("no-such-value") == null, "getAttributeByValue unknown null");
        check(p.getAttribute("no-such-attr") == null, "getAttribute unknown null");

        // input: EMPTY element with a defaulted enumerated attribute
        Element input = dtd.getElement("input");
        check(input.isEmpty(), "input empty");
        AttributeList type = input.getAttribute("type");
        check(type != null, "input type attribute");
        check("TEXT".equals(type.getValue()), "input type default value TEXT");
        check("text|password|checkbox|radio|submit|reset|file|hidden|image|".equals(join(type.getValues())), "input type enumerated values");

        // getAttributes() returns the linked attribute list head
        AttributeList first = input.getAttributes();
        check(first != null, "input getAttributes non-null");
        check(first.getName().equals("align"), "input first attribute is align");

        // index self-consistency
        check(p.getIndex() >= 0, "p index non-negative");
        check(dtd.getElement(p.getIndex()) == p, "getElement(index) round-trip to p");

        // static name2type converter (CDATA/RCDATA/EMPTY/ANY only, else 0)
        check(Element.name2type("CDATA") == DTDConstants.CDATA, "name2type CDATA");
        check(Element.name2type("RCDATA") == DTDConstants.RCDATA, "name2type RCDATA");
        check(Element.name2type("EMPTY") == DTDConstants.EMPTY, "name2type EMPTY");
        check(Element.name2type("ANY") == DTDConstants.ANY, "name2type ANY");
        check(Element.name2type("MODEL") == 0, "name2type MODEL -> 0");
        check(Element.name2type("unknown") == 0, "name2type unknown -> 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
