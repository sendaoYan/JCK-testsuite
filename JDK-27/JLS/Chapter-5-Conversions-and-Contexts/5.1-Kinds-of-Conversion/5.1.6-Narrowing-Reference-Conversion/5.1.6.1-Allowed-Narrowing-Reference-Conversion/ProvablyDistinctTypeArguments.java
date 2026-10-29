/*
 * @test
 * @summary JLS 5.1.6.1: no narrowing reference conversion exists from
 *          ArrayList<String> to ArrayList<Object> (or vice versa) because the
 *          type arguments String and Object are provably distinct.
 * @compile/fail ProvablyDistinctTypeArguments.java
 */

import java.util.ArrayList;

class ProvablyDistinctTypeArguments {
    void m() {
        ArrayList<String> ls = new ArrayList<String>();
        ArrayList<Object> lo = (ArrayList<Object>) ls;   // error: provably distinct
    }
}
