/*
 * @test
 * @summary org.omg.PortableServer.ServantLocatorPackage.CookieHolder conformance: holder value field.
 * @run main CookieHolderTest
 */

public class CookieHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableServer.ServantLocatorPackage.CookieHolder h0 = new org.omg.PortableServer.ServantLocatorPackage.CookieHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.Object v = null;
        org.omg.PortableServer.ServantLocatorPackage.CookieHolder h1 = new org.omg.PortableServer.ServantLocatorPackage.CookieHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened (jdk8u372-b07 probed): Streamable face / NO_IMPLEMENT stubs (unimplemented in JDK-8) ----
        final Class<?> chc = org.omg.PortableServer.ServantLocatorPackage.CookieHolder.class;
        check(java.lang.reflect.Modifier.isFinal(chc.getModifiers()), "final class");
        check(org.omg.CORBA.portable.Streamable.class.isAssignableFrom(chc), "implements Streamable");
        check(chc.getDeclaredConstructors().length == 2, "exactly 2 ctors: () and (Object)");
        java.lang.Object cookie = new java.lang.Object();
        org.omg.PortableServer.ServantLocatorPackage.CookieHolder ch =
            new org.omg.PortableServer.ServantLocatorPackage.CookieHolder(cookie);
        check(ch.value == cookie, "(Object) ctor stores by reference");
        ch.value = null;
        check(ch.value == null, "value field public mutable, nullable");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        try {
            ch._type();
            check(false, "_type() must throw NO_IMPLEMENT on this JDK");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(expected.minor == 0, "_type() -> NO_IMPLEMENT minor==0 (unimplemented Streamable stub)");
        }
        try {
            ch._write(orb.create_output_stream());
            check(false, "_write() must throw NO_IMPLEMENT on this JDK");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(expected.minor == 0, "_write(os) -> NO_IMPLEMENT minor==0");
        }
        try {
            ch._read(orb.create_output_stream().create_input_stream());
            check(false, "_read() must throw NO_IMPLEMENT on this JDK");
        } catch (org.omg.CORBA.NO_IMPLEMENT expected) {
            check(expected.minor == 0, "_read(is) -> NO_IMPLEMENT minor==0");
        }
        check(ch.value == null, "failed Streamable calls leave value untouched");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
