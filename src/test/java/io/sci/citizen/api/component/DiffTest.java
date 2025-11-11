package io.sci.citizen.api.component;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiffTest {

    private record Item(String id, String value) {}

    @Test
    void diffByKeySeparatesElementsByPresence() {
        List<Item> first = List.of(
                new Item("a", "first-a"),
                new Item("b", "first-b"),
                new Item("c", "first-c")
        );

        List<Item> second = List.of(
                new Item("b", "second-b"),
                new Item("c", "second-c"),
                new Item("d", "second-d")
        );

        Diff.Result<Item> result = Diff.diffByKey(first, second, Item::id);

        assertThat(result.onlyInFirst)
                .containsExactly(new Item("a", "first-a"));

        assertThat(result.onlyInSecond)
                .containsExactly(new Item("d", "second-d"));

        assertThat(result.inBoth)
                .containsExactly(
                        new Item("b", "first-b"),
                        new Item("c", "first-c")
                );
    }

    @Test
    void diffByKeyKeepsFirstOccurrenceWhenKeysRepeat() {
        List<Item> first = List.of(
                new Item("a", "first-a"),
                new Item("a", "first-a-duplicate")
        );

        List<Item> second = List.of(
                new Item("a", "second-a"),
                new Item("a", "second-a-duplicate")
        );

        Diff.Result<Item> result = Diff.diffByKey(first, second, Item::id);

        assertThat(result.onlyInFirst).isEmpty();
        assertThat(result.onlyInSecond).isEmpty();
        assertThat(result.inBoth)
                .containsExactly(new Item("a", "first-a"));
    }
}
