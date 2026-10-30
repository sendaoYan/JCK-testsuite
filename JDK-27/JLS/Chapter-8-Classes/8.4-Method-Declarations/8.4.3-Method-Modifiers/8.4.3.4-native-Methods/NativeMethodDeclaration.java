/*
 * @test
 * @summary JLS 8.4.3.4: a native method's implementation is omitted; the body is
 *          given as a semicolon only.
 * @compile NativeMethodDeclaration.java
 */

class NativeMethodDeclaration {
    native void open(String name) throws java.io.IOException;
}
