/*
 * @test
 * @summary JLS 9.4 (Java SE 8): an abstract interface method may not have a body.
 * @compile/fail AbstractMethodCannotHaveBody.java
 */

interface AbstractMethodCannotHaveBody {
    void m() {}   // error: interface abstract methods cannot have body
}
