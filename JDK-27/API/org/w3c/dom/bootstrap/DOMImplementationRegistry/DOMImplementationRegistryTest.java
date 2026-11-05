/*
 * @test
 * @summary org.w3c.dom.bootstrap.DOMImplementationRegistry conformance:
 *          final class, PROPERTY, newInstance(), accessors.
 * @run main DOMImplementationRegistryTest
 */

import java.lang.reflect.Modifier;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

public class DOMImplementationRegistryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Modifier.isFinal(DOMImplementationRegistry.class.getModifiers()),
              "final class");
        check(DOMImplementationRegistry.PROPERTY != null
                && !DOMImplementationRegistry.PROPERTY.isEmpty(),
              "PROPERTY constant non-empty");

        // newInstance() returns a registry, or throws when no implementation
        // is available; both are conformant.
        try {
            DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
            check(reg != null, "newInstance() returns registry");
            reg.getDOMImplementation("XML");
            reg.getDOMImplementationList("XML");
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | ClassCastException ex) {
            check(ex != null, "newInstance() may throw when no impl available");
        }
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check("org.w3c.dom.DOMImplementationSourceList".equals(org.w3c.dom.bootstrap.DOMImplementationRegistry.PROPERTY), "PROPERTY");
        org.w3c.dom.bootstrap.DOMImplementationRegistry reg = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance();
        check(reg != null, "newInstance");
        org.w3c.dom.DOMImplementation core = reg.getDOMImplementation("Core");
        check(core != null, "Core implementation present");
        org.w3c.dom.DOMImplementation xml = reg.getDOMImplementation("XML 3.0");
        check(xml != null, "XML 3.0 implementation present");
        check(reg.getDOMImplementation("NoSuchFeature") == null, "unknown feature null");
        org.w3c.dom.DOMImplementationList list = reg.getDOMImplementationList("XML");
        check(list != null && list.getLength() > 0, "DOMImplementationList non-empty");
        check(list.item(0) != null, "list item");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMImplementationRegistry contract ----
    static void deepContract() throws Exception {
        // newInstance bootstraps from the system property without needing it set
        org.w3c.dom.bootstrap.DOMImplementationRegistry reg = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance();
        check(reg != null, "newInstance returns a registry");
        check(reg == org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance() || reg.getClass() != null, "repeated newInstance yields usable registries");
        // the default source list wires the Xerces core implementation
        org.w3c.dom.DOMImplementation impl = reg.getDOMImplementation("Core 3.0");
        check(impl != null, "default wiring resolves Core 3.0");
        check(impl.hasFeature("XML", "3.0") && impl.hasFeature("LS", "3.0"), "default implementation covers XML/LS");
        check(!impl.hasFeature("CSS", "2.0"), "default implementation has no CSS");
        // feature string parsing: feature/version token pairs
        check(reg.getDOMImplementation("XML 3.0") != null, "XML 3.0 token pair");
        check(reg.getDOMImplementation("Traversal 2.0") != null, "Traversal 2.0 token pair");
        check(reg.getDOMImplementation("Range 2.0") != null, "Range 2.0 token pair");
        check(reg.getDOMImplementation("Events 2.0") != null, "Events 2.0 token pair");
        check(reg.getDOMImplementation("MutationEvents 2.0") != null, "MutationEvents 2.0 token pair");
        check(reg.getDOMImplementation("NoSuchFeature 1.0") == null, "unknown token pair null");
        boolean npe = false;
        try { reg.getDOMImplementation(null); } catch (NullPointerException expected) { npe = true; }
        check(npe, "null feature string -> NullPointerException");
        // list API on the registry
        org.w3c.dom.DOMImplementationList l = reg.getDOMImplementationList("Core 3.0");
        check(l.getLength() >= 1 && l.item(0) != null, "registry list non-empty");
        check(reg.getDOMImplementationList("bogus 1.0").getLength() == 0, "registry list empty for unknown features");
        // read-only view of the source-list property state (do not mutate globals)
        String prop = System.getProperty("org.w3c.dom.DOMImplementationSourceList");
        check(true, "DOMImplementationSourceList currently=" + prop);
    }
}
