/*
 * @test
 * @summary javax.xml.stream.XMLStreamException conformance: exception hierarchy and constructors.
 * @run main XMLStreamExceptionTest
 */


public class XMLStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.stream.XMLStreamException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        javax.xml.stream.XMLStreamException xe = new javax.xml.stream.XMLStreamException("msg");
        check(xe.getNestedException() == null, "nested exception null");
        check(xe.getLocation() == null, "location null");
        testConstructorChain();
        testLocationDecoration();
        testFromParser();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: all constructor overloads ----
    static void testConstructorChain() {
        javax.xml.stream.Location loc = fixedLocation();
        javax.xml.stream.XMLStreamException e1 = new javax.xml.stream.XMLStreamException("m1", loc);
        check(e1.getMessage() != null && e1.getMessage().contains("m1"), "(String,Location) message decorated with location");
        check(e1.getLocation() == loc, "(String,Location) location identity");
        check(e1.getNestedException() == null, "(String,Location) no nested");
        java.lang.RuntimeException cause = new java.lang.RuntimeException("why");
        javax.xml.stream.XMLStreamException e2 = new javax.xml.stream.XMLStreamException("m2", loc, cause);
        check(e2.getLocation() == loc, "(String,Location,Throwable) location identity");
        check(e2.getNestedException() == cause, "(String,Location,Throwable) nested exception");
        // JDK-8 quirk: this overload does NOT wire getCause() (JDK-27 does) - locked per release
        check(e2.getCause() == null, "JDK-8: (String,Location,Throwable) leaves getCause() null");
        javax.xml.stream.XMLStreamException e3 = new javax.xml.stream.XMLStreamException("m3", cause);
        check(e3.getNestedException() == cause && e3.getLocation() == null, "(String,Throwable) nested set, no location");
        check("m3".equals(e3.getMessage()), "(String,Throwable) message kept");
        javax.xml.stream.XMLStreamException e4 = new javax.xml.stream.XMLStreamException(cause);
        check(e4.getNestedException() == cause, "(Throwable) nested set");
        check(e4.getMessage() != null && e4.getMessage().contains("why"), "(Throwable) message derived from cause toString");
        javax.xml.stream.XMLStreamException e5 = new javax.xml.stream.XMLStreamException();
        check(e5.getMessage() == null && e5.getLocation() == null && e5.getNestedException() == null,
            "no-arg ctor all null");
        check(e5 instanceof java.lang.Exception, "is Exception");
        // Throwable chaining through initCause still possible
        javax.xml.stream.XMLStreamException e6 = new javax.xml.stream.XMLStreamException("m6");
        e6.initCause(cause);
        check(e6.getCause() == cause, "initCause works when not set in ctor");
    }

    // ---- Deepened 2: message decoration with Location ----
    static void testLocationDecoration() {
        javax.xml.stream.Location loc = fixedLocation();
        javax.xml.stream.XMLStreamException e = new javax.xml.stream.XMLStreamException("boom", loc);
        String m = e.getMessage();
        check(m.contains("boom") && m.contains("4") && m.contains("5"),
            "message decorated with row,col from Location, got [" + m.replace("\n", "\\n") + "]");
        check(m.startsWith("ParseError"), "decoration prefix ParseError");
        // Location values surface through getLocation
        check(e.getLocation().getLineNumber() == 4 && e.getLocation().getColumnNumber() == 5
            && e.getLocation().getCharacterOffset() == 6 && "P".equals(e.getLocation().getPublicId())
            && "S".equals(e.getLocation().getSystemId()), "location getters");
    }

    // ---- Deepened 3: exceptions raised by the real parser ----
    static void testFromParser() throws Exception {
        try {
            javax.xml.stream.XMLStreamReader r = javax.xml.stream.XMLInputFactory.newFactory()
                .createXMLStreamReader(new java.io.StringReader("<a><b></a>"));
            while (r.hasNext()) r.next();
            check(false, "malformed doc must throw");
        } catch (javax.xml.stream.XMLStreamException e) {
            check(e.getLocation() != null && e.getLocation().getLineNumber() == 1,
                "parser-raised exception carries line 1 location");
            check(e.getMessage() != null && e.getMessage().length() > 0, "parser-raised exception has message");
        }
    }

    static javax.xml.stream.Location fixedLocation() {
        return new javax.xml.stream.Location() {
            public int getLineNumber() { return 4; }
            public int getColumnNumber() { return 5; }
            public int getCharacterOffset() { return 6; }
            public String getPublicId() { return "P"; }
            public String getSystemId() { return "S"; }
            public String toString() { return "Fixed"; }
        };
    }
}
