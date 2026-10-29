/*
 * @test
 * @summary JLS 6.3.2.2: a pattern variable introduced by e when true is not in
 *          scope in the else branch of an if (e) S else T statement.
 * @compile/fail IfElseNegative.java
 */

class IfElseNegative {
    void m(Object o) {
        if (o instanceof String s) {
            System.out.println(s);
        } else {
            System.out.println(s);   // error: s not in scope in the else branch
        }
    }
}
