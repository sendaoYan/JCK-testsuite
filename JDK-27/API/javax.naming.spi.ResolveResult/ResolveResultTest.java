/*
 * @test
 * @summary javax.naming.spi.ResolveResult conformance: white-box structure + black-box behavior.
 * @run main ResolveResultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.Serializable;
import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.spi.ResolveResult;

public class ResolveResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.spi.ResolveResult");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean serializable = false;
        for (Class<?> itf : c.getInterfaces()) if (itf == Serializable.class) serializable = true;
        check(serializable, "implements java.io.Serializable");
        check(hasMethod(c, "getRemainingName"), "getRemainingName");
        check(hasMethod(c, "getResolvedObj"), "getResolvedObj");
        check(hasMethod(c, "setRemainingName"), "setRemainingName");
        check(hasMethod(c, "appendRemainingName"), "appendRemainingName");
        check(hasMethod(c, "appendRemainingComponent"), "appendRemainingComponent");
        check(hasMethod(c, "setResolvedObj"), "setResolvedObj");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor (Object, String) parses the remaining name as a CompositeName.
        ResolveResult r = new ResolveResult("obj", "a/b");
        check("obj".equals(r.getResolvedObj()), "string ctor resolved object");
        Name remaining = r.getRemainingName();
        check(remaining != null, "string ctor remaining name non-null");
        check(remaining instanceof CompositeName, "string ctor remaining is CompositeName");
        check(remaining.size() == 2, "CompositeName has two components");
        check("a".equals(remaining.get(0)), "first component");
        check("b".equals(remaining.get(1)), "second component");
        check("a/b".equals(remaining.toString()), "remaining toString");

        // Constructor (Object, Name) clones the supplied name.
        CompositeName supplied = new CompositeName("x/y");
        ResolveResult r2 = new ResolveResult("robj", supplied);
        check("robj".equals(r2.getResolvedObj()), "name ctor resolved object");
        check(r2.getRemainingName() != supplied, "name ctor clones the name");
        check("x/y".equals(r2.getRemainingName().toString()), "cloned name content");
        supplied.add("z"); // mutate original; result must be unaffected
        check("x/y".equals(r2.getRemainingName().toString()), "clone isolates from original mutation");

        // setRemainingName replaces (and clones) the remaining name; null clears it.
        r2.setRemainingName(new CompositeName("p"));
        check("p".equals(r2.getRemainingName().toString()), "setRemainingName replaces");
        r2.setRemainingName(null);
        check(r2.getRemainingName() == null, "setRemainingName(null) clears");

        // appendRemainingName adds components to the end.
        ResolveResult r3 = new ResolveResult("o", "a");
        r3.appendRemainingName(new CompositeName("b/c"));
        check("a/b/c".equals(r3.getRemainingName().toString()), "appendRemainingName appends");

        // appendRemainingComponent adds a single component.
        r3.appendRemainingComponent("d");
        check("a/b/c/d".equals(r3.getRemainingName().toString()), "appendRemainingComponent appends");

        // appendRemainingComponent(null) is a no-op.
        r3.appendRemainingComponent(null);
        check("a/b/c/d".equals(r3.getRemainingName().toString()), "appendRemainingComponent(null) no-op");

        // setResolvedObj replaces the resolved object.
        r3.setResolvedObj("changed");
        check("changed".equals(r3.getResolvedObj()), "setResolvedObj replaces");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
