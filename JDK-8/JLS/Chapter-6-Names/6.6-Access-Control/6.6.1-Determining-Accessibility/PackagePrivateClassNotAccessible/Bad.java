/*
 * @test
 * @summary JLS 6.6.1 (Java SE 8): a package-private class (no access modifier)
 *          is accessible only from within the package in which it is declared.
 * @compile Hidden.java
 * @compile/fail Bad.java
 */
package ppc2;

import ppc1.Hidden;

class Bad {
    void m() {
        Hidden h = new Hidden();   // error: Hidden is not public in ppc1
    }
}
