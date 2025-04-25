package api.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그 유형을 정의하는 열거형
 */
@AllArgsConstructor
@Getter
public enum LogType {
    CG("Connect :: {}"),
    DG("Disconnect :: {}"),
    RG("Receive Data :: {}"),
    SG("Send Normal Data :: {}"),
    SE("Send Error Data :: {}"),
    ER("Error :: {}"),
    LG("Log :: {}"),
    CV("Connect to VAN :: {}"),
    DV("Disconnect to VAN :: {}"),
    SV("Send to VA :: {}"),
    RV("Receive from VAN :: {}");

    private final String description;

}
