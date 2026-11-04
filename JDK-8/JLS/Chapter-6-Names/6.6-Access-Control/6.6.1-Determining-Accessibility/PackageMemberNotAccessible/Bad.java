/*
 * @test
 * @summary JLS 6.6.1 (Java SE 8): a package-private member is accessible only
 *          from within the package in which it is declared.
 * @compile Base.java
 * @compile/fail Bad.java
 */
package pp2;

import pp1.Base;

class Bad {
    void m(Base b) {
        int p = b.pack;   // error: pack is not public in Base; cannot be accessed from outside package
    }
}
