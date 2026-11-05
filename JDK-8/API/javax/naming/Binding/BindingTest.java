/*
 * @test
 * @summary javax.naming.Binding conformance: full contract coverage.
 *          all four constructors, getClassName (explicit vs derived from the
 *          object's runtime class), getObject/setObject, isRelative, exact
 *          toString format (name: class:object), null object/class handling,
 *          and serialization round-trip.
 * @run main BindingTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.naming.Binding;

public class BindingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Binding roundTrip(Binding b) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(b); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Binding) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        Binding b = new Binding("cn=foo", "value");
        check("cn=foo".equals(b.getName()), "getName");
        check("java.lang.String".equals(b.getClassName()), "getClassName");
        check("value".equals(b.getObject()), "getObject");
        b.setObject("other");
        check("other".equals(b.getObject()), "setObject");
        Binding b2 = new Binding("n", "java.lang.Integer", Integer.valueOf(3), true);
        check(b2.isRelative(), "isRelative");
        check("java.lang.Integer".equals(b2.getClassName()), "getClassName with class");
    }

    static void testConstructors() {
        Binding b1 = new Binding("n", "obj");
        check("n".equals(b1.getName()) && "obj".equals(b1.getObject()), "ctor(name, object)");
        check("java.lang.String".equals(b1.getClassName()), "ctor(name, object) derives class");

        Binding b2 = new Binding("n", "obj", false);
        check(!b2.isRelative(), "ctor(name, object, relative)");

        Binding b3 = new Binding("n", "java.lang.Integer", Integer.valueOf(3));
        check("java.lang.Integer".equals(b3.getClassName()), "ctor(name, className, object)");

        Binding b4 = new Binding("n", "java.lang.Integer", Integer.valueOf(3), true);
        check(b4.isRelative(), "ctor(name, className, object, relative)");
    }

    static void testGetClassName() {
        Binding b = new Binding("n", (Object) null);
        check(b.getClassName() == null, "null object -> null className");
        Binding b2 = new Binding("n", (String) null, "obj");
        check("java.lang.String".equals(b2.getClassName()), "null className -> derived from object");
    }

    static void testToString() {
        Binding b = new Binding("n", "java.lang.Integer", Integer.valueOf(3));
        check("n: java.lang.Integer:3".equals(b.toString()), "toString name:class:object");
        Binding nullObj = new Binding("n", (Object) null);
        check("n: null:null".equals(nullObj.toString()), "toString null object");
    }

    static void testSerialization() throws Exception {
        Binding b = new Binding("n", "java.lang.Integer", Integer.valueOf(3), true);
        Binding back = roundTrip(b);
        check("n".equals(back.getName()) && "java.lang.Integer".equals(back.getClassName()), "round trip fields");
        check(Integer.valueOf(3).equals(back.getObject()), "round trip object");
        check(back.isRelative(), "round trip relative");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testConstructors();
        testGetClassName();
        testToString();
        testSerialization();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dBindConstructorMatrix();
        dBindClassDerivation();
        dBindSetters();
        dBindToString();
        dBindSerialization();
        System.out.println("deep: Binding contracts OK");
    }

    static void dBindConstructorMatrix() {
        Binding b1 = new Binding("n", "v");
        check("n".equals(b1.getName()) && "v".equals(b1.getObject()), "(name, obj)");
        check("java.lang.String".equals(b1.getClassName()), "class derived from object");
        check(b1.isRelative(), "relative by default");
        Binding b2 = new Binding("n", "v", false);
        check(!b2.isRelative(), "relative flag false");
        Binding b3 = new Binding("n", "my.Clazz", "v");
        check("my.Clazz".equals(b3.getClassName()), "explicit class name");
        Binding b4 = new Binding("n", "my.Clazz", "v", false);
        check(!b4.isRelative() && "my.Clazz".equals(b4.getClassName()), "4-arg ctor");
        Binding bNull = new Binding("n", null);
        check(bNull.getObject() == null && bNull.getClassName() == null,
                "null object -> null class at construction");
    }

    static void dBindClassDerivation() {
        Binding b = new Binding("n", null);
        b.setObject("later");
        check("java.lang.String".equals(b.getClassName()), "class derived lazily after setObject");
        Binding prim = new Binding("n", 42);
        check("java.lang.Integer".equals(prim.getClassName()), "wrapper class derived");
        Binding typed = new Binding("n", "java.util.Date", null);
        check("java.util.Date".equals(typed.getClassName()), "explicit class survives null object");
        typed.setObject("x");
        check("java.util.Date".equals(typed.getClassName()), "explicit class not recomputed");
    }

    static void dBindSetters() {
        Binding b = new Binding("n", "v");
        b.setName("renamed");
        check("renamed".equals(b.getName()), "setName");
        b.setClassName("c.N");
        check("c.N".equals(b.getClassName()), "setClassName");
        b.setObject("w");
        check("w".equals(b.getObject()), "setObject");
        b.setRelative(false);
        check(!b.isRelative(), "setRelative");
        b.setNameInNamespace("full/ns");
        check("full/ns".equals(b.getNameInNamespace()), "setNameInNamespace inherited");
    }

    static void dBindToString() {
        Binding b = new Binding("nm", "v");
        check(b.toString().contains("nm"), "toString contains name");
        Binding notRel = new Binding("nm", "v", false);
        check(notRel.toString().contains("(not relative)"), "toString marks non-relative");
        Binding rel = new Binding("nm", "v", true);
        check(!rel.toString().contains("(not relative)"), "toString plain when relative");
    }

    static void dBindSerialization() throws Exception {
        Binding b = new Binding("nm", "my.Cls", "obj", false);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(b); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            Binding back = (Binding) oi.readObject();
            check("nm".equals(back.getName()) && "my.Cls".equals(back.getClassName()),
                    "name and class survive");
            check("obj".equals(back.getObject()) && !back.isRelative(), "object and relative flag survive");
        }
    }

}
