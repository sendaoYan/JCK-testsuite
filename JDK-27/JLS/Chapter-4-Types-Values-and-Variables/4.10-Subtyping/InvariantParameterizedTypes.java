/*
 * @test
 * @summary JLS 4.10: "Subtyping does not extend through parameterized types:
 *          T <: S does not imply that C<T> <: C<S>." Even though String <:
 *          Object, List<String> is not a subtype of List<Object>.
 * @compile/fail InvariantParameterizedTypes.java
 */

import java.util.List;
import java.util.ArrayList;

class InvariantParameterizedTypes {
    void m() {
        List<String> ls = new ArrayList<String>();
        List<Object> lo = ls;   // error: List<String> is not List<Object>
    }
}
