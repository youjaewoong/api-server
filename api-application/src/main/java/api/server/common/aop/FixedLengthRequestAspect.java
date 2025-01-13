package api.server.common.aop;

import api.server.fixedlength.enmus.CommonHeaderType;
import api.server.fixedlength.request.FixedLengthRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FixedLengthRequestAspect {

    @Before("execution(* api.server.fixedlength..*(..)) && args(request,..)")
    public void setHeaderType(JoinPoint joinPoint, FixedLengthRequest request) {
        // 현재 호출된 컨트롤러 클래스 이름 가져오기
        String controllerName = joinPoint.getTarget().getClass().getSimpleName();

        // 클래스 이름에 따라 headerType 설정
        if (controllerName.contains("BatchController")) {
            request.setCommonHeaderType(CommonHeaderType.BATCH);
        } else if (controllerName.contains("Controller")) {
            request.setCommonHeaderType(CommonHeaderType.EAI);
        }
    }
}
