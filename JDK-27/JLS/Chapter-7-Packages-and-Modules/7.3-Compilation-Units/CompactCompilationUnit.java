/*
 * @test
 * @summary JLS 7.3: a compact compilation unit consists of class member
 *          declarations at the top level, at least one of which is a method
 *          declaration; it implicitly declares a top level class (§8.1.8).
 * @compile CompactCompilationUnit.java
 */

void main() {
    System.out.println("compact compilation unit");
}
