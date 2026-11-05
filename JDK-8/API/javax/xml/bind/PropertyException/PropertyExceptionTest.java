/*
 * @test
 * @summary javax.xml.bind.PropertyException conformance: hierarchy, constructors and real property-name failure channel.
 * @run main PropertyExceptionTest
 */

public class PropertyExceptionTest {
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
        check(javax.xml.bind.PropertyException.class.getSuperclass().getName().equals("javax.xml.bind.JAXBException"), "extends javax.xml.bind.JAXBException");
        testHierarchy();
        testCtors();
        testNameValueCtor();
        testRealChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() throws Exception {
        check(javax.xml.bind.JAXBException.class.isAssignableFrom(javax.xml.bind.PropertyException.class), "is a JAXBException");
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.bind.PropertyException.class), "is Serializable");
        check(java.io.ObjectStreamClass.lookup(javax.xml.bind.PropertyException.class).getSerialVersionUID() == 4091278398208222475L,
                "serialVersionUID == 4091278398208222475");
        check(javax.xml.bind.PropertyException.class.getConstructors().length == 6, "6 public constructors (extra (name,value) form)");
    }

    static void testCtors() {
        javax.xml.bind.PropertyException e1 = new javax.xml.bind.PropertyException("m1");
        check("m1".equals(e1.getMessage()), "(String) message");
        check(e1.getErrorCode() == null, "(String) errorCode null");
        check(e1.getLinkedException() == null, "(String) linked null");

        javax.xml.bind.PropertyException e2 = new javax.xml.bind.PropertyException("m2", "code2");
        check("m2".equals(e2.getMessage()), "(String,String) message");
        check("code2".equals(e2.getErrorCode()), "(String,String) errorCode");

        RuntimeException t = new RuntimeException("boom");
        javax.xml.bind.PropertyException e3 = new javax.xml.bind.PropertyException(t);
        check(e3.getLinkedException() == t, "(Throwable) linked");
        check(e3.getCause() == t, "(Throwable) cause");

        javax.xml.bind.PropertyException e4 = new javax.xml.bind.PropertyException("m4", t);
        check("m4".equals(e4.getMessage()), "(String,Throwable) message");
        check(e4.getLinkedException() == t, "(String,Throwable) linked");
        check(e4.getCause() == t, "(String,Throwable) cause");

        javax.xml.bind.PropertyException e5 = new javax.xml.bind.PropertyException("m5", "code5", t);
        check("m5".equals(e5.getMessage()), "(String,String,Throwable) message");
        check("code5".equals(e5.getErrorCode()), "(String,String,Throwable) errorCode");
        check(e5.getLinkedException() == t, "(String,String,Throwable) linked");

        javax.xml.bind.PropertyException e6 = new javax.xml.bind.PropertyException("s", t);
        RuntimeException t2 = new RuntimeException("b");
        e6.setLinkedException(t2);
        check(e6.getLinkedException() == t2 && e6.getCause() == t2, "setLinkedException keeps cause in sync");
    }

    // (name, value) convenience constructor: message format locked by probe
    static void testNameValueCtor() {
        javax.xml.bind.PropertyException e = new javax.xml.bind.PropertyException("propX", (Object) "valX");
        check("name: propX value: valX".equals(e.getMessage()), "(name,value) message == \"name: propX value: valX\"");
        check(e.getErrorCode() == null, "(name,value) errorCode null");
        check(e.getLinkedException() == null, "(name,value) linked null");
        javax.xml.bind.PropertyException e2 = new javax.xml.bind.PropertyException("p2", (Object) Integer.valueOf(7));
        check(e2.getMessage().contains("p2") && e2.getMessage().contains("7"), "(name,value) works with non-String value");
    }

    // real channel: RI throws PropertyException from unknown property names
    static void testRealChannel() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Marshaller m = ctx.createMarshaller();
        boolean caughtSet = false;
        try {
            m.setProperty("bogus.property", "v");
        } catch (javax.xml.bind.PropertyException pe) {
            caughtSet = true;
            check(pe.getMessage() != null, "marshaller setProperty(bogus) message present");
        }
        check(caughtSet, "Marshaller.setProperty(unknown) throws PropertyException");

        boolean caughtGet = false;
        try {
            m.getProperty("bogus.property");
        } catch (javax.xml.bind.PropertyException pe) {
            caughtGet = true;
        }
        check(caughtGet, "Marshaller.getProperty(unknown) throws PropertyException");

        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
        boolean caughtU = false;
        try {
            u.setProperty("bogus.property", "v");
        } catch (javax.xml.bind.PropertyException pe) {
            caughtU = true;
        }
        check(caughtU, "Unmarshaller.setProperty(unknown) throws PropertyException");

        boolean caughtUG = false;
        try {
            u.getProperty("bogus.property");
        } catch (javax.xml.bind.PropertyException pe) {
            caughtUG = true;
        }
        check(caughtUG, "Unmarshaller.getProperty(unknown) throws PropertyException");
    }
}
