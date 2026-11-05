/*
 * @test
 * @summary org.omg.CORBA.UserException conformance: abstract class shape.
 * @run main UserExceptionTest
 */

public class UserExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.UserException");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(java.lang.Exception.class.isAssignableFrom(c), "extends java.lang.Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "implements org.omg.CORBA.portable.IDLEntity");

        // ---- deepened: subclass channel (abstract base, java.lang.Exception ctors inherited) ----
        org.omg.CORBA.UserException anon = new org.omg.CORBA.UserException() { };
        check(anon.getMessage() == null, "no-arg anonymous subclass message null");
        org.omg.CORBA.UserException named = new org.omg.CORBA.UserException("m") { };
        check("m".equals(named.getMessage()), "(String) subclass message readback");
        check(anon instanceof java.lang.Exception, "UserException is a checked Exception");
        check(anon.toString().contains("UserException"), "toString names the hierarchy");
        check(org.omg.CORBA.UserException.class.getDeclaredConstructors().length == 2,
              "base declares its own () and (String) constructors");
        for (java.lang.reflect.Constructor<?> ct : org.omg.CORBA.UserException.class.getDeclaredConstructors())
            check(java.lang.reflect.Modifier.isProtected(ct.getModifiers()),
                  "UserException base ctor " + ct + " is protected");
        check(org.omg.CORBA.WrongTransaction.class.getSuperclass() == org.omg.CORBA.UserException.class,
              "WrongTransaction directly extends UserException");
        check(org.omg.CORBA.Bounds.class.getSuperclass() == org.omg.CORBA.UserException.class,
              "Bounds directly extends UserException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
