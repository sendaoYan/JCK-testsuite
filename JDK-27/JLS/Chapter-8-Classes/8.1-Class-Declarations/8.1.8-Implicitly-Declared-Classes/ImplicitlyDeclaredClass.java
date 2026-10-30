/*
 * @test
 * @summary JLS 8.1.8: a compact compilation unit implicitly declares a final,
 *          top-level class in the unnamed package whose direct superclass is
 *          Object and which has an implicitly declared default constructor.
 * @compile ImplicitlyDeclaredClass.java
 */

int field = 1;

void main() {
    System.out.println("implicitly declared class");
}
