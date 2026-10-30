/*
 * @test
 * @summary JLS 8.1.5: a non-abstract class must implement all abstract member
 *          methods of each direct superinterface.
 * @compile/fail ConcreteClassMustImplementAbstract.java
 */

interface Colorable {
    void setColor(int color);
    int getColor();
}

class ConcreteClassMustImplementAbstract implements Colorable {
    int color;
}
