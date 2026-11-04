/*
 * @test
 * @summary JLS 16.2.7 (Java SE 8): after an if statement without an else, a
 *          variable assigned only in the then-branch is not definitely assigned.
 * @compile/fail IfWithoutElseFail.java
 */

class IfWithoutElseFail {
    int m(boolean c) {
        int x;
        if (c) { x = 1; }
        return x;   // error: variable x might not have been initialized
    }
}
