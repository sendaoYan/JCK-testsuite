/*
 * @test
 * @summary javax.xml.bind.helpers.PrintConversionEventImpl conformance: constructors, inheritance from ValidationEventImpl and interface typing.
 * @run main PrintConversionEventImplTest
 */


public class PrintConversionEventImplTest {
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
        javax.xml.bind.helpers.PrintConversionEventImpl e = new javax.xml.bind.helpers.PrintConversionEventImpl(1, "msg", (javax.xml.bind.ValidationEventLocator) null);
        check(e.getSeverity() == 1, "severity");
        check("msg".equals(e.getMessage()), "message");
        check(e instanceof javax.xml.bind.PrintConversionEvent, "is PrintConversionEvent");
        testHierarchy();
        testCtorMatrix();
        testInheritedMutators();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() {
        check(javax.xml.bind.helpers.PrintConversionEventImpl.class.getSuperclass()
                .getName().equals("javax.xml.bind.helpers.ValidationEventImpl"), "extends ValidationEventImpl");
        check(e1() instanceof javax.xml.bind.ValidationEvent, "is a ValidationEvent");
        String[] ifs = new String[javax.xml.bind.helpers.PrintConversionEventImpl.class.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++)
            ifs[i] = javax.xml.bind.helpers.PrintConversionEventImpl.class.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.bind.PrintConversionEvent"), "directly implements PrintConversionEvent");
        check(javax.xml.bind.helpers.PrintConversionEventImpl.class.getConstructors().length == 2, "2 public constructors");
    }

    static javax.xml.bind.helpers.PrintConversionEventImpl e1() {
        return new javax.xml.bind.helpers.PrintConversionEventImpl(1, "x", null);
    }

    static void testCtorMatrix() {
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        loc.setLineNumber(15);
        javax.xml.bind.helpers.PrintConversionEventImpl e =
                new javax.xml.bind.helpers.PrintConversionEventImpl(1, "pr", loc);
        check(e.getSeverity() == 1, "3-arg ctor severity");
        check("pr".equals(e.getMessage()), "3-arg ctor message");
        check(e.getLocator() == loc, "3-arg ctor locator identity");
        check(e.getLinkedException() == null, "3-arg ctor linked null");

        IllegalStateException t = new IllegalStateException("printer");
        javax.xml.bind.helpers.PrintConversionEventImpl e4 =
                new javax.xml.bind.helpers.PrintConversionEventImpl(1, "pr4", loc, t);
        check(e4.getSeverity() == 1, "4-arg ctor severity");
        check("pr4".equals(e4.getMessage()), "4-arg ctor message");
        check(e4.getLinkedException() == t, "4-arg ctor linked exception");
        check(e4.getLocator() == loc, "4-arg ctor locator");
        check(e4 instanceof javax.xml.bind.PrintConversionEvent, "4-arg instance is PrintConversionEvent");
        check(e4 instanceof javax.xml.bind.ValidationEvent, "4-arg instance is ValidationEvent too");
        javax.xml.bind.helpers.DefaultValidationEventHandler h = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        check(h.handleEvent(e4) == false, "ERROR-typed print event rejected by default handler");
    }

    static void testInheritedMutators() {
        javax.xml.bind.helpers.PrintConversionEventImpl e =
                new javax.xml.bind.helpers.PrintConversionEventImpl(1, "m", null);
        e.setSeverity(0);
        check(e.getSeverity() == 0, "inherited setSeverity");
        e.setMessage("m2");
        check("m2".equals(e.getMessage()), "inherited setMessage");
        RuntimeException t = new RuntimeException("r");
        e.setLinkedException(t);
        check(e.getLinkedException() == t, "inherited setLinkedException");
        javax.xml.bind.helpers.ValidationEventLocatorImpl loc2 =
                new javax.xml.bind.helpers.ValidationEventLocatorImpl();
        e.setLocator(loc2);
        check(e.getLocator() == loc2, "inherited setLocator");
        boolean iae = false;
        try { e.setSeverity(4); } catch (IllegalArgumentException x) { iae = true; }
        check(iae, "inherited severity validation applies");
        check(e.toString().contains("severity=WARNING"), "inherited toString after setSeverity(WARNING)");
    }
}
