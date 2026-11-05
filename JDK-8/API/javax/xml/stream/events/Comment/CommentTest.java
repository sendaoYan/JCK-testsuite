/*
 * @test
 * @summary javax.xml.stream.events.Comment conformance: interface hierarchy and methods.
 * @run main CommentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class CommentTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.Comment");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getText"), "has method getText");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (factory + parsed + writer round-trip) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory createComment: text equality, code, family, encoded form
        javax.xml.stream.events.Comment cm = ef.createComment(" note ");
        check(" note ".equals(cm.getText()), "factory comment text equality");
        check(cm.getEventType() == 5, "comment code 5");
        check(!cm.isCharacters() && !cm.isStartElement() && !cm.isEndElement() && !cm.isStartDocument()
                && !cm.isEndDocument() && !cm.isProcessingInstruction(), "comment isXxx family all false");
        java.io.StringWriter w = new java.io.StringWriter();
        cm.writeAsEncodedUnicode(w);
        check("<!-- note -->".equals(w.toString()), "encoded <!-- note -->");
        // special XML chars kept verbatim inside comment
        javax.xml.stream.events.Comment cm2 = ef.createComment("a<b>&\"'");
        java.io.StringWriter w2 = new java.io.StringWriter();
        cm2.writeAsEncodedUnicode(w2);
        check("<!--a<b>&\"'-->".equals(w2.toString()), "comment content not escaped");
        // createComment(null) tolerated
        javax.xml.stream.events.Comment cmn = ef.createComment(null);
        check(cmn != null && cmn.getText() == null, "createComment(null) getText null");
        try { cm.asCharacters(); check(false, "Comment.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 2) parsed comments: before-root and after-root, text exact
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<!--c1--><r>x</r><!--tail-->"));
        r.nextEvent(); // START_DOCUMENT always first
        javax.xml.stream.events.XMLEvent e = r.nextEvent();
        check(e.getEventType() == 5 && "c1".equals(((javax.xml.stream.events.Comment) e).getText()),
                "prolog comment c1");
        javax.xml.stream.events.Comment tail = null;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent x = r.nextEvent();
            if (x.getEventType() == 5) tail = (javax.xml.stream.events.Comment) x;
        }
        check(tail != null && "tail".equals(tail.getText()), "epilog comment tail");
        check(tail.getLocation().getLineNumber() >= 1, "comment location line >= 1");
        r.close();

        // 3) comment inside element content preserved in order
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r>a<!--mid-->b</r>"));
        r2.nextEvent(); r2.nextEvent();
        check("a".equals(r2.nextEvent().asCharacters().getData()), "text a");
        javax.xml.stream.events.XMLEvent mid = r2.nextEvent();
        check(mid.getEventType() == 5 && "mid".equals(((javax.xml.stream.events.Comment) mid).getText()), "mid comment");
        check("b".equals(r2.nextEvent().asCharacters().getData()), "text b");
        r2.close();

        // 4) round-trip: factory comment -> XMLEventWriter -> output contains exact form
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos);
        ew.add(ef.createStartDocument("UTF-8", "1.0"));
        ew.add(ef.createComment("c1"));
        ew.add(ef.createEndDocument());
        ew.flush();
        String out = new String(bos.toByteArray(), "UTF-8");
        check(out.contains("<!--c1-->"), "writer round-trip contains <!--c1-->");
        ew.close();
    }
}
