/*
 * @test
 * @summary javax.swing.text.html.parser.ContentModel conformance: white-box structure + black-box behavior.
 * @run main ContentModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Vector;

import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.ParserDelegator;

public class ContentModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.parser.ContentModel");
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
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        new ParserDelegator();
        DTD dtd = DTD.getDTD("html32");
        Element b = dtd.getElement("b");
        Element i = dtd.getElement("i");

        // default constructor
        ContentModel def = new ContentModel();
        check(def.type == 0, "default type 0");
        check(def.content == null, "default content null");
        check(def.next == null, "default next null");
        check(!def.empty(), "default empty() false");
        check(def.first() == null, "default first() null");

        // element constructor
        ContentModel mB = new ContentModel(b);
        check(mB.type == 0, "element ctor type 0");
        check(mB.content == b, "element ctor content");
        check(mB.first() == b, "element ctor first()");
        check(!mB.empty(), "element ctor empty() false");
        check(mB.first(b), "element ctor first(b) true");
        check(!mB.first(i), "element ctor first(i) false");
        check("b".equals(mB.toString()), "element ctor toString");

        // '*' (zero-or-more) model
        ContentModel star = new ContentModel('*', new ContentModel(b));
        check(star.type == '*', "star type");
        check(star.empty(), "star empty() true");
        check(star.first() == null, "star first() null");
        check(star.first(b), "star first(b) true");

        // '?' (optional) model
        ContentModel opt = new ContentModel('?', new ContentModel(b));
        check(opt.type == '?', "opt type");
        check(opt.empty(), "opt empty() true");

        // '+' (one-or-more) model
        ContentModel plus = new ContentModel('+', new ContentModel(b));
        check(plus.type == '+', "plus type");
        check(!plus.empty(), "plus empty() false");
        check(plus.first() == b, "plus first() == b");

        // ',' sequence: (b , i), built as a linked list
        ContentModel mI = new ContentModel(i);
        ContentModel mB2 = new ContentModel(b);
        mB2.next = mI; // link b -> i
        ContentModel seq = new ContentModel(',', mB2, null);
        check(seq.type == ',', "seq type");
        check(!seq.empty(), "seq empty() false");
        check(seq.first() == b, "seq first() == b");
        check(seq.first(b), "seq first(b) true");
        check(!seq.first(i), "seq first(i) false");
        Vector<Element> elems = new Vector<Element>();
        seq.getElements(elems);
        check(elems.size() == 2, "seq getElements size 2");
        check(elems.get(0) == b && elems.get(1) == i, "seq getElements order b,i");
        check("(b , i)".equals(seq.toString()), "seq toString (b , i)");
    }

    // ---- Part 3: deep contract coverage ----

    static void testDeepOperators() {
        new ParserDelegator();
        DTD dtd;
        try { dtd = DTD.getDTD("html32"); } catch (java.io.IOException e) { throw new AssertionError(e); }
        Element b = dtd.getElement("b");
        Element i = dtd.getElement("i");
        Element u = dtd.getElement("u");

        // '?' optional model: token acceptance and string form
        ContentModel opt = new ContentModel('?', new ContentModel(b));
        check(opt.first(b), "opt first(b) true");
        check(!opt.first(i), "opt first(i) false");
        check("b?".equals(opt.toString()), "opt toString b?");
        check(opt.content instanceof ContentModel, "opt content is ContentModel");

        // '+' one-or-more
        ContentModel plus = new ContentModel('+', new ContentModel(b));
        check(plus.first(b), "plus first(b) true");
        check(!plus.first(i), "plus first(i) false");
        check("b+".equals(plus.toString()), "plus toString b+");

        // '*' zero-or-more
        ContentModel star = new ContentModel('*', new ContentModel(b));
        check(!star.first(i), "star first(i) false");
        check("b*".equals(star.toString()), "star toString b*");

        // '|' alternation: empty false, first() null (ambiguous), first(elem) true for members
        ContentModel mB = new ContentModel(b);
        ContentModel mI = new ContentModel(i);
        mB.next = mI;
        ContentModel or = new ContentModel('|', mB, null);
        check(!or.empty(), "or empty false");
        check(or.first() == null, "or first() null");
        check(or.first(b), "or first(b) true");
        check(or.first(i), "or first(i) true");
        check(!or.first(u), "or first(u) false");
        check("(b | i)".equals(or.toString()), "or toString (b | i)");
        java.util.Vector<Element> vo = new java.util.Vector<Element>();
        or.getElements(vo);
        check(vo.size() == 2 && vo.get(0) == b && vo.get(1) == i, "or getElements [b,i]");

        // '&' conjunction: first() null, members accepted
        ContentModel and = new ContentModel('&', mB, null);
        check(!and.empty(), "and empty false");
        check(and.first() == null, "and first() null");
        check(and.first(i), "and first(i) true");
        check("(b & i)".equals(and.toString()), "and toString (b & i)");

        // single-member group prints without separator
        ContentModel single = new ContentModel(',', new ContentModel(b), null);
        check("(b)".equals(single.toString()), "single-member seq toString (b)");
        check(single.first() == b, "single-member first() == b");

        // three-element sequence b,i,u
        ContentModel mU = new ContentModel(u);
        mI.next = mU;
        ContentModel seq3 = new ContentModel(',', mB, null);
        check("(b , i , u)".equals(seq3.toString()), "seq3 toString (b , i , u)");
        java.util.Vector<Element> v3 = new java.util.Vector<Element>();
        seq3.getElements(v3);
        check(v3.size() == 3 && v3.get(0) == b && v3.get(1) == i && v3.get(2) == u, "seq3 getElements order");

        // type-0 model with a plain Element content
        Element q = dtd.getElement("cm-probe-q");
        ContentModel plain = new ContentModel(0, (Object) q, null);
        check("cm-probe-q".equals(plain.toString()), "type0 Element toString name");
        check(plain.first() == q, "type0 first() == element");
        check(!plain.empty(), "type0 empty false");
        check(plain.first(q), "type0 first(q) true");
        java.util.Vector<Element> vq = new java.util.Vector<Element>();
        plain.getElements(vq);
        check(vq.size() == 1 && vq.get(0) == q, "type0 getElements adds element");

        // content of a non-Element, non-ContentModel type: toString works, first() CCE
        ContentModel strModel = new ContentModel(0, (Object) "str", null);
        check("str".equals(strModel.toString()), "type0 String toString");
        try {
            strModel.first();
            throw new AssertionError("expected ClassCastException");
        } catch (ClassCastException expected) { passed++; }
    }

    static void testDeepHtml32Models() {
        new ParserDelegator();
        DTD dtd;
        try { dtd = DTD.getDTD("html32"); } catch (java.io.IOException e) { throw new AssertionError(e); }
        // archived html32 content models (stable across JDK versions)
        check("(head , body , plaintext?)".equals(dtd.getElement("html").getContent().toString()),
              "html content model");
        check("(title? & isindex? & base? & nextid?)".equals(dtd.getElement("head").getContent().toString()),
              "head content model");
        check("(tr | caption)*".equals(dtd.getElement("table").getContent().toString()),
              "table content model");
        check(dtd.getElement("body").getContent() != null, "body content non-null");
        check(dtd.getElement("br").getContent() == null, "br (EMPTY) content null");
        // getElements flattens a real model without duplicates of membership
        java.util.Vector<Element> ve = new java.util.Vector<Element>();
        dtd.getElement("table").getContent().getElements(ve);
        check(ve.size() == 2 && ve.get(0).getName().equals("tr") && ve.get(1).getName().equals("caption"),
              "table getElements [tr, caption]");
    }

    static void testDeepSerialization() throws Exception {
        new ParserDelegator();
        DTD dtd;
        try { dtd = DTD.getDTD("html32"); } catch (java.io.IOException e) { throw new AssertionError(e); }
        check(java.io.ObjectStreamClass.lookup(ContentModel.class).getSerialVersionUID()
              == -1130825523866321257L, "serialVersionUID");
        ContentModel orig = new ContentModel('*', new ContentModel(dtd.getElement("b")));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray()));
        ContentModel back = (ContentModel) ois.readObject();
        ois.close();
        check(back.type == '*', "roundtrip type");
        check("b*".equals(back.toString()), "roundtrip toString b*");
        check(back.first() == null, "roundtrip star first() null");
        check(back.empty(), "roundtrip empty true");
        // the inner Element is deserialized as a distinct graph: identity with the
        // live DTD elements is NOT restored, but self-consistency is
        check(back.content instanceof ContentModel, "roundtrip inner is ContentModel");
        ContentModel inner = (ContentModel) back.content;
        check(inner.content instanceof Element, "roundtrip inner content is Element");
        Element innerElem = (Element) inner.content;
        check("b".equals(innerElem.getName()), "roundtrip inner element name b");
        check(back.first(innerElem), "roundtrip first(deserialized b) true");
        check(!back.first(dtd.getElement("i")), "roundtrip first(live i) false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepOperators();
        testDeepHtml32Models();
        testDeepSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
