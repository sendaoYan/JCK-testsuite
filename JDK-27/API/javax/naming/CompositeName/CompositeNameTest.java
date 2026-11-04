/*
 * @test
 * @summary javax.naming.CompositeName conformance: white-box structure + black-box behavior.
 * @run main CompositeNameTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

public class CompositeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = CompositeName.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Name.class.isAssignableFrom(c), "implements Name");
        check(SerializableOK(c), "Serializable");
        check(c.getDeclaredConstructor(String.class).getExceptionTypes()[0] == InvalidNameException.class,
                "String ctor throws InvalidNameException");
        for (String m : new String[] {"toString", "equals", "hashCode", "compareTo", "clone",
                "size", "isEmpty", "getAll", "get", "getPrefix", "getSuffix", "startsWith",
                "endsWith", "addAll", "add", "remove"})
            check(hasMethod(c, m), "declares/overrides " + m);
    }
    static boolean SerializableOK(Class<?> c) { return java.io.Serializable.class.isAssignableFrom(c); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // parsing: separators, escapes, quotes
        CompositeName empty = new CompositeName("");
        check(empty.size() == 0 && empty.isEmpty() && empty.toString().isEmpty(), "empty parse");
        check(new CompositeName().size() == 0, "default ctor empty");

        CompositeName esc = new CompositeName("a\\/b/c");
        check(esc.size() == 2, "escaped separator kept in one comp");
        check(esc.get(0).equals("a/b"), "unescaped value");
        check(esc.toString().equals("\"a/b\"/c"), "toString re-quotes");

        CompositeName q = new CompositeName("\"a.b\"/c");
        check(q.size() == 2 && q.get(0).equals("a.b"), "quoted comp keeps separator chars");

        try { new CompositeName("a\\"); throw new AssertionError("trailing escape"); }
        catch (InvalidNameException e) { check(e.getMessage().equals("a\\: unescaped \\ at end of component"), "exact msg"); }
        try { new CompositeName("\"x"); throw new AssertionError("unterminated quote"); }
        catch (InvalidNameException e) { check(e.getMessage().equals("\"x: no close quote"), "exact msg"); }

        // mutation chains
        CompositeName n = new CompositeName("a/b/c");
        check(n.add("d") == n, "add returns this");
        check(n.size() == 4, "size after add");
        n.add(1, "X");
        check(n.get(1).equals("X") && n.size() == 5, "insert at index");
        check(n.remove(1).equals("X"), "remove returns comp");
        n.addAll(new CompositeName("e/f"));
        check(n.size() == 6 && n.get(5).equals("f"), "addAll append");
        n.addAll(0, new CompositeName("s"));
        check(n.get(0).equals("s"), "addAll at 0");

        // prefixes / suffixes / match
        CompositeName m = new CompositeName("a/b/c");
        check(m.getPrefix(0).isEmpty(), "empty prefix");
        check(m.getPrefix(3).toString().equals("a/b/c"), "full prefix");
        check(m.getSuffix(0).toString().equals("a/b/c"), "full suffix");
        check(m.getSuffix(3).isEmpty(), "empty suffix");
        check(m.startsWith(new CompositeName()) && m.endsWith(new CompositeName()), "empty name matches");
        check(!m.startsWith(new CompositeName("c")), "wrong head");

        // equals/hashCode/compareTo/clone
        CompositeName m2 = new CompositeName("a/b/c");
        check(m.equals(m2) && m2.equals(m), "equals symmetric");
        check(m.hashCode() == m2.hashCode(), "hashCode contract");
        check(!m.equals(new CompositeName("a/b")), "different size not equal");
        check(m.compareTo(m2) == 0, "compareTo 0");
        check(new CompositeName("a/b").compareTo(new CompositeName("a/c")) < 0, "lexicographic");
        check(new CompositeName("a").compareTo(new CompositeName("a/b")) < 0, "prefix sorts first");
        try { m.compareTo("a/b/c"); throw new AssertionError("compareTo(String)"); }
        catch (ClassCastException e) { check(e.getMessage().equals("Not a CompositeName"), "CCE msg"); }

        CompositeName cl = (CompositeName) m.clone();
        check(cl.equals(m) && cl != m, "clone equals, distinct");
        cl.add("z");
        check(m.size() == 3, "clone is independent");

        // serialization round trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(m); }
        Object rt = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(rt.equals(m), "serialization round trip");

        Enumeration<String> en = m.getAll();
        int cnt = 0;
        while (en.hasMoreElements()) { en.nextElement(); cnt++; }
        check(cnt == 3, "getAll count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
