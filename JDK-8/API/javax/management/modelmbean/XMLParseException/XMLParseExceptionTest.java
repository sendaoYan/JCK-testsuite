/*
 * @test
 * @summary javax.management.modelmbean.XMLParseException conformance: exception hierarchy and construction.
 * @run main XMLParseExceptionTest
 */

public class XMLParseExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.modelmbean.XMLParseException x = new javax.management.modelmbean.XMLParseException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("java.lang.Exception").isAssignableFrom(c), "extends java.lang.Exception");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testConstructorAndMessageFormats();
        testRealTriggersFromDescriptorSupport();
        testSerializationRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): message formats, real XML triggers, serialization ----

    static void testConstructorAndMessageFormats() {
        javax.management.modelmbean.XMLParseException e1 = new javax.management.modelmbean.XMLParseException();
        check("XML Parse Exception.".equals(e1.getMessage()), "no-arg message: [" + e1.getMessage() + "]");
        javax.management.modelmbean.XMLParseException e2 = new javax.management.modelmbean.XMLParseException("m");
        check("XML Parse Exception: m".equals(e2.getMessage()), "String message format");
        javax.management.modelmbean.XMLParseException e3 = new javax.management.modelmbean.XMLParseException(
                new Exception("in"), "m2");
        check(e3.getMessage().startsWith("XML Parse Exception: m2"), "(Exception,String) message prefix");
        check(e3.getMessage().contains("java.lang.Exception: in"), "(Exception,String) message embeds cause");
        // unlike InvalidTargetObjectTypeException, a null cause is NOT tolerated here (e.toString() unguarded)
        try {
            new javax.management.modelmbean.XMLParseException(null, "m3");
            check(false, "null cause must throw NPE");
        } catch (NullPointerException npe) {
            check(true, "null cause -> NullPointerException (contrast with InvalidTargetObjectTypeException)");
        }
        check(javax.management.modelmbean.XMLParseException.class.getConstructors().length == 3,
                "exactly three public constructors");
        check(!java.lang.RuntimeException.class.isAssignableFrom(javax.management.modelmbean.XMLParseException.class),
                "checked exception (not runtime)");
    }

    static void testRealTriggersFromDescriptorSupport() throws Exception {
        // real source 1: XML constructor input without a <Descriptor> pair
        try {
            new javax.management.modelmbean.DescriptorSupport("<bad");
            check(false, "bad XML must throw XMLParseException");
        } catch (javax.management.modelmbean.XMLParseException xe) {
            check(xe.getMessage() != null && xe.getMessage().contains("No <descriptor>"),
                    "missing descriptor pair message: " + xe.getMessage());
        }
        // real source 2: a single plain String literal resolves to the XML constructor
        try {
            new javax.management.modelmbean.DescriptorSupport("plainstring");
            check(false, "plain string through the XML ctor must throw");
        } catch (javax.management.modelmbean.XMLParseException xe) {
            check(true, "single String literal routed to the XML constructor");
        }
        // well-formed XML round-trip does NOT throw
        javax.management.modelmbean.DescriptorSupport ok =
                new javax.management.modelmbean.DescriptorSupport("<Descriptor></Descriptor>");
        check(ok.getFieldNames().length == 0, "well-formed empty XML accepted");
        // declared on the XML constructor signature (checked exception)
        boolean declared = false;
        for (Class<?> ex : javax.management.modelmbean.DescriptorSupport.class
                .getConstructor(String.class).getExceptionTypes())
            if (ex == javax.management.modelmbean.XMLParseException.class) declared = true;
        check(declared, "XMLParseException declared on DescriptorSupport(String)");
    }

    static void testSerializationRoundTrip() throws Exception {
        javax.management.modelmbean.XMLParseException e = new javax.management.modelmbean.XMLParseException("boom");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(e);
        oos.close();
        javax.management.modelmbean.XMLParseException back = (javax.management.modelmbean.XMLParseException)
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(e.getMessage().equals(back.getMessage()), "round-trip preserves message");
        check(back.toString().contains("XMLParseException"), "toString contains class name");
    }
}
