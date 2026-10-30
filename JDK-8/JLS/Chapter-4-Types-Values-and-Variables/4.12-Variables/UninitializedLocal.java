/*
 * @test
 * @summary JLS 4.12.5 (Java SE 8): unlike fields, a local variable is not given
 *          a default value and must be definitely assigned before use.
 * @compile/fail UninitializedLocal.java
 */

class UninitializedLocal {
    void m() {
        int x;
        int y = x;   // error: variable x might not have been initialized
    }
}
