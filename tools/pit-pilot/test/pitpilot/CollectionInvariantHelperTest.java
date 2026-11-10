package pitpilot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class CollectionInvariantHelperTest {

    @Test
    public void distinctAndDuplicateLists() {
        assertTrue(CollectionInvariantHelper.allDistinct(Arrays.asList(1, 2, 3)));
        assertTrue(CollectionInvariantHelper.allDistinct(Collections.<Integer>emptyList()));
        assertTrue(CollectionInvariantHelper.allDistinct(Collections.singletonList(7)));
        assertFalse(CollectionInvariantHelper.allDistinct(Arrays.asList(1, 2, 1)));
        assertFalse(CollectionInvariantHelper.allDistinct(Arrays.asList("a", "b", "a")));
    }

    @Test
    public void dedupRelationHoldsAndFails() {
        assertTrue(CollectionInvariantHelper.isDedupOf(Arrays.asList(3, 1, 2, 1), new HashSet<>(Arrays.asList(1, 2, 3))));
        assertFalse(CollectionInvariantHelper.isDedupOf(Arrays.asList(1, 2), new HashSet<>(Arrays.asList(1))));
    }

    @Test
    public void emptySemanticsWithNull() {
        assertTrue(CollectionInvariantHelper.isEmpty(null));
        assertTrue(CollectionInvariantHelper.isEmpty(Collections.emptyList()));
        assertFalse(CollectionInvariantHelper.isEmpty(Collections.singletonList(1)));
    }

    @Test
    public void intersectionEqualityTrueAndFalse() {
        Set<Integer> expected = new HashSet<>(Arrays.asList(2, 3));
        assertTrue(CollectionInvariantHelper.intersectionEquals(Arrays.asList(1, 2, 3), Arrays.asList(2, 3, 4), expected));
        assertFalse(CollectionInvariantHelper.intersectionEquals(Arrays.asList(1, 2), Arrays.asList(3, 4), expected));
    }
}
