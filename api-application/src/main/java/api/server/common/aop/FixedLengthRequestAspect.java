package api.server.common.aop;

import api.server.common.constant.ControllerNameConstant;
import api.server.fixedlength.enmus.CommonHeaderType;
import api.server.fixedlength.request.FixedLengthRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import api.server.common.constant.ControllerNameConstant;
import api.server.fixedlength.enmus.CommonHeaderType;
import api.server.fixedlength.request.FixedLengthRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * FixedLengthRequestAspect는 특정 컨트롤러 호출 시 요청 객체의 HeaderType 값을 적절히 설정하는 역할을 하는 AOP 클래스입니다.
 */
@Aspect
@Component
public class FixedLengthRequestAspect {

    /**
     * 컨트롤러 메서드가 호출되기 전에 실행되며, 요청 객체의 HeaderType을 설정합니다.
     *
     * @param joinPoint 현재 실행 중인 조인포인트 정보
     * @param request   FixedLength 요청 객체
     */
    @Before("execution(* api.server.fixedlength..*(..)) && args(request,..)")
    public void setHeaderType(JoinPoint joinPoint, FixedLengthRequest request) {
        String controllerName = getControllerName(joinPoint);
        CommonHeaderType headerType = determineHeaderType(controllerName);
        request.setCommonHeaderType(headerType);
    }

    /**
     * 조인포인트를 기반으로 호출된 컨트롤러의 클래스 이름을 반환합니다.
     *
     * @param joinPoint 현재 실행 중인 조인포인트 정보
     * @return 호출된 컨트롤러의 클래스 이름
     */
    private String getControllerName(JoinPoint joinPoint) {
        return joinPoint.getTarget().getClass().getSimpleName();
    }

    /**
     * 클래스 이름에 따라 적절한 HeaderType을 결정합니다.
     *
     * @param controllerName 호출된 컨트롤러의 클래스 이름
     * @return 결정된 CommonHeaderType
     * @throws IllegalArgumentException 클래스 이름이 알려진 값과 일치하지 않을 경우 발생
     */
    private CommonHeaderType determineHeaderType(String controllerName) {
        if (controllerName.contains(ControllerNameConstant.BATCH_NAME)) {
            return CommonHeaderType.BATCH;
        } else if (controllerName.contains(ControllerNameConstant.EAI_NAME)) {
            return CommonHeaderType.EAI;
        }
        throw new IllegalArgumentException("Unknown controller name: " + controllerName);
    }
}
