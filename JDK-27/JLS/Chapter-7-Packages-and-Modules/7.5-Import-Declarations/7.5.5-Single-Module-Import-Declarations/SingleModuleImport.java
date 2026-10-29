/*
 * @test
 * @summary JLS 7.5.5: a single-module-import declaration imports, on demand, all
 *          public top level classes and interfaces in the packages exported by
 *          a named module; it can be used in a source file that is not itself
 *          part of a module.
 * @run main SingleModuleImport
 */

import module java.xml;

public class SingleModuleImport {
    public static void main(String[] args) {
        // DocumentBuilderFactory is exported by module java.xml (package
        // javax.xml.parsers); the simple name is available via the import.
        javax.xml.parsers.DocumentBuilderFactory f =
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
        if (f == null) throw new AssertionError("DocumentBuilderFactory");

        System.out.println("PASS");
    }
}
