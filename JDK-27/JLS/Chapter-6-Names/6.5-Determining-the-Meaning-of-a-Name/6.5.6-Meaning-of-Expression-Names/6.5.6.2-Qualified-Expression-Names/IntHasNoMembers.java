/*
 * @test
 * @summary JLS 6.5.6.2 Example 6.5.6.2-1: a qualified expression name Q.Id where
 *          Q is not a reference type is a compile-time error (int has no members).
 * @compile/fail IntHasNoMembers.java
 */

class IntHasNoMembers {
    void m() {
        int i = 0;
        i.x++;   // error: int cannot be dereferenced
    }
}
