/*
 * @test
 * @summary javax.xml.bind.UnmarshalException conformance: hierarchy, constructors, and real unmarshal failure channel.
 * @run main UnmarshalExceptionTest
 */

public class UnmarshalExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.xml.bind.annotation.XmlRootElement
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Person {
        @javax.xml.bind.annotation.XmlElement String name;
        @javax.xml.bind.annotation.XmlElement int age;
    }

    public static void main(String[] args) throws Exception {
        check(javax.xml.bind.UnmarshalException.class.getSuperclass().getName().equals("javax.xml.bind.JAXBException"), "extends javax.xml.bind.JAXBException");
        testHierarchy();
        testCtors();
        testSync();
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() throws Exception {
        check(javax.xml.bind.JAXBException.class.isAssignableFrom(javax.xml.bind.UnmarshalException.class), "is a JAXBException");
        check(java.lang.Exception.class.isAssignableFrom(javax.xml.bind.UnmarshalException.class), "is an Exception");
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.bind.UnmarshalException.class), "is Serializable");
        check(java.io.ObjectStreamClass.lookup(javax.xml.bind.UnmarshalException.class).getSerialVersionUID() == 8222528164859488039L,
                "serialVersionUID == 8222528164859488039");
        check(javax.xml.bind.UnmarshalException.class.getConstructors().length == 5, "5 public constructors");
    }

    static void testCtors() {
        javax.xml.bind.UnmarshalException e1 = new javax.xml.bind.UnmarshalException("m1");
        check("m1".equals(e1.getMessage()), "(String) message");
        check(e1.getErrorCode() == null, "(String) errorCode null");
        check(e1.getLinkedException() == null, "(String) linked null");
        check(e1.getCause() == null, "(String) cause null");

        javax.xml.bind.UnmarshalException e2 = new javax.xml.bind.UnmarshalException("m2", "code2");
        check("m2".equals(e2.getMessage()), "(String,String) message");
        check("code2".equals(e2.getErrorCode()), "(String,String) errorCode");

        RuntimeException t = new RuntimeException("boom");
        javax.xml.bind.UnmarshalException e3 = new javax.xml.bind.UnmarshalException(t);
        check(e3.getMessage() == null, "(Throwable) message null");
        check(e3.getLinkedException() == t, "(Throwable) linked");
        check(e3.getCause() == t, "(Throwable) cause");

        javax.xml.bind.UnmarshalException e4 = new javax.xml.bind.UnmarshalException("m4", t);
        check("m4".equals(e4.getMessage()), "(String,Throwable) message");
        check(e4.getLinkedException() == t, "(String,Throwable) linked");
        check(e4.getCause() == t, "(String,Throwable) cause");

        javax.xml.bind.UnmarshalException e5 = new javax.xml.bind.UnmarshalException("m5", "code5", t);
        check("m5".equals(e5.getMessage()), "(String,String,Throwable) message");
        check("code5".equals(e5.getErrorCode()), "(String,String,Throwable) errorCode");
        check(e5.getLinkedException() == t, "(String,String,Throwable) linked");
        check(e5.getCause() == t, "(String,String,Throwable) cause");
    }

    static void testSync() {
        RuntimeException t = new RuntimeException("a");
        javax.xml.bind.UnmarshalException e = new javax.xml.bind.UnmarshalException("s", t);
        RuntimeException t2 = new RuntimeException("b");
        e.setLinkedException(t2);
        check(e.getLinkedException() == t2, "setLinkedException readback");
        check(e.getCause() == t2, "getCause() follows setLinkedException (sync)");
        check(e.toString().contains("javax.xml.bind.UnmarshalException"), "toString contains class name");
        try {
            throw new javax.xml.bind.UnmarshalException("poly", "ec");
        } catch (javax.xml.bind.JAXBException ex) {
            check("ec".equals(ex.getErrorCode()), "catchable as JAXBException with errorCode intact");
        }
    }

    // real channel: RI throws UnmarshalException from bad XML with precise cause wiring
    static void testRealChannel() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();

        // malformed XML -> UnmarshalException whose linked/cause is the SAXParseException
        boolean caught = false;
        try {
            u.unmarshal(new java.io.StringReader("<broken"));
        } catch (javax.xml.bind.UnmarshalException ue) {
            caught = true;
            check(ue.getLinkedException() instanceof org.xml.sax.SAXParseException, "malformed XML: linked exception is SAXParseException");
            check(ue.getCause() == ue.getLinkedException(), "malformed XML: cause mirrors linked exception");
            check(ue.getLinkedException().getMessage() != null, "malformed XML: SAX message present in linked exception");
        }
        check(caught, "malformed XML raises UnmarshalException (not raw SAXException)");

        // unexpected root element -> UnmarshalException with NO linked exception (not chained)
        boolean caught2 = false;
        try {
            u.unmarshal(new java.io.StringReader("<notperson/>"));
        } catch (javax.xml.bind.UnmarshalException ue) {
            caught2 = true;
            check(ue.getLinkedException() == null, "root mismatch: linked exception null");
            check(ue.getCause() == null, "root mismatch: cause null");
        }
        check(caught2, "root-name mismatch raises UnmarshalException");

        // good XML still round-trips after all the failures
        Object r = u.unmarshal(new java.io.StringReader("<person><name>alice</name><age>30</age></person>"));
        check(r instanceof Person, "good XML still unmarshals");
        check("alice".equals(((Person) r).name) && ((Person) r).age == 30, "round-trip fields equal");
    }
}
