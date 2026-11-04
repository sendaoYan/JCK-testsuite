/*
 * @test
 * @summary JLS 6.6.2.1 (Java SE 8): a protected static member may not be accessed
 *          by a qualified name Q.Id from a subclass in another package when Q is
 *          the base class type (rather than the subclass type).
 * @compile Base.java Sub.java
 * @compile/fail Main.java
 */
package psb2;

import psb1.Base;

class Main {
    void m() {
        int v = Base.k;   // error: k has protected access in Base
    }
}
