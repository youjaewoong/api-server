package api.server.payment.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskInfoEntity {
    private String riskLevel;        // 위험도 레벨
    private String riskType;         // 위험 유형
    private String riskDescription;  // 위험 설명
    private Double riskScore;        // 위험 점수
    private String recommendation;   // 권장사항
}