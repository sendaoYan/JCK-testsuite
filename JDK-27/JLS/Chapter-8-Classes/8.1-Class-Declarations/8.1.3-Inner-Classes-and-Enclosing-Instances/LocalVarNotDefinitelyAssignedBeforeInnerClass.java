/*
 * @test
 * @summary JLS 8.1.3: any local variable used but not declared in an inner class
 *          must be definitely assigned before the body of the inner class.
 * @compile/fail LocalVarNotDefinitelyAssignedBeforeInnerClass.java
 */
class LocalVarNotDefinitelyAssignedBeforeInnerClass {
    void m() {
        int x;
        class Local { int y = x; }   // error: variable x might not have been initialized
    }
}
