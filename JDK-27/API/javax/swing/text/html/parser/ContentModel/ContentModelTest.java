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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
