/*
 * @test
 * @summary JLS 14.11.1: no more than one default label may be associated with a
 *          switch block.
 * @compile/fail SwitchDuplicateDefaultFail.java
 */

class SwitchDuplicateDefaultFail {
    void m(int v) {
        switch (v) {
            default: break;
            default: break;   // error: duplicate default label
        }
    }
}
