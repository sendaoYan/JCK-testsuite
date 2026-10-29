/*
 * @test
 * @summary JLS 6.6.1: a protected member is accessible from outside its package
 *          only in a subclass and only when accessed via the right qualifying
 *          type; a non-subclass in another package cannot access it.
 * @compile Base.java
 * @compile/fail Bad.java
 */
package p2;

import p1.Base;

class Bad {
    void m(Base b) {
        int p = b.prot;   // error: prot has protected access in p1.Base
    }
}
