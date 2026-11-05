/*
 * @test
 * @summary org.w3c.dom.CharacterData conformance: reflection shape checks.
 * @run main CharacterDataTest
 */

import java.lang.reflect.*;

public class CharacterDataTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.CharacterData");
        check(c != null, "org.w3c.dom.CharacterData loads");
        check(c.getName().equals("org.w3c.dom.CharacterData"), "binary name is org.w3c.dom.CharacterData");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.CharacterData is public");
        check(c.isInterface(), "org.w3c.dom.CharacterData is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.Node")), "direct superinterfaces");
        check(declares(c, "getData"), "declares getData");
        check(declares(c, "setData"), "declares setData");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "substringData"), "declares substringData");
        check(declares(c, "appendData"), "declares appendData");
        check(declares(c, "insertData"), "declares insertData");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral CharacterData contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Text t = d.createTextNode("hello");
        check(org.w3c.dom.Node.TEXT_NODE == t.getNodeType(), "text nodeType 3");
        check("hello".equals(t.getData()) && "hello".equals(t.getNodeValue()) && "hello".equals(t.getTextContent()), "initial data via getData/getNodeValue/getTextContent");
        check(t.getLength() == 5, "getLength counts UTF-16 units");
        // three write channels stay in sync
        t.setData("a1");
        check("a1".equals(t.getNodeValue()), "setData syncs getNodeValue");
        t.setNodeValue("a2");
        check("a2".equals(t.getData()), "setNodeValue syncs getData");
        t.setTextContent("a3");
        check("a3".equals(t.getData()), "setTextContent syncs getData");
        t.setData("a4");
        check("a4".equals(t.getTextContent()), "getData syncs getTextContent");
        // null handling: setData(null) stores null, later getLength NPEs (Xerces), setData recovers
        t.setData(null);
        check(t.getData() == null, "setData(null) leaves getData null");
        boolean npe = false;
        try { t.getLength(); } catch (NullPointerException expected) { npe = true; }
        check(npe, "getLength after setData(null) -> NullPointerException (both sides, message differs)");
        t.setData("ok");
        check(t.getLength() == 2, "setData recovers after null state");
        check(d.createTextNode(null).getData() == null, "createTextNode(null) -> null data");
        // appendData
        t.setData("a");
        t.appendData(null);
        check("a".equals(t.getData()), "appendData(null) is a no-op (Xerces)");
        t.appendData("XY");
        check("aXY".equals(t.getData()) && t.getLength() == 3, "appendData grows data and length");
        // substringData matrix
        t.setData("hello");
        check("hello".equals(t.substringData(0, 5)), "substringData(0,len) whole string");
        check("ell".equals(t.substringData(1, 3)), "substringData middle");
        check("o".equals(t.substringData(4, 1)), "substringData last char");
        check("o".equals(t.substringData(4, 2)), "substringData clamps count past end (offset+count>len allowed)");
        check("hello".equals(t.substringData(0, 6)), "substringData(0,len+1) clamps");
        check("llo".equals(t.substringData(2, 3)), "substringData tail");
        try { t.substringData(6, 1); check(false, "substringData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(6,1) -> INDEX_SIZE_ERR"); }
        try { t.substringData(5, 0); check(false, "substringData offset==len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(len,0) -> INDEX_SIZE_ERR (offset==len rejected)"); }
        try { t.substringData(0, -1); check(false, "substringData negative count must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(0,-1) -> INDEX_SIZE_ERR"); }
        try { t.substringData(-1, 2); check(false, "substringData negative offset must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(-1,2) -> INDEX_SIZE_ERR"); }
        // insertData: offset==len appends, offset>len throws
        t.insertData(5, "!");
        check("hello!".equals(t.getData()), "insertData(len,..) appends");
        t.setData("hello");
        try { t.insertData(6, "x"); check(false, "insertData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "insertData(6,..) -> INDEX_SIZE_ERR"); }
        t.insertData(2, "XY");
        check("heXYllo".equals(t.getData()), "insertData middle");
        // deleteData: clamps count, offset==len no-op, offset>len throws
        t.deleteData(3, 100);
        check("heX".equals(t.getData()), "deleteData clamps count past end");
        t.setData("hello");
        t.deleteData(1, 2);
        check("hlo".equals(t.getData()), "deleteData middle");
        t.setData("hello");
        t.deleteData(5, 0);
        check("hello".equals(t.getData()), "deleteData(len,0) no-op allowed");
        try { t.deleteData(6, 0); check(false, "deleteData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "deleteData(6,0) -> INDEX_SIZE_ERR"); }
        // replaceData: grows/shrinks, offset==len appends
        t.setData("hello");
        t.replaceData(0, 2, "J");
        check("Jllo".equals(t.getData()), "replaceData shrink");
        t.setData("hello");
        t.replaceData(5, 0, "X");
        check("helloX".equals(t.getData()), "replaceData(len,0,..) appends");
        try { t.replaceData(7, 1, "x"); check(false, "replaceData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "replaceData(7,1,..) -> INDEX_SIZE_ERR"); }
        // mutation stays visible through the parent's textContent
        org.w3c.dom.Element host = d.createElement("h");
        d.appendChild(host);
        org.w3c.dom.Text ht = d.createTextNode("ab");
        host.appendChild(ht);
        ht.appendData("c");
        check("abc".equals(host.getTextContent()), "character-data edits visible via parent textContent");
        check(host.getChildNodes().getLength() == 1, "data edits never change child count");
        // Comment also exposes CharacterData channels
        org.w3c.dom.Comment c = d.createComment("c");
        check("#comment".equals(c.getNodeName()) && org.w3c.dom.Node.COMMENT_NODE == c.getNodeType(), "comment node identity");
        c.setTextContent("d");
        check("d".equals(c.getData()), "comment setTextContent syncs getData");
        c.setNodeValue("n");
        check("n".equals(c.getData()), "comment setNodeValue syncs getData");
        org.w3c.dom.Comment cn = d.createComment(null);
        check(cn.getData() == null, "createComment(null) -> null data");
        boolean cnpe = false;
        try { cn.getLength(); } catch (NullPointerException expected) { cnpe = true; }
        check(cnpe, "comment getLength after null data -> NullPointerException");
        cn.setData("z");
        check(cn.getLength() == 1, "comment setData recovers length");
    }
}
