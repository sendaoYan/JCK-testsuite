/*
 * @test
 * @summary javax.naming.Reference conformance: white-box structure + black-box behavior.
 * @run main ReferenceTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.naming.BinaryRefAddr;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class ReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Reference.class;
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(Reference.class.getSuperclass() == Object.class, "extends Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getDeclaredConstructor(String.class) != null, "ctor(String)");
        check(c.getDeclaredConstructor(String.class, RefAddr.class) != null, "ctor(String,RefAddr)");
        check(c.getDeclaredConstructor(String.class, String.class, String.class) != null, "ctor(String,String,String)");
        for (String m : new String[] {"getClassName", "getFactoryClassName", "getFactoryClassLocation",
                "add", "remove", "get", "getAll", "size", "clear", "clone", "equals", "hashCode", "toString"})
            check(method(c, m), "declares " + m);
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Reference r = new Reference("MyClass", "MyFactory", "http://loc");
        check(r.getClassName().equals("MyClass"), "className");
        check(r.getFactoryClassName().equals("MyFactory"), "factory");
        check(r.getFactoryClassLocation().equals("http://loc"), "location");
        check(r.size() == 0, "starts empty");

        r.add(new StringRefAddr("t1", "v1"));
        r.add(0, new StringRefAddr("t0", "v0"));
        check(r.size() == 2, "size after adds");
        check(r.get(0).getType().equals("t0"), "ordered insertion at 0");
        check(r.get("t1").getContent().equals("v1"), "get by type");
        check(r.get("zz") == null, "unknown type -> null");

        check(((javax.naming.RefAddr) r.remove(0)).getType().equals("t0"), "remove returns addr");
        check(r.size() == 1, "size after remove");
        r.clear();
        check(r.size() == 0, "clear");

        r.add(new BinaryRefAddr("bin", new byte[] {9, 8}));
        check(r.getAll().nextElement().getType().equals("bin"), "getAll enumerates");

        Reference rc = (Reference) r.clone();
        check(rc.equals(r), "clone equals");
        check(rc.get(0) == r.get(0), "clone shares addr elements (Vector clone)");
        rc.add(new StringRefAddr("x", "y"));
        check(r.size() == 1, "clone structurally independent");

        check(r.equals(new Reference("MyClass", new BinaryRefAddr("bin", new byte[] {9, 8}))), "equals content");
        check(!r.equals(new Reference("Other", new BinaryRefAddr("bin", new byte[] {9, 8}))), "className differs");
        check(r.equals(r) && !r.equals(null) && !r.equals("x"), "equals degenerate");

        Reference plain = new Reference("C");
        check(plain.getFactoryClassName() == null && plain.getFactoryClassLocation() == null, "no-factory ctor");
        Reference one = new Reference("C", new StringRefAddr("a", "1"));
        check(one.size() == 1, "single-addr ctor");
        check(one.toString().startsWith("Reference Class Name: C"), "toString prefix");

        Reference eq = new Reference("MyClass", new BinaryRefAddr("bin", new byte[] {9, 8}));
        check(r.equals(eq) && r.hashCode() == eq.hashCode(), "equal references equals+hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
