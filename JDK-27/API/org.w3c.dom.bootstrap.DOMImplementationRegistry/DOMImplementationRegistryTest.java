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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
