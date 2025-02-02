package api.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GramType {
    /**
     * <pre>
     *  C : Char
     *  N : Number
     *  L : list 인 경우 아래 필드들은 반복문 대상
     * </pre>
     */
    C, N, L;
}
