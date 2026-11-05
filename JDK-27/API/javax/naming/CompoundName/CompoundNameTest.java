/*
 * @test
 * @summary javax.naming.CompoundName conformance: white-box structure + black-box behavior.
 * @run main CompoundNameTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;
import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.CompositeName;

public class CompoundNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Properties lr(String sep) {
        Properties p = new Properties();
        p.put("jndi.syntax.direction", "left_to_right");
        p.put("jndi.syntax.separator", sep);
        return p;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = CompoundName.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Name.class.isAssignableFrom(c), "implements Name");
        Constructor<?> ctor = c.getDeclaredConstructor(String.class, Properties.class);
        check(ctor.getExceptionTypes()[0] == InvalidNameException.class, "throws InvalidNameException");
        for (String m : new String[] {"toString", "equals", "hashCode", "clone", "compareTo",
                "size", "isEmpty", "getAll", "get", "getPrefix", "getSuffix", "startsWith",
                "endsWith", "addAll", "add", "remove"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) { found = true; break; }
            check(found, "has " + m);
        }
        check(c.getMethod("size").getReturnType() == int.class, "size returns int");
        check(c.getMethod("get", int.class).getReturnType() == String.class, "get returns String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // flat syntax: no direction -> single component, separator ignored
        Properties flat = new Properties();
        flat.put("jndi.syntax.separator", ".");
        CompoundName fn = new CompoundName("a.b.c", flat);
        check(fn.size() == 1 && fn.get(0).equals("a.b.c"), "flat single component");
        try { fn.add("w"); throw new AssertionError("flat add"); }
        catch (InvalidNameException e) { check(e.getMessage().equals("A flat name can only have a single component"), "flat add msg"); }

        // right-to-left ordering
        Properties rtl = lr(".");
        rtl.put("jndi.syntax.direction", "right_to_left");
        CompoundName rn = new CompoundName("a.b.c", rtl);
        check(rn.get(0).equals("c") && rn.get(2).equals("a"), "rtl get");
        check(rn.toString().equals("a.b.c"), "rtl toString round trip");

        // left-to-right with escape
        Properties esc = lr("/");
        esc.put("jndi.syntax.escape", "\\");
        CompoundName en = new CompoundName("a\\/b/c", esc);
        check(en.size() == 2 && en.get(0).equals("a/b"), "escaped separator");

        // beginquote/endquote protect a separator inside a component
        Properties quo = lr("/");
        quo.put("jndi.syntax.beginquote", "\"");
        CompoundName qn = new CompoundName("\"a/b\"/c", quo);
        check(qn.size() == 2 && qn.get(0).equals("a/b"), "quoted comp");
        check(qn.toString().equals("\"a/b\"/c"), "toString requotes");
        try { new CompoundName("\"ab/c", quo); throw new AssertionError("unterminated quote"); }
        catch (InvalidNameException e) { check(e.getMessage().equals("\"ab/c: no close quote"), "unterminated msg"); }

        // structural ops
        Properties p = lr(".");
        CompoundName n = new CompoundName("a.b.c", p);
        check(n.size() == 3 && !n.isEmpty(), "size");
        check(n.getPrefix(1).toString().equals("a"), "prefix");
        check(n.getSuffix(1).toString().equals("b.c"), "suffix");
        check(n.startsWith(new CompoundName("a.b", p)), "startsWith");
        check(n.endsWith(new CompoundName("b.c", p)), "endsWith");
        n.add("d");
        check(n.size() == 4 && n.get(3).equals("d"), "add appends");
        n.add(0, "z");
        check(n.get(0).equals("z"), "add at index");
        check(n.remove(0).equals("z"), "remove");
        n.addAll(new CompoundName("e.f", p));
        check(n.size() == 6, "addAll");
        n.addAll(1, new CompoundName("g", p));
        check(n.get(1).equals("g"), "addAll at index");

        // equality includes syntax; clone independent
        check(new CompoundName("a.b", p).equals(new CompoundName("a.b", p)), "equals same syntax");
        check(new CompoundName("a.b", p).hashCode() == new CompoundName("a.b", p).hashCode(), "hashCode");
        Properties rtl2 = lr(".");
        rtl2.put("jndi.syntax.direction", "right_to_left");
        check(!new CompoundName("a.b", p).equals(new CompoundName("a.b", rtl2)), "different syntax not equal");
        CompoundName cl = (CompoundName) n.clone();
        check(cl.equals(n), "clone equals");
        cl.remove(0);
        check(n.size() == 7, "clone independent");
        check(new CompoundName("a.b", p).compareTo(new CompoundName("a.b", p)) == 0, "compareTo equal");
        check(new CompoundName("a", p).compareTo(new CompoundName("a.b", p)) < 0, "compareTo shorter first");

        // invalid: escape at end of component
        try { new CompoundName("a\\", esc); throw new AssertionError("trailing escape"); }
        catch (InvalidNameException e) { check(true, "InvalidNameException thrown"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dCmpDirectionMatrix();
        dCmpSeparators();
        dCmpEscape();
        dCmpQuotes();
        dCmpIgnoreCaseAndTrim();
        dCmpNegativeSyntax();
        dCmpMutationAndBounds();
        dCmpRoundTripAndSerialization();
        System.out.println("deep: CompoundName contracts OK");
    }

    static java.util.Properties dProps(String dir, String sep) {
        java.util.Properties p = new java.util.Properties();
        p.setProperty("jndi.syntax.direction", dir);
        if (sep != null) p.setProperty("jndi.syntax.separator", sep);
        return p;
    }

    static java.util.Properties dDot() {
        java.util.Properties p = dProps("left_to_right", ".");
        p.setProperty("jndi.syntax.escape", "\\");
        return p;
    }

    static void dCmpDirectionMatrix() throws Exception {
        CompoundName ltr = new CompoundName("a.b.c", dDot());
        check(ltr.size() == 3 && "a".equals(ltr.get(0)) && "c".equals(ltr.get(2)), "left_to_right order");
        java.util.Properties rtl = dProps("right_to_left", ".");
        CompoundName r = new CompoundName("a.b.c", rtl);
        check(r.size() == 3 && "c".equals(r.get(0)) && "a".equals(r.get(2)), "right_to_left order");
        check("a.b.c".equals(r.toString()), "right_to_left toString canonical");
        check("c".equals(r.getPrefix(1).toString()), "rtl prefix is rightmost");
        check("a.b".equals(r.getSuffix(1).toString()), "rtl suffix is leftmost part");
        java.util.Properties flat = dProps("flat", null);
        CompoundName f = new CompoundName("a.b.c", flat);
        check(f.size() == 1 && "a.b.c".equals(f.get(0)), "flat keeps separators inside single comp");
        CompoundName f2 = new CompoundName("", flat);
        check(f2.isEmpty(), "flat empty");
        // default direction when property missing is flat
        java.util.Properties noDir = new java.util.Properties();
        CompoundName nd = new CompoundName("x.y", noDir);
        check(nd.size() == 1, "missing direction defaults to flat");
    }

    static void dCmpSeparators() throws Exception {
        java.util.Properties eq = dProps("left_to_right", "=");
        CompoundName n = new CompoundName("a=b=c", eq);
        check(n.size() == 3 && "a".equals(n.get(0)) && "c".equals(n.get(2)), "separator '='");
        java.util.Properties comma = dProps("left_to_right", ",");
        CompoundName c = new CompoundName("x,y", comma);
        check(c.size() == 2 && "x".equals(c.get(0)), "separator ','");
        // multi-char separator
        java.util.Properties mc = dProps("left_to_right", "::");
        CompoundName m = new CompoundName("a::b", mc);
        check(m.size() == 2 && "a".equals(m.get(0)), "multi-char separator");
        // second separator
        java.util.Properties two = dProps("left_to_right", ".");
        two.setProperty("jndi.syntax.separator2", "/");
        CompoundName t = new CompoundName("a.b/c.d", two);
        check(t.size() == 4, "separator2 also splits");
        check("a.b.c.d".equals(t.toString()), "toString normalizes to the primary separator");
        // leading / trailing / adjacent separators
        CompoundName lead = new CompoundName(".a", dDot());
        check(lead.size() == 2 && "".equals(lead.get(0)), "leading separator -> empty first comp");
        CompoundName trail = new CompoundName("a.", dDot());
        check(trail.size() == 2 && "".equals(trail.get(1)), "trailing separator -> empty last comp");
        CompoundName adj = new CompoundName("a..b", dDot());
        check(adj.size() == 3 && "".equals(adj.get(1)), "adjacent separators -> empty middle comp");
    }

    static void dCmpEscape() throws Exception {
        java.util.Properties p = dDot();
        CompoundName esc = new CompoundName("a\\.b.c", p);
        check(esc.size() == 2 && "a.b".equals(esc.get(0)), "escaped separator stays in comp");
        check("a\\.b.c".equals(esc.toString()), "toString re-escapes");
        check(new CompoundName(esc.toString(), p).equals(esc), "escape round trip");
        CompoundName nonMeta = new CompoundName("a\\xb.c", p);
        check(nonMeta.get(0).equals("a\\xb"), "escape before non-meta kept literal");
        CompoundName doubleEsc = new CompoundName("a\\\\.b", p);
        check(doubleEsc.get(0).equals("a\\"), "double escape -> single backslash");
        boolean ine = false;
        try { new CompoundName("a\\", p); } catch (InvalidNameException e) { ine = true; }
        check(ine, "trailing escape -> InvalidNameException");
        // escaped escape round trip
        check(new CompoundName(doubleEsc.toString(), p).equals(doubleEsc), "double escape round trip");
    }

    static void dCmpQuotes() throws Exception {
        java.util.Properties p = dDot();
        p.setProperty("jndi.syntax.beginquote", "\"");
        CompoundName quoted = new CompoundName("\"a.b\".c", p);
        check(quoted.size() == 2 && "a.b".equals(quoted.get(0)), "quoted comp keeps separator");
        check("\"a.b\".c".equals(quoted.toString()), "toString re-quotes");
        check(new CompoundName(quoted.toString(), p).equals(quoted), "quote round trip");
        java.util.Properties p2 = dDot();
        p2.setProperty("jndi.syntax.beginquote", "<");
        p2.setProperty("jndi.syntax.endquote", ">");
        CompoundName angled = new CompoundName("<a.b>.c", p2);
        check(angled.size() == 2 && "a.b".equals(angled.get(0)), "distinct begin/end quote");
        java.util.Properties p3 = dDot();
        p3.setProperty("jndi.syntax.beginquote", "\"");
        p3.setProperty("jndi.syntax.beginquote2", "'");
        CompoundName alt = new CompoundName("'x.y'.z", p3);
        check(alt.size() == 2 && "x.y".equals(alt.get(0)), "beginquote2 recognized");
        CompoundName first = new CompoundName("\"x.y\".z", p3);
        check(first.size() == 2 && "x.y".equals(first.get(0)), "primary quote still recognized");
        CompoundName escapedQuote = new CompoundName("\"a\\\"b\".c", p);
        check(escapedQuote.get(0).equals("a\"b"), "escaped end-quote inside quoted comp");
        boolean ine = false;
        try { new CompoundName("\"a\"\"b\".c", p); } catch (InvalidNameException e) { ine = true; }
        check(ine, "close quote before end of component -> InvalidNameException");
        // typeval + quoted value form
        java.util.Properties tv = dProps("right_to_left", ",");
        tv.setProperty("jndi.syntax.separator.typeval", "=");
        tv.setProperty("jndi.syntax.beginquote", "\"");
        tv.setProperty("jndi.syntax.escape", "\\");
        CompoundName rdn = new CompoundName("cn=\"a,b\"", tv);
        check(rdn.size() == 1 && "cn=\"a,b\"".equals(rdn.get(0)), "typeval followed by quoted value");
    }

    static void dCmpIgnoreCaseAndTrim() throws Exception {
        java.util.Properties ic = dProps("left_to_right", ".");
        ic.setProperty("jndi.syntax.ignorecase", "true");
        CompoundName upper = new CompoundName("AbC.dEf", ic);
        check("AbC".equals(upper.get(0)), "get keeps original case");
        check(upper.equals(new CompoundName("abc.def", ic)), "ignorecase equals");
        check(new CompoundName("abc.def", ic).equals(upper), "ignorecase equals symmetric");
        check(upper.compareTo(new CompoundName("abc.def", ic)) == 0, "ignorecase compareTo");
        check(upper.hashCode() == new CompoundName("abc.def", ic).hashCode(), "ignorecase hashCode");
        java.util.Properties cs = dProps("left_to_right", ".");
        check(!new CompoundName("AbC", cs).equals(new CompoundName("abc", cs)), "case significant by default");
        java.util.Properties tb = dProps("left_to_right", ".");
        tb.setProperty("jndi.syntax.trimblanks", "true");
        CompoundName padded = new CompoundName(" a . b ", tb);
        check(padded.equals(new CompoundName("a.b", tb)), "trimblanks equal ignores blanks");
        check(padded.compareTo(new CompoundName("a.b", tb)) == 0, "trimblanks compareTo");
        check(" a ".equals(padded.get(0)), "trimblanks only affects comparison, get keeps blanks");
        java.util.Properties ntb = dProps("left_to_right", ".");
        check(!new CompoundName(" a ", ntb).equals(new CompoundName("a", ntb)), "blanks significant without flag");
        // ignorecase parsing follows Boolean.valueOf: case-insensitive "TRUE" honored
        java.util.Properties bogus = dProps("left_to_right", ".");
        bogus.setProperty("jndi.syntax.ignorecase", "TRUE");
        check(new CompoundName("A", bogus).equals(new CompoundName("a", bogus)),
                "ignorecase=TRUE honored (Boolean.valueOf parsing)");
    }

    static void dCmpNegativeSyntax() throws Exception {
        boolean iae = false;
        try {
            java.util.Properties bad = new java.util.Properties();
            bad.setProperty("jndi.syntax.direction", "left_to_right");
            new CompoundName("a", bad);
        } catch (IllegalArgumentException e) {
            iae = true;
            check(e.getMessage() != null && e.getMessage().contains("jndi.syntax.separator"),
                    "missing separator message names property");
        }
        check(iae, "non-flat without separator -> IllegalArgumentException");
        iae = false;
        try {
            java.util.Properties badDir = dProps("sideways", ".");
            new CompoundName("a", badDir);
        } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "invalid direction -> IllegalArgumentException");
        boolean npe = false;
        try { new CompoundName(null, dDot()); } catch (NullPointerException e) { npe = true; }
        check(npe, "null name -> NullPointerException");
        npe = false;
        try { new CompoundName("a", null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null syntax -> NullPointerException");
    }

    static void dCmpMutationAndBounds() throws Exception {
        CompoundName n = new CompoundName("a.c", dDot());
        check(n.add("b") == n, "add returns this");
        CompoundName withB = new CompoundName("a.c", dDot());
        withB.add(1, "b");
        check("a.b.c".equals(withB.toString()), "insert in middle");
        check("b".equals(withB.remove(1)), "remove returns removed comp");
        check("a.c".equals(withB.toString()), "back to original");
        CompoundName addAll = new CompoundName("a", dDot());
        check(addAll.addAll(new CompoundName("b.c", dDot())) == addAll, "addAll returns this");
        check("a.b.c".equals(addAll.toString()), "addAll appends");
        CompoundName at = new CompoundName("a.c", dDot());
        at.addAll(1, new CompoundName("b", dDot()));
        check("a.b.c".equals(at.toString()), "addAll at index");
        boolean aioobe = false;
        try { new CompoundName("a", dDot()).getPrefix(5); } catch (ArrayIndexOutOfBoundsException e) { aioobe = true; }
        check(aioobe, "getPrefix beyond size -> AIOOBE");
        aioobe = false;
        try { new CompoundName("a", dDot()).getPrefix(-1); } catch (ArrayIndexOutOfBoundsException e) { aioobe = true; }
        check(aioobe, "negative prefix -> AIOOBE");
        aioobe = false;
        try { new CompoundName("a", dDot()).remove(9); } catch (ArrayIndexOutOfBoundsException e) { aioobe = true; }
        check(aioobe, "remove out of range -> AIOOBE");
        aioobe = false;
        try { new CompoundName("a", dDot()).add(9, "x"); } catch (ArrayIndexOutOfBoundsException e) { aioobe = true; }
        check(aioobe, "add out of range -> AIOOBE");
        // different-syntax Properties are allowed for addAll (syntax of suffix not checked today)
        java.util.Properties other = dProps("left_to_right", "/");
        CompoundName mixed = new CompoundName("a.b", dDot());
        mixed.addAll(new CompoundName("c/d", other));
        check(mixed.size() == 4, "addAll with other syntax accepted (documented impl note)");
    }

    static void dCmpRoundTripAndSerialization() throws Exception {
        java.util.Properties p = dDot();
        CompoundName n = new CompoundName("a\\.b.c", p);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(n); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            CompoundName back = (CompoundName) oi.readObject();
            check(back.equals(n), "serialization round trip");
            check("a.b".equals(back.get(0)), "escaped comp survives");
            check(back.toString().equals("a\\.b.c"), "toString style survives serialization");
        }
        CompoundName clone = (CompoundName) n.clone();
        clone.add("extra");
        check(n.size() == 2 && clone.size() == 3, "clone independent");
        CompoundName rtl = new CompoundName("a.b", dProps("right_to_left", "."));
        CompoundName same = new CompoundName("a.b", dProps("right_to_left", "."));
        check(rtl.equals(same), "rtl equals");
        check(rtl.hashCode() == same.hashCode(), "rtl hashCode");
        check(!rtl.equals(new CompoundName("a.b", dDot())), "different syntax not equal");
        CompoundName cnAsName = n;
        check(!cnAsName.equals(new CompositeName("a/b")), "CompoundName != CompositeName");
        check(!new CompositeName("a").startsWith(rtl), "CompositeName.startsWith(CompoundName) false");
        check(!rtl.startsWith(new CompositeName("a")), "CompoundName.startsWith(CompositeName) false");
    }

}
