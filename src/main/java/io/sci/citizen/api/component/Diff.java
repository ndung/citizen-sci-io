package io.sci.citizen.api.component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Diff {

    public static final class Result<T> {
        public final List<T> onlyInFirst;
        public final List<T> onlyInSecond;
        public final List<T> inBoth; // optional but handy
        Result(List<T> a, List<T> b, List<T> both) { onlyInFirst=a; onlyInSecond=b; inBoth=both; }
    }

    public static <T,K> Result<T> diffByKey(List<T> first, List<T> second, Function<T,K> keyFn) {
        Map<K, T> aByKey = first.stream()
                .collect(Collectors.toMap(keyFn, Function.identity(), (u, v) -> u, LinkedHashMap::new));
        Map<K, T> bByKey = second.stream()
                .collect(Collectors.toMap(keyFn, Function.identity(), (u, v) -> u, LinkedHashMap::new));

        Set<K> aKeys = new LinkedHashSet<>(aByKey.keySet());
        Set<K> bKeys = new LinkedHashSet<>(bByKey.keySet());

        Set<K> onlyAKeys = new LinkedHashSet<>(aKeys); onlyAKeys.removeAll(bKeys);
        Set<K> onlyBKeys = new LinkedHashSet<>(bKeys); onlyBKeys.removeAll(aKeys);
        Set<K> bothKeys   = new LinkedHashSet<>(aKeys); bothKeys.retainAll(bKeys);

        List<T> onlyInFirst  = onlyAKeys.stream().map(aByKey::get).collect(Collectors.toList());
        List<T> onlyInSecond = onlyBKeys.stream().map(bByKey::get).collect(Collectors.toList());
        List<T> inBoth       = bothKeys.stream().map(aByKey::get).collect(Collectors.toList());

        return new Result<>(onlyInFirst, onlyInSecond, inBoth);
    }
}
