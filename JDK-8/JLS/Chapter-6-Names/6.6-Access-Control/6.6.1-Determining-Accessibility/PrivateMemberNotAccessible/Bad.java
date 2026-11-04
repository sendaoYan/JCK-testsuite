/*
 * @test
 * @summary JLS 6.6.1 (Java SE 8): a private member is accessible only within the
 *          body of the top level class that encloses its declaration, so another
 *          top-level class in a different package cannot access it.
 * @compile Base.java
 * @compile/fail Bad.java
 */
package pv2;

import pv1.Base;

class Bad {
    void m(Base b) {
        int s = b.secret;   // error: secret has private access in Base
    }
}
