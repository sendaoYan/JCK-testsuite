/*
 * @test
 * @summary JLS 16.2.7: after an if statement without an else, a variable assigned
 *          only in the then-branch is not definitely assigned (the branch may be
 *          skipped), so a subsequent read is a compile-time error.
 * @compile/fail IfWithoutElseFail.java
 */

class IfWithoutElseFail {
    int m(boolean c) {
        int x;
        if (c) { x = 1; }
        return x;   // error: variable x might not have been initialized
    }
}
