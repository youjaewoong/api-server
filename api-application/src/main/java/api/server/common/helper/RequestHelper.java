package api.server.common.helper;

import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@UtilityClass
public class RequestHelper {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    /**
     * 현재 요청의 클라이언트 IP를 반환합니다.
     *
     * @return 클라이언트 IP 주소
     */
    public static String getClientIp() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return "Unknown IP"; // 요청 컨텍스트가 없는 경우 처리
        }

        for (String header : IP_HEADERS) {
            String ip = getHeaderIp(request, header);
            if (ip != null) {
                return normalizeIp(ip);
            }
        }

        // 기본적으로 getRemoteAddr() 호출
        return normalizeIp(request.getRemoteAddr());
    }

    /**
     * 현재 요청의 HttpServletRequest를 반환합니다.
     *
     * @return HttpServletRequest 객체
     */
    private static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null; // 요청이 없는 경우
    }

    /**
     * 헤더에서 IP를 가져옵니다.
     *
     * @param request HttpServletRequest 객체
     * @param header  헤더 이름
     * @return 헤더에서 추출한 IP 주소
     */
    private static String getHeaderIp(HttpServletRequest request, String header) {
        String ip = request.getHeader(header);
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim(); // 다중 IP 중 첫 번째 반환
        }
        return null;
    }

    /**
     * IPv6 로컬호스트를 IPv4 로컬호스트로 변환합니다.
     *
     * @param ip 입력 IP 주소
     * @return 변환된 IP 주소
     */
    private static String normalizeIp(String ip) {
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
