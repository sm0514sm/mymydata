package com.sangminlee.mymydata.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LimitedSortedAppendOnlyListTest {

    private LimitedSortedAppendOnlyList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new LimitedSortedAppendOnlyList<Integer>(5, Comparator.naturalOrder());
    }

    @Test
    @DisplayName("단일 요소 추가 테스트")
    void addSingleElementTest() {
        list.add(5);
        assertEquals(1, list.stream().count());
        assertTrue(list.stream().anyMatch(i -> i == 5));
    }

    @Test
    @DisplayName("여러 요소 추가 테스트")
    void addMultipleElementsTest() {
        list.add(3);
        list.add(1);
        list.add(4);
        list.add(2);
        list.add(5);

        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5);
        assertIterableEquals(expected, list.stream().toList());
    }

    @Test
    @DisplayName("최대 크기 초과 테스트")
    void exceedLimitTest() {
        list.add(6);
        list.add(2);
        list.add(4);
        list.add(1);
        list.add(5);
        list.add(3);

        assertEquals(5, list.stream().count());
        List<Integer> expected = Arrays.asList(2, 3, 4, 5, 6);
        assertIterableEquals(expected, list.stream().toList());
    }

    @Test
    @DisplayName("컬렉션 일괄 추가 테스트")
    void addAllTest() {
        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3);
        list.addAll(numbers);

        assertEquals(5, list.stream().count());
        List<Integer> expected = Arrays.asList(2, 3, 5, 8, 9);
        assertIterableEquals(expected, list.stream().toList());
    }

    @Test
    @DisplayName("마지막 요소 가져오기 테스트")
    void getLastTest() {
        assertTrue(list.getLast().isEmpty());

        list.add(3);
        list.add(1);
        list.add(4);

        assertEquals(4, list.getLast().orElse(null));
    }

    @Test
    @DisplayName("역순 정렬 테스트")
    void reverseOrderTest() {
        LimitedSortedAppendOnlyList<Integer> reverseList = new LimitedSortedAppendOnlyList<Integer>(5, Comparator.reverseOrder());
        reverseList.add(3);
        reverseList.add(1);
        reverseList.add(4);
        reverseList.add(2);
        reverseList.add(5);

        List<Integer> expected = Arrays.asList(5, 4, 3, 2, 1);
        assertIterableEquals(expected, reverseList.stream().toList());
    }

    @Test
    @DisplayName("문자열 정렬 테스트")
    void stringOrderTest() {
        LimitedSortedAppendOnlyList<String> stringList = new LimitedSortedAppendOnlyList<String>(5, Comparator.naturalOrder());
        stringList.add("banana");
        stringList.add("apple");
        stringList.add("cherry");
        stringList.add("date");

        List<String> expected = Arrays.asList("apple", "banana", "cherry", "date");
        assertIterableEquals(expected, stringList.stream().toList());
    }
}