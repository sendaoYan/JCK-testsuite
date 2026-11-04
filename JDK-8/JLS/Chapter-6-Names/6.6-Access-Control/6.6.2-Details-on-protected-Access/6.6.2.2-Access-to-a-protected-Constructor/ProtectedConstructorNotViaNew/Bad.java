/*
 * @test
 * @summary JLS 6.6.2.2 (Java SE 8): a protected constructor is not accessible to
 *          an unrelated class in another package via a class instance creation
 *          expression (new).
 * @compile Base.java
 * @compile/fail Bad.java
 */
package pcn2;

import pcn1.Base;

class Bad {
    void m() {
        Base b = new Base();   // error: Base() has protected access in Base
    }
}
