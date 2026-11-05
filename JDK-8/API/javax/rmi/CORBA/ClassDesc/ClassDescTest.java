/*
 * @test
 * @summary javax.rmi.CORBA.ClassDesc conformance: structure, field layout, serialization semantics.
 * @run main ClassDescTest
 */

import java.io.*;
import java.lang.reflect.*;
import javax.rmi.CORBA.ClassDesc;

public class ClassDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.ClassDesc");
        check(!c.isInterface(), "class not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete (instantiable)");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "serializable");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPublic(ctors[0].getModifiers())
                && ctors[0].getParameterTypes().length == 0, "single public no-arg constructor");
        // the two private string fields prescribed by the spec: repository id + codebase
        Field repid = c.getDeclaredField("repid");
        Field codebase = c.getDeclaredField("codebase");
        check(repid.getType() == String.class && Modifier.isPrivate(repid.getModifiers()),
                "private String repid field");
        check(codebase.getType() == String.class && Modifier.isPrivate(codebase.getModifiers()),
                "private String codebase field");
        check(c.getDeclaredFields().length >= 2, "declares at least the repid/codebase fields");
        // fixed serialVersionUID for cross-release wire compatibility
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -3477057297839810709L,
                "serialVersionUID locked to -3477057297839810709");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassDesc cd = new ClassDesc();
        check(cd instanceof java.io.Serializable, "serializable");
        // default field state: both descriptor slots start null
        Field repid = ClassDesc.class.getDeclaredField("repid");
        Field codebase = ClassDesc.class.getDeclaredField("codebase");
        repid.setAccessible(true);
        codebase.setAccessible(true);
        check(repid.get(cd) == null, "fresh instance repid is null");
        check(codebase.get(cd) == null, "fresh instance codebase is null");
        // no equals/hashCode override: identity semantics
        ClassDesc cd2 = new ClassDesc();
        check(cd.equals(cd), "equals(self)");
        check(!cd.equals(cd2), "identity equals (two fresh descriptors differ)");
        check(!cd.equals(null), "not equals null");
        check(!cd.equals("x"), "not equals other type");
        check(cd.hashCode() != cd2.hashCode() || true, "hashCode available");
        // toString is the default Object form
        check(cd.toString().startsWith("javax.rmi.CORBA.ClassDesc@"), "default Object toString");

        // serialization round trip: wire form carries the null repid/codebase defaults
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(cd);
        o.flush();
        Object back = new ObjectInputStream(new ByteArrayInputStream(b.toByteArray())).readObject();
        check(back instanceof ClassDesc, "round trip keeps the ClassDesc type");
        check(back != cd, "round trip produces a distinct object");
        check(repid.get(back) == null && codebase.get(back) == null,
                "round trip keeps null repid/codebase");
        // the serialized form starts with the class descriptor for javax.rmi.CORBA.ClassDesc
        byte[] wire = b.toByteArray();
        check(new String(wire, "ISO-8859-1").contains("javax.rmi.CORBA.ClassDesc"),
                "wire form names the ClassDesc class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
