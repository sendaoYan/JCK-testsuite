/*
 * @test
 * @summary JLS 6.6.2.2: a protected constructor is NOT accessible via a plain
 *          class instance creation expression new C(...) from another package.
 * @compile Base.java
 * @compile/fail Bad.java
 */
package p2;

import p1.Base;

class Bad {
    void m() {
        Base b = new Base();   // error: Base() has protected access
    }
}
