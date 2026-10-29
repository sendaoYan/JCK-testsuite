/*
 * @test
 * @summary JLS 6.6.1: a package-private member is accessible only from within the
 *          package in which it is declared.
 * @compile Base.java
 * @compile/fail Bad.java
 */
package p2;

import p1.Base;

class Bad {
    void m(Base b) {
        int p = b.pack;   // error: pack is not public; cannot be accessed from outside package
    }
}
