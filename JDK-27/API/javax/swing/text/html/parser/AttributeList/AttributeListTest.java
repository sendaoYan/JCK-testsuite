/*
 * @test
 * @summary javax.swing.text.html.parser.AttributeList conformance: white-box structure + black-box behavior.
 * @run main AttributeListTest
 */

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.ParserDelegator;

public class AttributeListTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.AttributeList");
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
    static void testBlackBox() throws Exception {
        // static attribute-type name <-> value converters
        check(AttributeList.name2type("CDATA") == DTDConstants.CDATA, "name2type CDATA");
        check(AttributeList.name2type("ID") == DTDConstants.ID, "name2type ID");
        check(AttributeList.name2type("NMTOKEN") == DTDConstants.NMTOKEN, "name2type NMTOKEN");
        check(AttributeList.name2type("NUMBER") == DTDConstants.NUMBER, "name2type NUMBER");
        check(AttributeList.name2type("fixed") == DTDConstants.FIXED, "name2type fixed");
        check(AttributeList.name2type("required") == DTDConstants.REQUIRED, "name2type required");
        check(AttributeList.name2type("implied") == DTDConstants.IMPLIED, "name2type implied");
        check(AttributeList.name2type("no-such-type") == DTDConstants.CDATA, "name2type unknown -> CDATA");

        check("CDATA".equals(AttributeList.type2name(DTDConstants.CDATA)), "type2name CDATA");
        check("NMTOKEN".equals(AttributeList.type2name(DTDConstants.NMTOKEN)), "type2name NMTOKEN");
        check("ID".equals(AttributeList.type2name(DTDConstants.ID)), "type2name ID");
        check(AttributeList.type2name(999_999) == null, "type2name unknown -> null");

        // simple constructor
        AttributeList one = new AttributeList("href");
        check("href".equals(one.getName()), "simple ctor getName");
        check(one.getType() == 0, "simple ctor type 0");
        check(one.getModifier() == 0, "simple ctor modifier 0");
        check(one.getValue() == null, "simple ctor value null");
        check(one.getValues() == null, "simple ctor values null");
        check(one.getNext() == null, "simple ctor next null");
        check("href".equals(one.toString()), "simple ctor toString");

        // full constructor + chained linked list via next
        Vector<String> vals = new Vector<String>();
        vals.add("left"); vals.add("center"); vals.add("right");
        AttributeList next = new AttributeList("align", DTDConstants.NMTOKEN, DTDConstants.IMPLIED, null, vals, null);
        AttributeList head = new AttributeList("dir", DTDConstants.NMTOKEN, DTDConstants.IMPLIED, "left", vals, next);
        check("dir".equals(head.getName()), "head name");
        check(head.getType() == DTDConstants.NMTOKEN, "head type");
        check(head.getModifier() == DTDConstants.IMPLIED, "head modifier");
        check("left".equals(head.getValue()), "head default value");
        check(head.getNext() == next, "head next link");
        check(next.getNext() == null, "next tail null");

        Enumeration<?> e = head.getValues();
        check(e != null, "values enumeration non-null");
        StringBuilder sb = new StringBuilder();
        while (e.hasMoreElements()) sb.append(e.nextElement()).append('|');
        check("left|center|right|".equals(sb.toString()), "values enumeration content");

        // obtain from a real DTD (html32): p has a single align attribute
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        AttributeList align = dtd.getElement("p").getAttribute("align");
        check(align != null, "p align attribute present");
        check("align".equals(align.getName()), "p align name");
        check(align.getType() == DTDConstants.NMTOKEN, "p align type NMTOKEN");
        check(align.getModifier() == DTDConstants.IMPLIED, "p align modifier IMPLIED");
        check(align.getValue() == null, "p align default null");
        check(align.getNext() == null, "p align is sole attribute");
        Enumeration<?> av = align.getValues();
        StringBuilder ab = new StringBuilder();
        while (av.hasMoreElements()) ab.append(av.nextElement()).append('|');
        check("left|center|right|".equals(ab.toString()), "p align values left|center|right");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
