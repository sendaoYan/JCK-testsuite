/*
 * @test
 * @summary JLS 6.6.1: a private member is accessible only within the body of the
 *          top level class that encloses its declaration.
 * @compile Base.java
 * @compile/fail Bad.java
 */
package p2;

import p1.Base;

class Bad {
    void m(Base b) {
        int s = b.secret;   // error: secret has private access
    }
}
