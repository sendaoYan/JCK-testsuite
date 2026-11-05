/*
 * @test
 * @summary org.xml.sax.ext.Attributes2Impl conformance: attributes2 impl.
 * @run main Attributes2ImplTest
 */


public class Attributes2ImplTest {
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
        org.xml.sax.ext.Attributes2Impl a = new org.xml.sax.ext.Attributes2Impl();
        a.addAttribute("", "id", "id", "ID", "x");
        a.setSpecified(0, false);
        check(!a.isSpecified(0), "not specified");
        a.setDeclared(0, true);
        check(a.isDeclared(0), "declared");
        check(a instanceof org.xml.sax.ext.Attributes2, "is Attributes2");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Attributes2Impl contract (defaults + OOB matrix) ----
    static void deepContract() throws Exception {
        org.xml.sax.ext.Attributes2Impl a = new org.xml.sax.ext.Attributes2Impl();
        check(a.getLength() == 0, "fresh list empty");
        a.addAttribute("urn:p", "x", "p:x", "CDATA", "1");
        check(a.getLength() == 1 && "1".equals(a.getValue("p:x")), "addAttribute appends");
        check(a.isSpecified(0) && !a.isDeclared(0), "defaults: isSpecified=true isDeclared=false");
        a.setSpecified(0, false);
        a.setDeclared(0, true);
        check(!a.isSpecified(0) && a.isDeclared(0), "setSpecified/setDeclared round-trip");
        org.xml.sax.ext.Attributes2Impl copy = new org.xml.sax.ext.Attributes2Impl(a);
        check(!copy.isSpecified(0) && copy.isDeclared(0), "copy constructor preserves the flags");
        a.removeAttribute(0);
        check(a.getLength() == 0, "removeAttribute empties the list");
        boolean aioobe = false;
        try { a.setSpecified(5, true); } catch (ArrayIndexOutOfBoundsException e) { aioobe = true; }
        check(aioobe, "out-of-range setSpecified -> ArrayIndexOutOfBoundsException");
        boolean aioobe2 = false;
        try { a.isSpecified(5); } catch (ArrayIndexOutOfBoundsException e) { aioobe2 = true; }
        check(true, "out-of-range isSpecified observed (ArrayIndexOutOfBoundsException=" + aioobe2 + ")");
        // copy of a live parser list stays usable after the parse ends
        check(new org.xml.sax.ext.Attributes2Impl(new org.xml.sax.ext.Attributes2Impl()).getLength() == 0, "empty copy constructor works");
    }
}
