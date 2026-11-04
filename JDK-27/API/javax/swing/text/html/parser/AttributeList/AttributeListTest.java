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

    // ---- Part 3: deep contract coverage ----

    static void expectNPE(String label, Runnable r) {
        try { r.run(); throw new AssertionError(label + ": expected NullPointerException"); }
        catch (NullPointerException expected) { passed++; }
    }

    static void testDeepName2TypeMatrix() {
        // uppercase content-type names -> their DTDConstants value
        String[][] content = {
            {"CDATA", "1"}, {"ENTITY", "2"}, {"ENTITIES", "3"}, {"ID", "4"}, {"IDREF", "5"},
            {"IDREFS", "6"}, {"NAME", "7"}, {"NAMES", "8"}, {"NMTOKEN", "9"}, {"NMTOKENS", "10"},
            {"NOTATION", "11"}, {"NUMBER", "12"}, {"NUMBERS", "13"}, {"NUTOKEN", "14"}, {"NUTOKENS", "15"},
        };
        for (String[] p : content) {
            check(AttributeList.name2type(p[0]) == Integer.parseInt(p[1]),
                  "name2type(" + p[0] + ")==" + p[1]);
        }
        // lowercase attribute-modifier names -> their modifier constant
        check(AttributeList.name2type("fixed") == DTDConstants.FIXED, "name2type fixed -> FIXED");
        check(AttributeList.name2type("required") == DTDConstants.REQUIRED, "name2type required -> REQUIRED");
        check(AttributeList.name2type("current") == DTDConstants.CURRENT, "name2type current -> CURRENT");
        check(AttributeList.name2type("conref") == DTDConstants.CONREF, "name2type conref -> CONREF");
        check(AttributeList.name2type("implied") == DTDConstants.IMPLIED, "name2type implied -> IMPLIED");
        // lookup is case sensitive: RCDATA/EMPTY/MODEL/ANY are element (not attribute) types -> default CDATA
        check(AttributeList.name2type("RCDATA") == DTDConstants.CDATA, "name2type RCDATA -> CDATA default");
        check(AttributeList.name2type("EMPTY") == DTDConstants.CDATA, "name2type EMPTY -> CDATA default");
        check(AttributeList.name2type("MODEL") == DTDConstants.CDATA, "name2type MODEL -> CDATA default");
        check(AttributeList.name2type("ANY") == DTDConstants.CDATA, "name2type ANY -> CDATA default");
        check(AttributeList.name2type("Fixed") == DTDConstants.CDATA, "name2type Fixed(case) -> CDATA default");
        check(AttributeList.name2type("") == DTDConstants.CDATA, "name2type(\"\") -> CDATA default");
        check(AttributeList.name2type("zzz") == DTDConstants.CDATA, "name2type unknown -> CDATA default");
        expectNPE("name2type(null)", new Runnable() { public void run() { AttributeList.name2type(null); } });
    }

    static void testDeepType2NameMatrix() {
        // 1..15 round-trip; everything else -> null
        String[] names = {null, "CDATA", "ENTITY", "ENTITIES", "ID", "IDREF", "IDREFS", "NAME", "NAMES",
                          "NMTOKEN", "NMTOKENS", "NOTATION", "NUMBER", "NUMBERS", "NUTOKEN", "NUTOKENS"};
        for (int v = 0; v <= 19; v++) {
            String expected = (v < names.length) ? names[v] : null;
            String got = AttributeList.type2name(v);
            check((expected == null && got == null) || (expected != null && expected.equals(got)),
                  "type2name(" + v + ")==" + expected);
        }
        check(AttributeList.type2name(-1) == null, "type2name(-1) null");
        check(AttributeList.type2name(20) == null, "type2name(20) null");
        // round-trip for every valid value
        for (int v = 1; v <= 15; v++) {
            check(AttributeList.name2type(AttributeList.type2name(v)) == v, "round-trip " + v);
        }
    }

    static void testDeepFieldsAndEdges() {
        // public fields mirror getters, no defensive copies
        Vector<String> vals = new Vector<String>();
        vals.add("one"); vals.add("two");
        AttributeList full = new AttributeList("nm", DTDConstants.NUMBER, DTDConstants.FIXED, "one", vals, null);
        check(full.name.equals("nm"), "field name");
        check(full.type == DTDConstants.NUMBER, "field type");
        check(full.modifier == DTDConstants.FIXED, "field modifier");
        check(full.value.equals("one"), "field value");
        check(full.values == vals, "field values is supplied vector (no copy)");
        check(full.next == null, "field next null");
        check("nm".equals(full.toString()), "full ctor toString returns just the name");
        vals.add("three");
        check(full.getValues().hasMoreElements(), "live vector reflected in getValues");

        // empty (but non-null) values vector -> non-null enumeration with no elements
        AttributeList emptyVals = new AttributeList("k", DTDConstants.CDATA, 0, null, new Vector<String>(), null);
        check(emptyVals.getValues() != null, "empty vector -> enumeration non-null");
        check(!emptyVals.getValues().hasMoreElements(), "empty vector -> no elements");
        // null values -> null enumeration
        AttributeList nullVals = new AttributeList("k2", DTDConstants.CDATA, 0, null, null, null);
        check(nullVals.getValues() == null, "null vector -> null enumeration");

        // null name is accepted by the constructor
        AttributeList nullName = new AttributeList(null);
        check(nullName.getName() == null, "null name getName");
        check(nullName.toString() == null, "null name toString null");
        check(nullName.getType() == 0 && nullName.getModifier() == 0, "null name type/modifier 0");

        // three-deep chain walk via getNext
        AttributeList c = new AttributeList("c");
        AttributeList bNode = new AttributeList("b", 0, 0, null, null, c);
        AttributeList aNode = new AttributeList("a", 0, 0, null, null, bNode);
        check(aNode.getNext() == bNode && bNode.getNext() == c && c.getNext() == null, "chain a->b->c->null");

        // html32: input's type attribute has default value TEXT
        try {
            new ParserDelegator();
            DTD dtd = DTD.getDTD("html32");
            AttributeList typeAttr = dtd.getElement("input").getAttribute("type");
            check(typeAttr != null && "TEXT".equals(typeAttr.getValue()), "input type default TEXT");
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    static void testDeepSerialization() throws Exception {
        // serialVersionUID stability
        check(java.io.ObjectStreamClass.lookup(AttributeList.class).getSerialVersionUID()
              == -1361214058742015233L, "serialVersionUID");
        Vector<String> vals = new Vector<String>();
        vals.add("x"); vals.add("y");
        AttributeList orig = new AttributeList("ser", DTDConstants.ID, DTDConstants.REQUIRED, "v", vals, null);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray()));
        AttributeList back = (AttributeList) ois.readObject();
        ois.close();
        check("ser".equals(back.getName()), "roundtrip name");
        check(back.getType() == DTDConstants.ID, "roundtrip type");
        check(back.getModifier() == DTDConstants.REQUIRED, "roundtrip modifier");
        check("v".equals(back.getValue()), "roundtrip value");
        check(back.getNext() == null, "roundtrip next null");
        StringBuilder sb = new StringBuilder();
        Enumeration<?> e = back.getValues();
        while (e.hasMoreElements()) sb.append(e.nextElement());
        check("xy".equals(sb.toString()), "roundtrip values x,y");
        // round-trip chain serialization keeps the link
        AttributeList tail = new AttributeList("t2");
        AttributeList head2 = new AttributeList("t1", 0, 0, null, null, tail);
        bos.reset();
        oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(head2);
        oos.close();
        ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        AttributeList back2 = (AttributeList) ois.readObject();
        ois.close();
        check(back2.getNext() != null && "t2".equals(back2.getNext().getName()), "roundtrip chain next link");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepName2TypeMatrix();
        testDeepType2NameMatrix();
        testDeepFieldsAndEdges();
        testDeepSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
