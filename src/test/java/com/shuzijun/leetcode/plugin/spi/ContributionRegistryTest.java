package com.shuzijun.leetcode.plugin.spi;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ContributionRegistryTest {

    @Test
    public void ordersByOrderThenId() {
        List<TestContribution> ordered = ContributionRegistry.ordered(Arrays.asList(
                new TestContribution("tree", 30),
                new TestContribution("all", 20),
                new TestContribution("simple", 10),
                new TestContribution("top", 20)
        ));

        assertEquals("simple", ordered.get(0).getId());
        assertEquals("all", ordered.get(1).getId());
        assertEquals("top", ordered.get(2).getId());
        assertEquals("tree", ordered.get(3).getId());
    }

    @Test
    public void rejectsDuplicateIds() {
        try {
            ContributionRegistry.ordered(Arrays.asList(
                    new TestContribution("simple", 10),
                    new TestContribution("simple", 20)
            ));
            fail("Expected duplicate contribution failure");
        } catch (IllegalArgumentException expected) {
            assertEquals("Duplicate contribution id: simple", expected.getMessage());
        }
    }

    private static final class TestContribution implements OrderedContribution {
        private final String id;
        private final int order;

        private TestContribution(String id, int order) {
            this.id = id;
            this.order = order;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
