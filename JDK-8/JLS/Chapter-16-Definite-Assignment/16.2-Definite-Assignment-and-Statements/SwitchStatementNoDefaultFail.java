/*
 * @test
 * @summary JLS 16.2.9 (Java SE 8): after a switch statement, a variable assigned
 *          only in some case arms (with no default) is not definitely assigned.
 * @compile/fail SwitchStatementNoDefaultFail.java
 */

class SwitchStatementNoDefaultFail {
    void m(int k) {
        int x;
        switch (k) {
            case 0: x = 1; break;
            case 1: x = 2; break;
        }
        int y = x;   // error: variable x might not have been initialized
    }
}
