/*
 * @test
 * @summary java.util.jar.Attributes.Name conformance: constants/case-insensitive equality.
 * @run main AttributesNameTest
 */

import java.util.jar.Attributes.Name;

public class AttributesNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testName() {
        check("Manifest-Version".equals(Name.MANIFEST_VERSION.toString()), "MANIFEST_VERSION");
        check("Main-Class".equals(Name.MAIN_CLASS.toString()), "MAIN_CLASS");
        check("Class-Path".equals(Name.CLASS_PATH.toString()), "CLASS_PATH");
        Name custom = new Name("Custom-Header");
        check("Custom-Header".equals(custom.toString()), "custom toString");
        check(custom.equals(new Name("custom-header")), "equals case-insensitive");
        check(custom.hashCode() == new Name("custom-header").hashCode(), "hashCode case-insensitive");
    }

    public static void main(String[] args) {
        testName();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
