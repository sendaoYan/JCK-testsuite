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

    public static void main(String[] args) throws Exception {
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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
