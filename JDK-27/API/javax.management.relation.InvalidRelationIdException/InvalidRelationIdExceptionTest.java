/*
 * @test
 * @summary javax.management.relation.InvalidRelationIdException conformance: white-box structure + black-box behavior.
 * @run main InvalidRelationIdExceptionTest
 */
public class InvalidRelationIdExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.InvalidRelationIdException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "class must be public");
        check(!c.isInterface(), "must be a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "must be concrete (not abstract)");
        check(c.getSuperclass() == Class.forName("javax.management.relation.RelationException"),
              "must extend javax.management.relation.RelationException");
        check(Exception.class.isAssignableFrom(c), "must be an Exception");

        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "must expose exactly 2 public constructors");
        boolean noArg = false, strArg = false;
        for (java.lang.reflect.Constructor<?> ct : ctors) {
            Class<?>[] pt = ct.getParameterTypes();
            if (pt.length == 0) noArg = true;
            else if (pt.length == 1 && pt[0] == String.class) strArg = true;
        }
        check(noArg, "must have a no-arg constructor");
        check(strArg, "must have a (String) constructor");

        java.lang.reflect.Field suid = c.getDeclaredField("serialVersionUID");
        check(suid.getType() == long.class, "serialVersionUID must be long");
        check(java.lang.reflect.Modifier.isStatic(suid.getModifiers()) &&
              java.lang.reflect.Modifier.isFinal(suid.getModifiers()) &&
              java.lang.reflect.Modifier.isPrivate(suid.getModifiers()),
              "serialVersionUID must be private static final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.InvalidRelationIdException");

        // default constructor: message must be null
        Throwable e1 = (Throwable) c.getDeclaredConstructor().newInstance();
        check(e1.getMessage() == null, "default constructor must leave message null");

        // (String) constructor: message preserved verbatim
        Throwable e2 = (Throwable) c.getConstructor(String.class).newInstance("boom-42");
        check("boom-42".equals(e2.getMessage()), "(String) constructor must preserve message");
        check(e2 instanceof javax.management.relation.RelationException, "chain: instanceof RelationException");
        check(e2 instanceof javax.management.JMException, "chain: instanceof JMException");
        check(e2 instanceof Exception, "chain: instanceof Exception");

        // the checked exception keeps no stack-trace-dependent state; toString contains the class name
        check(e2.toString().equals("javax.management.relation.InvalidRelationIdException: boom-42"), "toString() must be <fqcn>: <message>");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
