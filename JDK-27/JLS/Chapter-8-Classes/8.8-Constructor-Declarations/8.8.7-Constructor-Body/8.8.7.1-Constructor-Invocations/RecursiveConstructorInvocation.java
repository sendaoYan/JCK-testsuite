/*
 * @test
 * @summary JLS 8.8.7.1: it is a compile-time error if a constructor invokes itself
 *          directly or indirectly through a cycle of this(...) invocations.
 * @compile/fail RecursiveConstructorInvocation.java
 */

class RecursiveConstructorInvocation {
    RecursiveConstructorInvocation() {
        this();   // error: recursive constructor invocation
    }
}
