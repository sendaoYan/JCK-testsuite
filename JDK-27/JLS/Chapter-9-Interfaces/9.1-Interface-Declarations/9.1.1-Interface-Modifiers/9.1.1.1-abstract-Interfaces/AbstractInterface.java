/*
 * @test
 * @summary JLS 9.1.1.1: every interface is implicitly abstract; the abstract
 *          modifier is obsolete and may be redundantly specified.
 * @compile AbstractInterface.java
 */

abstract interface AbstractInterface {
    void m();
}
