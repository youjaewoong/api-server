package api.server.fixedlength.vo;

import api.server.common.enums.GramType;
import api.server.fixedlength.helper.FixedLengthLengthCalculatorHelper;
import lombok.Data;
import java.util.List;

/**
 * JSON 데이터 모델.
 */
@Data
public class FixedLengthJsonVO {

    private String gramId;
    private String gramName;
    private String serviceId;
    private List<Field> inFields;
    private List<Field> outFields;
    private int totalInLength;  // 추가된 필드
    private int totalOutLength; // 추가된 필드

    @Data
    public static class Field implements FixedLengthLengthCalculatorHelper.HasLength {
        private String fieldId;
        private String fieldName;
        private int length;
        private int offset;
        private GramType gramType;

        @Override
        public int getLength() {
            return this.length;
        }
    }
}
