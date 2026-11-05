/*
 * @test
 * @summary javax.naming.Name conformance: interface hierarchy and members,
 *          plus stable serialVersionUID.
 * @run main NameTest
 */

import javax.naming.Name;
import javax.naming.CompositeName;
import javax.naming.CompoundName;

public class NameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.naming.Name");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        expIfs.add("java.lang.Cloneable");
        expIfs.add("java.lang.Comparable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("add"), "has method add");
        check(names.contains("addAll"), "has method addAll");
        check(names.contains("endsWith"), "has method endsWith");
    }

    static void testSerialVersionUID() {
        check(Name.serialVersionUID == -3617482732056931635L,
                "stable serialVersionUID");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testSerialVersionUID();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dNameInterfaceMatrix();
        dNameCloneContract();
        dNameCompareTo();
        System.out.println("deep: Name interface contracts OK");
    }

    static java.util.Properties dNSyntax() {
        java.util.Properties p = new java.util.Properties();
        p.setProperty("jndi.syntax.direction", "left_to_right");
        p.setProperty("jndi.syntax.separator", ".");
        p.setProperty("jndi.syntax.escape", "\\");
        return p;
    }

    static void dNameInterfaceMatrix() throws Exception {
        Name composite = new CompositeName("a/b");
        Name compound = new CompoundName("x.y", dNSyntax());
        check(composite.size() == 2 && compound.size() == 2, "sizes");
        check(!composite.equals(compound) && !compound.equals(composite),
                "composite and compound names never equal");
        check(composite.add("z") == composite, "add returns this name");
        check(composite.size() == 3, "add appended");
        check("z".equals(composite.remove(2)), "remove returns removed component");
        java.util.List<String> comps = new java.util.ArrayList<>();
        java.util.Enumeration<String> en = compound.getAll();
        while (en.hasMoreElements()) comps.add(en.nextElement());
        check(comps.size() == 2 && "x".equals(comps.get(0)), "getAll order preserved");
        check(composite.isEmpty() == false && new CompositeName().isEmpty(), "isEmpty");
        check("b".equals(composite.get(1)), "get by index");
        check(composite.getPrefix(1).toString().equals("a"), "getPrefix");
        check(composite.getSuffix(1).toString().equals("b"), "getSuffix");
        check(composite.startsWith(new CompositeName("a")), "startsWith");
        check(composite.endsWith(new CompositeName("b")), "endsWith");
        Name empty = new CompositeName();
        check(empty.startsWith(new CompositeName()) && empty.endsWith(new CompositeName()),
                "empty name starts/ends with empty");
        // addAll appends and returns this
        Name target = new CompositeName("a");
        check(target.addAll(new CompositeName("b/c")) == target, "addAll returns this");
        check(target.size() == 3, "addAll appended components");
        check(target.addAll(0, new CompositeName("z")) == target, "addAll(int,Name) returns this");
        check("z".equals(target.get(0)), "addAll at index");
    }

    static void dNameCloneContract() throws Exception {
        CompositeName c = new CompositeName("a/b");
        Name cClone = (Name) c.clone();
        check(cClone.equals(c), "clone equal");
        c.remove(0);
        check(cClone.size() == 2, "composite clone independent");
        CompoundName d = new CompoundName("x.y", dNSyntax());
        Name dClone = (Name) d.clone();
        d.add("mut");
        check(dClone.size() == 2, "compound clone independent");
        check(dClone.toString().equals("x.y"), "compound clone keeps syntax");
    }

    static void dNameCompareTo() throws Exception {
        Name a = new CompositeName("a/b");
        Name b = new CompositeName("a/c");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0, "ordering both directions");
        boolean cce = false;
        try { a.compareTo("a/b"); } catch (ClassCastException e) { cce = true; }
        check(cce, "compareTo non-Name -> ClassCastException");
        cce = false;
        try { new CompoundName("a", dNSyntax()).compareTo(new CompositeName("a")); }
        catch (ClassCastException e) { cce = true; }
        check(cce, "compound compareTo composite -> ClassCastException");
        check(a.compareTo(new CompositeName("a/b")) == 0, "equal compare zero");
        Name shorter = new CompositeName("a");
        check(shorter.compareTo(a) < 0, "shorter name compares less");
    }

}
