package com.sangminlee.mymydata.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * 제한된 크기의 정렬된 추가 전용 리스트를 구현한 유틸리티 클래스입니다.
 * 이 클래스는 지정된 최대 크기를 유지하면서 TreeSet을 활용하여 요소를 자동으로 정렬합니다.
 * 새로운 요소가 추가될 때 최대 크기를 초과하면 가장 낮은 우선순위의 요소가 자동으로 제거됩니다.
 *
 * @param <T> 리스트에 저장될 요소의 타입
 */
public class LimitedSortedAppendOnlyList<T> {

    private final int limit;
    private final TreeSet<T> items;

    /**
     * LimitedSortedAppendOnlyList의 생성자입니다.
     *
     * @param limit      리스트의 최대 크기
     * @param comparator 요소를 정렬하는데 사용할 Comparator
     */
    public LimitedSortedAppendOnlyList(int limit, Comparator<T> comparator) {
        this.limit = limit;
        this.items = new TreeSet<>(comparator);
    }

    /**
     * 새로운 요소를 리스트에 추가합니다.
     * 리스트가 최대 크기에 도달했을 경우, 가장 낮은 우선순위의 요소가 제거됩니다.
     *
     * @param item 추가할 요소
     */
    public void add(T item) {
        items.add(item);
        if (items.size() > limit) {
            items.pollFirst();
        }
    }

    /**
     * 여러 요소를 한 번에 리스트에 추가합니다.
     *
     * @param items 추가할 요소들의 컬렉션
     */
    public void addAll(Collection<T> items) {
        items.forEach(this::add);
    }

    /**
     * 리스트의 요소들을 스트림으로 반환합니다.
     *
     * @return 리스트 요소들의 스트림
     */
    public Stream<T> stream() {
        return items.stream();
    }

    /**
     * 리스트의 마지막 요소(가장 높은 우선순위의 요소)를 반환합니다.
     *
     * @return 마지막 요소를 포함한 Optional, 리스트가 비어있으면 빈 Optional
     */
    public Optional<T> getLast() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(items.getLast());
    }

    /**
     * 리스트를 초기화합니다.
     */
    public void clear() {
        items.clear();
    }

    /**
     * 리스트의 특정 요소를 삭제합니다.
     */
    public void remove(T item) {
        items.remove(item);
    }
}