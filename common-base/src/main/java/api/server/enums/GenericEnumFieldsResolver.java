package api.server.enums;

import api.server.enums.GenericEnum;
import api.server.enums.entry.GenericEnumDetailEntry;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class GenericEnumFieldsResolver {
    private GenericEnumFieldsResolver() {
    }

    public static <T extends GenericEnum<?>, V> Map<String, V> getEnumFieldsNames(Class<T> enumClass, Class<V> valueClass) throws IllegalAccessException {
        TreeMap<String, V> result = new TreeMap();
        getEnumFields(enumClass, valueClass).forEach((key, value) -> result.put(key.toString(), value));
        return result;
    }

    public static <T extends GenericEnum<?>, V> Map<T, V> getEnumFields(Class<T> enumClass, Class<V> valueClass) throws IllegalAccessException {
        Map<T, V> result = new TreeMap();
        Field[] fields = enumClass.getDeclaredFields();

        for(Field field : fields) {
            if (isEnumField(field)) {
                T enumType = (T)(Enum.valueOf(enumClass.asSubclass(Enum.class), field.getName()));
                result.put(enumType, (V) ((GenericEnum)field.get((Object)null)).getValue());
            }
        }

        return result;
    }

    public static <T extends GenericEnum<?>, V> Map<T, GenericEnumDetailEntry<V>> getEnumFieldsWithDetail(Class<T> enumClass, Class<V> valueClass) throws IllegalAccessException {
        TreeMap<T, GenericEnumDetailEntry<V>> result = new TreeMap();
        Field[] fields = enumClass.getDeclaredFields();

        for(Field field : fields) {
            if (isEnumField(field)) {
                T enumType = (T)(Enum.valueOf(enumClass.asSubclass(Enum.class), field.getName()));
                V value = (V)((GenericEnum)field.get((Object)null)).getValue();
                String description = enumType.getDescription();
                result.put(enumType, (GenericEnumDetailEntry<V>) GenericEnumDetailEntry.builder().value(value).description(description).build());
            }
        }

        return result;
    }

    public static <T extends GenericEnum<?>> T toEnumType(Class<T> enumClass, Object value) throws IllegalAccessException, InvalidParameterException {
        Optional<? extends Map.Entry<T, ?>> entry = getEnumFields(enumClass, value.getClass()).entrySet().stream().filter((entity) -> {
            boolean result = entity.getValue().equals(value);
            if (!result) {
                try {
                    result = entity.getValue().toString().equals(value);
                } catch (Exception var4) {
                }
            }

            return result;
        }).findFirst();
        if (entry.isEmpty()) {
            throw new InvalidParameterException("Not Exist Enum Generic Value - value : " + value);
        } else {
            return (T)(((Map.Entry)entry.get()).getKey());
        }
    }

    public static <T extends GenericEnum<?>> T toEnumType(Class<T> enumClass, Object value, T defaultTypeWhenThrow) {
        try {
            return (T)toEnumType(enumClass, value);
        } catch (Exception var4) {
            return defaultTypeWhenThrow;
        }
    }

    public static <T extends GenericEnum<?>> Type getValueType(Class<T> enumClass) {
        return ((ParameterizedType)enumClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }

    private static boolean isEnumField(Field field) {
        return java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                java.lang.reflect.Modifier.isFinal(field.getModifiers()) &&
                java.lang.reflect.Modifier.isPublic(field.getModifiers()) &&
                GenericEnum.class.isAssignableFrom(field.getType());
    }
}