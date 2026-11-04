/*
 * @test
 * @summary JLS 18.5.2 (Java SE 8): the diamond operator cannot be used with an
 *          anonymous class in Java SE 8; the type arguments cannot be inferred.
 * @compile/fail DiamondAnonymousClassFail.java
 */

import java.util.ArrayList;
import java.util.List;

class DiamondAnonymousClassFail {
    void m() {
        List<String> l = new ArrayList<>() { };   // error: cannot use '<>' with anonymous inner classes
    }
}
