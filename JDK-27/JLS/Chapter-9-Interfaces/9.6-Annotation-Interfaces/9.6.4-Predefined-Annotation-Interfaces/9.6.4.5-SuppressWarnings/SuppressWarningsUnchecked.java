/*
 * @test
 * @summary JLS 9.6.4.5: SuppressWarnings("unchecked") suppresses unchecked
 *          warnings arising from a raw-type use.
 * @compile SuppressWarningsUnchecked.java
 */

import java.util.ArrayList;
import java.util.List;

class SuppressWarningsUnchecked {
    @SuppressWarnings("unchecked")
    void m() {
        List raw = new ArrayList();
        raw.add("x");
    }
}
