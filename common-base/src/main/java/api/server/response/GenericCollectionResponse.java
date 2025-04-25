package api.server.response;

import java.io.Serializable;
import java.util.List;

public class GenericCollectionResponse<T> implements Serializable {
    private int pageIndex;
    private int pageRowSize;
    private int totalCount;
    private List<T> collection;

    public static <T> GenericCollectionResponse.GenericCollectionResponseBuilder<T> builder() {
        return new GenericCollectionResponse.GenericCollectionResponseBuilder<T>();
    }

    public int getPageIndex() {
        return this.pageIndex;
    }

    public int getPageRowSize() {
        return this.pageRowSize;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public List<T> getCollection() {
        return this.collection;
    }

    public GenericCollectionResponse(int pageIndex, int pageRowSize, int totalCount, List<T> collection) {
        this.pageIndex = pageIndex;
        this.pageRowSize = pageRowSize;
        this.totalCount = totalCount;
        this.collection = collection;
    }

    public GenericCollectionResponse() {
    }

    public String toString() {
        int var10000 = this.getPageIndex();
        return "GenericCollectionResponse(pageIndex=" + var10000 + ", pageRowSize=" + this.getPageRowSize() + ", totalCount=" + this.getTotalCount() + ", collection=" + this.getCollection() + ")";
    }

    public static class GenericCollectionResponseBuilder<T> {
        private int pageIndex;
        private int pageRowSize;
        private int totalCount;
        private List<T> collection;

        GenericCollectionResponseBuilder() {
        }

        public GenericCollectionResponse.GenericCollectionResponseBuilder<T> pageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        public GenericCollectionResponse.GenericCollectionResponseBuilder<T> pageRowSize(int pageRowSize) {
            this.pageRowSize = pageRowSize;
            return this;
        }

        public GenericCollectionResponse.GenericCollectionResponseBuilder<T> totalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public GenericCollectionResponse.GenericCollectionResponseBuilder<T> collection(List<T> collection) {
            this.collection = collection;
            return this;
        }

        public GenericCollectionResponse<T> build() {
            return new GenericCollectionResponse<T>(this.pageIndex, this.pageRowSize, this.totalCount, this.collection);
        }

        public String toString() {
            return "GenericCollectionResponse.GenericCollectionResponseBuilder(pageIndex=" + this.pageIndex + ", pageRowSize=" + this.pageRowSize + ", totalCount=" + this.totalCount + ", collection=" + this.collection + ")";
        }
    }
}
