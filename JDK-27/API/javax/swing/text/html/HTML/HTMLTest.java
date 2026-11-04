/*
 * @test
 * @summary javax.swing.text.html.HTML conformance: tag/attribute factories, integer
 *          parsing, UnknownTag equality and serialization (headless-safe).
 * @run main HTMLTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTML.UnknownTag;

public class HTMLTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTML", false, HTMLTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isPublic(c.getModifiers()) == true, "public modifier");
        check(Modifier.isProtected(c.getModifiers()) == false, "protected modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAllTags"), "method getAllTags");
        check(names.contains("getTag"), "method getTag");
        check(names.contains("getIntegerAttributeValue"), "method getIntegerAttributeValue");
        check(names.contains("getAllAttributeKeys"), "method getAllAttributeKeys");
        check(names.contains("getAttributeKey"), "method getAttributeKey");
    }

    // Flags observed on both JDK-8 and JDK-27: name:isBlock:breaksFlow:isPreformatted
    static final String[] TAG_TABLE = {
        "a:0:0:0", "address:0:0:0", "applet:0:0:0", "area:0:0:0", "b:0:0:0",
        "base:0:0:0", "basefont:0:0:0", "big:0:0:0", "blockquote:1:1:0",
        "body:1:1:0", "br:0:1:0", "caption:0:0:0", "center:0:1:0", "cite:0:0:0",
        "code:0:0:0", "dd:1:1:0", "dfn:0:0:0", "dir:1:1:0", "div:1:1:0",
        "dl:1:1:0", "dt:1:1:0", "em:0:0:0", "font:0:0:0", "form:0:1:0",
        "frame:0:0:0", "frameset:0:0:0", "h1:1:1:0", "h2:1:1:0", "h3:1:1:0",
        "h4:1:1:0", "h5:1:1:0", "h6:1:1:0", "head:1:1:0", "hr:0:1:0",
        "html:0:1:0", "i:0:0:0", "img:0:0:0", "input:0:0:0", "isindex:0:1:0",
        "kbd:0:0:0", "li:1:1:0", "link:0:0:0", "map:0:0:0", "menu:1:1:0",
        "meta:0:0:0", "nobr:0:0:0", "noframes:1:1:0", "object:0:0:0",
        "ol:1:1:0", "option:0:0:0", "p:1:1:0", "param:0:0:0", "pre:1:1:1",
        "samp:0:0:0", "script:0:0:0", "select:0:0:0", "small:0:0:0",
        "span:0:0:0", "strike:0:0:0", "s:0:0:0", "strong:0:0:0", "style:0:0:0",
        "sub:0:0:0", "sup:0:0:0", "table:1:0:0", "td:1:1:0", "textarea:0:0:1",
        "th:1:1:0", "title:1:1:0", "tr:1:0:0", "tt:0:0:0", "u:0:0:0",
        "ul:1:1:0", "var:0:0:0",
    };

    // ---- Part 2: tag factory contract ----
    static void testTags() {
        check("html".equals(HTML.Tag.HTML.toString()), "HTML.Tag.HTML toString");

        HTML.Tag[] tags = HTML.getAllTags();
        check(tags != null && tags.length == 74, "getAllTags length 74");
        check(TAG_TABLE.length == 74, "local flag table has 74 rows");

        // full flags table: identity with getTag cache + block/flow/pre flags + toString
        boolean[] seen = new boolean[74];
        for (String row : TAG_TABLE) {
            String[] parts = row.split(":");
            HTML.Tag t = HTML.getTag(parts[0]);
            check(t != null, "getTag(" + parts[0] + ") non-null");
            if (t == null) continue;
            check(parts[0].equals(t.toString()), "toString " + parts[0]);
            boolean blk = parts[1].equals("1"), flow = parts[2].equals("1"), pre = parts[3].equals("1");
            check(t.isBlock() == blk, "isBlock " + parts[0]);
            check(t.breaksFlow() == flow, "breaksFlow " + parts[0]);
            check(t.isPreformatted() == pre, "isPreformatted " + parts[0]);
            int idx = -1;
            for (int i = 0; i < tags.length; i++) if (tags[i] == t) idx = i;
            check(idx >= 0 && !seen[idx], "constant in getAllTags " + parts[0]);
            if (idx >= 0) seen[idx] = true;
            check(HTML.getTag(parts[0]) == t, "getTag cache identity " + parts[0]);
        }
        for (int i = 0; i < 74; i++) check(seen[i], "getAllTags row " + i + " covered");

        // spot identity with the public constants
        check(HTML.getTag("a") == HTML.Tag.A, "getTag(a) == Tag.A");
        check(HTML.getTag("p") == HTML.Tag.P, "getTag(p) == Tag.P");
        check(HTML.getTag("body") == HTML.Tag.BODY, "getTag(body) == Tag.BODY");
        check(HTML.getTag("pre") == HTML.Tag.PRE, "getTag(pre) == Tag.PRE");
        check(HTML.getTag("img") == HTML.Tag.IMG, "getTag(img) == Tag.IMG");
        check(HTML.getTag("table") == HTML.Tag.TABLE, "getTag(table) == Tag.TABLE");

        // lookup is lower-case only; mixed case never matches
        check(HTML.getTag("A") == null, "getTag(A) upper-case null");
        check(HTML.getTag("Body") == null, "getTag(Body) mixed case null");
        check(HTML.getTag("") == null, "getTag(empty) null");
        check(HTML.getTag("zzz-no-such") == null, "getTag(unknown) null");
        boolean npe = false;
        try { HTML.getTag(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getTag(null) NPE");

        // bare HTML.Tag() constructor: unnamed tag
        HTML.Tag plain = new HTML.Tag();
        check(plain.toString() == null, "new Tag() toString null");
        check(!plain.isBlock(), "new Tag() not block");
        check(!plain.breaksFlow(), "new Tag() breaksFlow false");
        check(!plain.isPreformatted(), "new Tag() not preformatted");

        // pseudo tags implied/comment are not in getAllTags flow semantics
        check(!HTML.Tag.IMPLIED.isPreformatted(), "IMPLIED not preformatted");
        check(!HTML.Tag.CONTENT.isBlock(), "CONTENT not block");
        check(!HTML.Tag.COMMENT.breaksFlow(), "COMMENT breaksFlow false");
    }

    // ---- Part 3: UnknownTag value contract ----
    static void testUnknownTag() throws Exception {
        UnknownTag u1 = new UnknownTag("foo");
        UnknownTag u2 = new UnknownTag("foo");
        UnknownTag u3 = new UnknownTag("bar");
        check("foo".equals(u1.toString()), "UnknownTag toString");
        check(u1.equals(u2), "equal names equals");
        check(u1.hashCode() == u2.hashCode(), "equal names same hashCode");
        check(!u1.equals(u3), "different names not equals");
        check(!u1.equals(HTML.Tag.P), "UnknownTag vs predefined tag");
        check(!HTML.Tag.P.equals(u1), "predefined tag vs UnknownTag");
        check(!u1.equals("foo"), "UnknownTag vs String");
        check(!u1.equals(null), "UnknownTag vs null");
        check(!u1.isBlock() && !u1.breaksFlow() && !u1.isPreformatted(),
              "UnknownTag default flags");

        long svu = ObjectStreamClass.lookup(UnknownTag.class).getSerialVersionUID();
        check(svu == -1534369342247250625L, "UnknownTag serialVersionUID stable");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(u1);
        oos.close();
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof UnknownTag, "UnknownTag round-trip class");
        check(u1.equals(back), "UnknownTag round-trip equals");
        check(back.hashCode() == u1.hashCode(), "UnknownTag round-trip hashCode");
    }

    // ---- Part 4: attribute key factory ----
    static void testAttributes() {
        check("href".equals(HTML.Attribute.HREF.toString()), "HREF toString lower-case");

        HTML.Attribute[] keys = HTML.getAllAttributeKeys();
        check(keys != null && keys.length == 81, "getAllAttributeKeys length 81");
        boolean hasHref = false, hasSrc = false, hasTitle = false, hasColspan = false;
        for (HTML.Attribute a : keys) {
            if (a == HTML.Attribute.HREF) hasHref = true;
            if (a == HTML.Attribute.SRC) hasSrc = true;
            if (a == HTML.Attribute.TITLE) hasTitle = true;
            if (a == HTML.Attribute.COLSPAN) hasColspan = true;
        }
        check(hasHref && hasSrc && hasTitle && hasColspan, "key constants present");

        check(HTML.getAttributeKey("href") == HTML.Attribute.HREF, "getAttributeKey(href)");
        check(HTML.getAttributeKey("src") == HTML.Attribute.SRC, "getAttributeKey(src)");
        check(HTML.getAttributeKey("title") == HTML.Attribute.TITLE, "getAttributeKey(title)");
        check(HTML.getAttributeKey("HREF") == null, "getAttributeKey upper-case null");
        check(HTML.getAttributeKey("zzz") == null, "getAttributeKey unknown null");
        boolean npe = false;
        try { HTML.getAttributeKey(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getAttributeKey(null) NPE");

        check("#DEFAULT".equals(HTML.NULL_ATTRIBUTE_VALUE), "NULL_ATTRIBUTE_VALUE");
    }

    // ---- Part 5: integer attribute parsing ----
    static void testIntegerParsing() {
        AttributeSet empty = new SimpleAttributeSet();
        check(HTML.getIntegerAttributeValue(empty, HTML.Attribute.COLSPAN, 5) == 5, "empty set default");

        check(HTML.getIntegerAttributeValue(empty, null, 7) == 7, "null attribute key default");

        boolean npe = false;
        try { HTML.getIntegerAttributeValue(null, HTML.Attribute.COLSPAN, 5); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "null set NPE");

        String[][] cases = {
            {"7", "7"}, {"-3", "-3"}, {"+3", "3"}, {"0", "0"},
            {" 7 ", "9"}, {"7x", "9"}, {"2147483648", "9"}, {"0x10", "9"}, {"", "9"},
        };
        for (String[] c : cases) {
            SimpleAttributeSet s = new SimpleAttributeSet();
            s.addAttribute(HTML.Attribute.COLSPAN, c[0]);
            check(HTML.getIntegerAttributeValue(s, HTML.Attribute.COLSPAN, 9)
                  == Integer.parseInt(c[1]), "parse '" + c[0] + "'");
        }
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testTags();
        testUnknownTag();
        testAttributes();
        testIntegerParsing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
