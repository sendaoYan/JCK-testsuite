/*
 * @test
 * @summary JLS 7.5.5 (Example 7.5.5-3): importing multiple modules can make a
 *          simple name ambiguous; List is in both java.util (java.base) and
 *          java.awt (java.desktop).
 * @compile/fail SingleModuleImportAmbiguous.java
 */

import module java.base;
import module java.desktop;

class SingleModuleImportAmbiguous {
    List l = null;   // error: reference to List is ambiguous
}
