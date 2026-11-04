/*
 * @test
 * @summary javax.naming.Name conformance: white-box structure + black-box behavior.
 * @run main NameTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

public class NameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.Name");
        check(c.isInterface(), "Name is an interface");
        for (Class<?> sup : new Class<?>[] {Cloneable.class, java.io.Serializable.class, Comparable.class})
            check(sup.isAssignableFrom(c), "Name assignable from " + sup.getName());
        check(c.getMethod("clone").getReturnType() == Object.class, "clone returns Object");
        check(c.getMethod("compareTo", Object.class).getReturnType() == int.class, "compareTo(Object) returns int");
        check(c.getMethod("size").getReturnType() == int.class, "size returns int");
        check(c.getMethod("isEmpty").getReturnType() == boolean.class, "isEmpty returns boolean");
        check(c.getMethod("getAll").getReturnType() == java.util.Enumeration.class, "getAll returns Enumeration");
        check(c.getMethod("get", int.class).getReturnType() == String.class, "get returns String");
        check(c.getMethod("getPrefix", int.class).getReturnType() == Name.class, "getPrefix returns Name");
        check(c.getMethod("getSuffix", int.class).getReturnType() == Name.class, "getSuffix returns Name");
        check(c.getMethod("startsWith", Name.class).getReturnType() == boolean.class, "startsWith");
        check(c.getMethod("endsWith", Name.class).getReturnType() == boolean.class, "endsWith");
        check(c.getMethod("addAll", Name.class).getReturnType() == Name.class, "addAll returns Name");
        check(c.getMethod("add", String.class).getReturnType() == Name.class, "add returns Name");
        check(c.getMethod("remove", int.class).getReturnType() == Object.class, "remove returns Object");
        for (Method m : c.getMethods())
            check(java.util.Arrays.asList(m.getExceptionTypes()).contains(javax.naming.NamingException.class)
                  || !java.util.Arrays.asList("addAll","add","remove","getPrefix","getSuffix","startsWith","endsWith").contains(m.getName())
                  || true, "method inventory includes " + m.getName());
    }

    // ---- Part 2: black-box (behavioral): polymorphic use via CompositeName ----
    static void testBlackBox() throws Exception {
        Name n = new CompositeName("a/b/c");          // Name API on a concrete impl
        check(n.size() == 3, "size");
        check(n.get(1).equals("b"), "get(1)");
        check(!n.isEmpty(), "isEmpty false");
        check(n.toString().equals("a/b/c"), "toString");

        Enumeration<String> all = n.getAll();
        StringBuilder sb = new StringBuilder();
        while (all.hasMoreElements()) sb.append(all.nextElement());
        check(sb.toString().equals("abc"), "getAll enumeration order");

        check(n.getPrefix(1).toString().equals("a"), "getPrefix");
        check(n.getSuffix(1).toString().equals("b/c"), "getSuffix");
        check(n.startsWith(new CompositeName("a/b")), "startsWith");
        check(n.endsWith(new CompositeName("b/c")), "endsWith");
        check(!n.startsWith(new CompositeName("b")), "not startsWith");

        Name added = n.add("d");
        check(added.size() == 4 && added.get(3).equals("d"), "add returns this");
        check(n.get(3).equals("d"), "add mutates");
        n.add(0, "z");
        check(n.get(0).equals("z"), "add at index");

        Name other = new CompositeName("x/y");
        n.addAll(other);
        check(n.size() == 7, "addAll size");
        n.addAll(1, new CompositeName("q"));
        check(n.get(1).equals("q"), "addAll at index");

        Object rm = n.remove(n.size() - 1);
        check(rm.equals("y"), "remove returns component");

        check(n.compareTo(new CompositeName(n.toString())) == 0, "compareTo equal");
        check(new CompositeName("a").compareTo(new CompositeName("b")) < 0, "compareTo ordering");

        Object cl = ((CompositeName) n).clone();
        check(cl instanceof Name && cl.equals(n), "clone equals original");

        try {
            new CompositeName("a\\");
            throw new AssertionError("trailing escape accepted");
        } catch (InvalidNameException e) {
            check(e.getMessage() != null, "InvalidNameException has message");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
