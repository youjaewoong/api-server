package api.server.enums.entry;

public class GenericEnumDetailEntry<V> {
    private V value;
    private String description;

    GenericEnumDetailEntry(V value, String description) {
        this.value = value;
        this.description = description;
    }

    public static <V> GenericEnumDetailEntry.GenericEnumDetailEntryBuilder<V> builder() {
        return new GenericEnumDetailEntry.GenericEnumDetailEntryBuilder<V>();
    }

    public V getValue() {
        return this.value;
    }

    public String getDescription() {
        return this.description;
    }

    public String toString() {
        Object var10000 = this.getValue();
        return "GenericEnumDetailEntry(value=" + var10000 + ", description=" + this.getDescription() + ")";
    }

    public static class GenericEnumDetailEntryBuilder<V> {
        private V value;
        private String description;

        GenericEnumDetailEntryBuilder() {
        }

        public GenericEnumDetailEntry.GenericEnumDetailEntryBuilder<V> value(V value) {
            this.value = value;
            return this;
        }

        public GenericEnumDetailEntry.GenericEnumDetailEntryBuilder<V> description(String description) {
            this.description = description;
            return this;
        }

        public GenericEnumDetailEntry<V> build() {
            return new GenericEnumDetailEntry<V>(this.value, this.description);
        }

        public String toString() {
            return "GenericEnumDetailEntry.GenericEnumDetailEntryBuilder(value=" + this.value + ", description=" + this.description + ")";
        }
    }
}
