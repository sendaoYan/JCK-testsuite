/*
 * @test
 * @summary JLS 6.6.1: a top level class with package access may be accessed only
 *          from within the package in which it is declared.
 * @compile Hidden.java
 * @compile/fail Bad.java
 */
package pkg2;

import pkg1.Hidden;

class Bad {
    void m() {
        Hidden h = new Hidden();   // error: Hidden is not accessible
    }
}
