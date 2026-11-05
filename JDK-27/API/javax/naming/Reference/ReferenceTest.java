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
import javax.naming.Name;
import java.util.Vector;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dRefConstructorMatrix();
        dRefAddrListOps();
        dRefEquality();
        dRefCloneClear();
        dRefSerialization();
        System.out.println("deep: Reference contracts OK");
    }

    static void dRefConstructorMatrix() {
        Reference r = new Reference("MyClass");
        check("MyClass".equals(r.getClassName()), "className");
        check(r.getFactoryClassName() == null, "no factory by default");
        check(r.getFactoryClassLocation() == null, "no factory location by default");
        check(r.size() == 0 && !r.getAll().hasMoreElements(), "no addrs by default");
        Reference r2 = new Reference("C", new StringRefAddr("t", "v"));
        check(r2.size() == 1 && "t".equals(r2.get(0).getType()), "initial addr");
        Reference r3 = new Reference("C", "Factory", "file:/loc");
        check("Factory".equals(r3.getFactoryClassName()) && "file:/loc".equals(r3.getFactoryClassLocation()),
                "factory ctor");
        Reference r4 = new Reference("C", new StringRefAddr("t", "v"), "Factory", "file:/loc");
        check(r4.size() == 1 && "Factory".equals(r4.getFactoryClassName()), "4-arg ctor");
        boolean ok = true;
        try { new Reference((String) null); } catch (NullPointerException e) { ok = false; }
        check(ok || new Reference((String) null).getClassName() == null,
                "null class name recorded (no requirement of rejection)");
    }

    static void dRefAddrListOps() {
        Reference r = new Reference("C");
        r.add(new StringRefAddr("a", "1"));
        r.add(new StringRefAddr("b", "2"));
        r.add(0, new StringRefAddr("zero", "z"));
        check("zero".equals(r.get(0).getType()), "insert at head");
        check(r.size() == 3, "size after insert");
        java.util.List<String> types = new java.util.ArrayList<>();
        java.util.Enumeration<RefAddr> en = r.getAll();
        while (en.hasMoreElements()) types.add(en.nextElement().getType());
        check(types.equals(java.util.Arrays.asList("zero", "a", "b")), "getAll order");
        check("1".equals(r.get("a").getContent()), "get by type");
        check(r.get("missing") == null, "get by unknown type null");
        r.add(new StringRefAddr("a", "1b"));
        check("1".equals(r.get("a").getContent()), "get by type returns first match");
        Object removed = r.remove(0);
        check("zero".equals(((StringRefAddr) removed).getType()), "remove returns addr");
        boolean oob = false;
        try { r.remove(99); } catch (ArrayIndexOutOfBoundsException e) { oob = true; }
        check(oob, "remove out of range -> AIOOBE");
        oob = false;
        try { r.get(99); } catch (ArrayIndexOutOfBoundsException e) { oob = true; }
        check(oob, "get out of range -> AIOOBE");
        r.clear();
        check(r.size() == 0, "clear empties");
        r.add(null);
        check(r.size() == 1 && r.get(0) == null, "null addr storable (recorded behavior)");
    }

    static void dRefEquality() {
        Reference a = new Reference("C", new StringRefAddr("t", "1"), "F", "L");
        Reference b = new Reference("C", new StringRefAddr("t", "1"), "F", "L");
        check(a.equals(b), "equal references");
        check(a.hashCode() == b.hashCode(), "hashCode contract");
        check(a.equals(new Reference("C", new StringRefAddr("t", "1"), "OtherF", "L")),
                "equals ignores factory information");
        check(a.equals(new Reference("C", new StringRefAddr("t", "1"), "F", "OtherL")),
                "equals ignores factory location");
        check(!a.equals(new Reference("Other", new StringRefAddr("t", "1"), "F", "L")),
                "class name participates");
        check(!a.equals(new Reference("C", new StringRefAddr("t", "2"), "F", "L")),
                "address content participates");
        check(!a.equals(null) && !a.equals("C"), "null/other type not equal");
    }

    static void dRefCloneClear() {
        Reference r = new Reference("C", new StringRefAddr("t", "1"));
        Reference clone = (Reference) r.clone();
        check(clone.equals(r), "clone equal");
        clone.add(new StringRefAddr("u", "2"));
        check(r.size() == 1 && clone.size() == 2, "clone list independent");
        check(clone.get(0) == r.get(0) || clone.get(0).equals(r.get(0)), "addr shared or equal (shallow clone)");
        String text = r.toString();
        check(text.contains("C") && text.contains("t"), "toString mentions class and addr type");
    }

    static void dRefSerialization() throws Exception {
        Reference r = new Reference("C", new StringRefAddr("t", "1"), "F", "file:/L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(r); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            Reference back = (Reference) oi.readObject();
            check(back.equals(r), "round trip equal");
            check("C".equals(back.getClassName()) && "F".equals(back.getFactoryClassName())
                    && "file:/L".equals(back.getFactoryClassLocation()), "class/factory survive");
            check("1".equals(back.get("t").getContent()), "addr content survives");
        }
    }

}
