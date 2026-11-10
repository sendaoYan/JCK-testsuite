package pitpilot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class SortInvariantVerifierTest {

    @Test
    public void isSortedTrueIncludingEqualAdjacent() {
        // 含相邻相等元素：区分 compareTo > 0 与 >= 0
        assertTrue(SortInvariantVerifier.isSorted(Arrays.asList(1, 1, 2, 3)));
        assertTrue(SortInvariantVerifier.isSorted(Collections.singletonList(5)));
        assertTrue(SortInvariantVerifier.isSorted(Collections.<Integer>emptyList()));
    }

    @Test
    public void isSortedFalseWhenDescending() {
        assertFalse(SortInvariantVerifier.isSorted(Arrays.asList(2, 1)));
        assertFalse(SortInvariantVerifier.isSorted(Arrays.asList(1, 3, 2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isSortedRejectsNull() {
        SortInvariantVerifier.isSorted(null);
    }

    @Test
    public void isPermutationTrueAndFalse() {
        List<Integer> a = Arrays.asList(3, 1, 2);
        List<Integer> b = Arrays.asList(1, 2, 3);
        assertTrue(SortInvariantVerifier.isPermutation(a, b));
        // 两个参数都未排序：覆盖「移除某个 Collections.sort 调用」的变异体
        assertTrue(SortInvariantVerifier.isPermutation(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)));
        assertTrue(SortInvariantVerifier.isPermutation(Arrays.asList(3, 2, 1), Arrays.asList(1, 3, 2)));
        assertTrue(SortInvariantVerifier.isPermutation(new ArrayList<Integer>(), new ArrayList<Integer>()));
        assertFalse(SortInvariantVerifier.isPermutation(a, Arrays.asList(1, 2)));
        assertFalse(SortInvariantVerifier.isPermutation(a, Arrays.asList(1, 2, 4)));
        assertFalse(SortInvariantVerifier.isPermutation(null, a));
        assertFalse(SortInvariantVerifier.isPermutation(a, null));
    }

    @Test
    public void isEmptyOrSingleton() {
        assertTrue(SortInvariantVerifier.isEmptyOrSingleton(null));
        assertTrue(SortInvariantVerifier.isEmptyOrSingleton(Collections.emptyList()));
        assertTrue(SortInvariantVerifier.isEmptyOrSingleton(Collections.singletonList(1)));
        assertFalse(SortInvariantVerifier.isEmptyOrSingleton(Arrays.asList(1, 2)));
    }
}
