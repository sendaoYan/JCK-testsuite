/*
 * @test
 * @summary JLS 15.13: a method reference must be congruent with the function
 *          type of its target; otherwise it is a compile-time error.
 * @compile/fail MethodReferenceIncompatible.java
 */

import java.util.function.Function;

class MethodReferenceIncompatible {
    void test() {
        Function<Integer, String> f = String::length;   // error: incompatible types (String->int vs Integer->String)
    }
}
