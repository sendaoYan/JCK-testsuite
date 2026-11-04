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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
