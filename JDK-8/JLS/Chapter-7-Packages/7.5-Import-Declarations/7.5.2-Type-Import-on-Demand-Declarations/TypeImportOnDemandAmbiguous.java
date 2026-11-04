/*
 * @test
 * @summary JLS 7.5.2 (Java SE 8): a simple name imported on demand from two
 *          different packages is ambiguous, a compile-time error when used.
 * @compile/fail TypeImportOnDemandAmbiguous.java
 */

import java.util.*;
import java.awt.*;

class T {
    void m() {
        List l = null;   // error: List is ambiguous (java.util.List vs java.awt.List)
    }
}
