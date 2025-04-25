package api.server.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface GenericEnum<T> {
    T getValue();

    String getDescription();

    String name();

    static <K, V extends GenericEnum<K>> Map<K, V> enumsToMap(V[] enums) {
        return enumsToMap(enums, (m) -> true);
    }

    static <K, V extends GenericEnum<K>> Map<K, V> enumsToMap(V[] enums, Predicate<V> predicate) {
        return (Map)Arrays.stream(enums).filter(predicate).collect(Collectors.toUnmodifiableMap(GenericEnum::getValue, Function.identity()));
    }

    static Class<?> getCodeReturnType(Class<?> clazz) {
        try {
            return clazz.getMethod("getValue").getReturnType();
        } catch (NoSuchMethodException var2) {
            return clazz;
        }
    }
}

