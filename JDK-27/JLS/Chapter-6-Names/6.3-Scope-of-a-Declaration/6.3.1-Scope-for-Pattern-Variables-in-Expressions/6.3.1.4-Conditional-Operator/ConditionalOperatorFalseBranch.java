/*
 * @test
 * @summary JLS 6.3.1.4: a pattern variable introduced by a when true is not in
 *          scope in the false branch c of a ? b : c.
 * @compile/fail ConditionalOperatorFalseBranch.java
 */

class ConditionalOperatorFalseBranch {
    void m(Object o) {
        int n = (o instanceof String s) ? s.length() : s.length();   // error: s not in scope in c
    }
}
