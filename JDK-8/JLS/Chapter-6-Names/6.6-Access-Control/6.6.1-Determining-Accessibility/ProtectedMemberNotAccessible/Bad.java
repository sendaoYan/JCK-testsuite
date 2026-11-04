/*
 * @test
 * @summary JLS 6.6.1 (Java SE 8): a protected member is accessible outside its
 *          package only to subclasses, so a non-subclass in another package
 *          cannot access it.
 * @compile Base.java
 * @compile/fail Bad.java
 */
package pr2;

import pr1.Base;

class Bad {
    void m(Base b) {
        int p = b.prot;   // error: prot has protected access in Base
    }
}
