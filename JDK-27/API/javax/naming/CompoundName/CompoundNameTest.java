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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
