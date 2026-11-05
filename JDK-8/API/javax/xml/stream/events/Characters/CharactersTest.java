/*
 * @test
 * @summary javax.xml.stream.events.Characters conformance: interface hierarchy and methods.
 * @run main CharactersTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class CharactersTest {
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
        Class<?> c = Class.forName("javax.xml.stream.events.Characters");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.stream.events.XMLEvent"), "implements javax.xml.stream.events.XMLEvent");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getData"), "has method getData");
        check(names.contains("isWhiteSpace"), "has method isWhiteSpace");
        check(names.contains("isCData"), "has method isCData");
        check(names.contains("isIgnorableWhiteSpace"), "has method isIgnorableWhiteSpace");
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (factory matrix + parsed three-state matrix) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory three-state matrix: plain / cdata / whitespace / ignorable / empty
        javax.xml.stream.events.Characters plain = ef.createCharacters("abc");
        check("abc".equals(plain.getData()) && !plain.isCData() && !plain.isWhiteSpace() && !plain.isIgnorableWhiteSpace(),
                "plain: F/F/F");
        check(plain.getEventType() == 4 && plain.isCharacters(), "plain code 4");
        javax.xml.stream.events.Characters cd = ef.createCData("x");
        check(cd.isCData() && !cd.isWhiteSpace() && !cd.isIgnorableWhiteSpace(), "cdata: T/F/F");
        javax.xml.stream.events.Characters sp = ef.createSpace("   ");
        check(!sp.isCData() && sp.isWhiteSpace() && !sp.isIgnorableWhiteSpace(), "space: F/T/F");
        check(sp.getEventType() == 4, "createSpace still CHARACTERS code 4");
        javax.xml.stream.events.Characters ig = ef.createIgnorableSpace("\t\n");
        check(!ig.isCData() && ig.isWhiteSpace() && ig.isIgnorableWhiteSpace(), "ignorable: F/T/T");
        javax.xml.stream.events.Characters empty = ef.createCharacters("");
        check("".equals(empty.getData()) && !empty.isWhiteSpace(), "empty string not whitespace");
        javax.xml.stream.events.Characters wsChars = ef.createCharacters("   ");
        check(wsChars.isWhiteSpace(), "createCharacters('   ') isWhiteSpace true");
        // CDATA content with markup chars kept verbatim, encoded wraps in CDATA section
        javax.xml.stream.events.Characters cdv = ef.createCData("<&]]>");
        java.io.StringWriter w = new java.io.StringWriter();
        cdv.writeAsEncodedUnicode(w);
        check("<![CDATA[<&]]>]]>".equals(w.toString()), "cdata encoded exact");
        // createCharacters(null) tolerated
        javax.xml.stream.events.Characters nul = ef.createCharacters(null);
        check(nul.getData() == null, "createCharacters(null) getData null");
        // self cast + wrong casts
        check(plain.asCharacters() == plain, "asCharacters self");
        try { plain.asEndElement(); check(false, "CH.asEndElement must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { plain.asStartElement(); check(false, "CH.asStartElement must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 2) parsed plain text: F/F/F
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader("<r>plain text</r>"));
        r.nextEvent(); r.nextEvent();
        javax.xml.stream.events.Characters pc = (javax.xml.stream.events.Characters) r.nextEvent();
        check("plain text".equals(pc.getData()) && !pc.isCData() && !pc.isWhiteSpace() && !pc.isIgnorableWhiteSpace(),
                "parsed plain: F/F/F");
        check(pc.getEventType() == 4, "parsed plain code 4");
        r.close();

        // 3) parsed whitespace between child elements: CHARACTERS(4) with ws=T, ig=F (no DTD -> not ignorable)
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader("<r>  <c/>  </r>"));
        r2.nextEvent(); r2.nextEvent();
        javax.xml.stream.events.Characters ws1 = (javax.xml.stream.events.Characters) r2.nextEvent();
        check("  ".equals(ws1.getData()) && ws1.isWhiteSpace() && !ws1.isIgnorableWhiteSpace() && ws1.getEventType() == 4,
                "inter-element whitespace: ws=T ig=F code 4 (SPACE not used)");
        r2.close();

        // 4) parsed CDATA section: impl reports CHARACTERS with isCData()=false (quirk, both sides)
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(
                new java.io.StringReader("<r><![CDATA[<cdata&>]]>t1</r>"));
        r3.nextEvent(); r3.nextEvent();
        javax.xml.stream.events.Characters c1 = (javax.xml.stream.events.Characters) r3.nextEvent();
        check("<cdata&>".equals(c1.getData()) && !c1.isCData(), "parsed CDATA keeps text, isCData=false (impl quirk)");
        javax.xml.stream.events.Characters c2 = (javax.xml.stream.events.Characters) r3.nextEvent();
        check("t1".equals(c2.getData()) && !c2.isCData(), "text after CDATA separate event");
        r3.close();

        // 5) default (replacing=true) splits text at predefined entities and char refs
        javax.xml.stream.XMLEventReader r4 = ifac.createXMLEventReader(new java.io.StringReader("<r>t1&amp;t2&#65;</r>"));
        r4.nextEvent(); r4.nextEvent();
        String[] chunks = {"t1", "&", "t2", "A"};
        int i = 0;
        while (i < 4) {
            javax.xml.stream.events.XMLEvent e = r4.nextEvent();
            check(e.isCharacters() && chunks[i].equals(e.asCharacters().getData()), "chunk[" + i + "]=" + chunks[i]);
            i++;
        }
        r4.close();

        // 6) IS_COALESCING=true merges entities + CDATA + text into one event (isCData still false)
        javax.xml.stream.XMLInputFactory cf = javax.xml.stream.XMLInputFactory.newFactory();
        cf.setProperty(javax.xml.stream.XMLInputFactory.IS_COALESCING, true);
        javax.xml.stream.XMLEventReader r5 = cf.createXMLEventReader(
                new java.io.StringReader("<r>t1&amp;t2<![CDATA[<c>]]>z</r>"));
        r5.nextEvent(); r5.nextEvent();
        javax.xml.stream.events.Characters co = (javax.xml.stream.events.Characters) r5.nextEvent();
        check("t1&t2<c>z".equals(co.getData()) && !co.isCData(), "coalesced single event");
        r5.close();
    }
}
