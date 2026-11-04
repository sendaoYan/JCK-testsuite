/*
 * @test
 * @summary JLS 8.7 (Java SE 8): a static initializer introduces a static context, so
 *          the keyword super is prohibited there.
 * @compile/fail StaticInitializerCannotUseSuper.java
 */

class StaticInitializerCannotUseSuper {
    static {
        super.hashCode();   // error: super cannot be used in a static context
    }
}
