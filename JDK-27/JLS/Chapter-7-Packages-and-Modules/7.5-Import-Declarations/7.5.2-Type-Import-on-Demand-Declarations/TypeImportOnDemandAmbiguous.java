/*
 * @test
 * @summary JLS 7.5.2: a simple name that is imported on demand from two different
 *          packages is ambiguous and results in a compile-time error.
 * @compile/fail TypeImportOnDemandAmbiguous.java
 */

import java.util.*;
import java.awt.*;

class TypeImportOnDemandAmbiguous {
    void m() {
        List l = null;   // error: List is ambiguous (java.util.List vs java.awt.List)
    }
}
