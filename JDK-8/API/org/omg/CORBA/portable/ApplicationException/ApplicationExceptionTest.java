/*
 * @test
 * @summary org.omg.CORBA.portable.ApplicationException conformance: concrete class behavior.
 * @run main ApplicationExceptionTest
 */

public class ApplicationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.portable.ApplicationException ae = new org.omg.CORBA.portable.ApplicationException("IDL:test:1.0", null);
        check("IDL:test:1.0".equals(ae.getId()), "getId");
        check(ae.getInputStream() == null, "getInputStream null");
        check(ae instanceof java.lang.Exception, "extends Exception");

        // ---- deepened: real CDR stream, stable reference, payload readable ----
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        os.write_string("payload");
        org.omg.CORBA.portable.ApplicationException withStream =
            new org.omg.CORBA.portable.ApplicationException("EXID", os.create_input_stream());
        check("EXID".equals(withStream.getId()), "real-stream ctor id");
        check(withStream.getInputStream() != null, "getInputStream non-null");
        check(withStream.getInputStream() == withStream.getInputStream(),
              "getInputStream returns the same instance each call");
        check("payload".equals(withStream.getInputStream().read_string()),
              "carried stream is positioned and readable");
        check(withStream.getMessage() == null, "message stays null (id lives only in getId)");
        check(!java.lang.reflect.Modifier.isFinal(org.omg.CORBA.portable.ApplicationException.class.getModifiers()),
              "ApplicationException is NOT final (jdk8u372, subclassable)");
        check(org.omg.CORBA.portable.ApplicationException.class.getSuperclass() == java.lang.Exception.class,
              "direct Exception subclass");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
