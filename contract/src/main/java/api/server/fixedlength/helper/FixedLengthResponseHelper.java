package api.server.fixedlength.helper;

import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Map;
@UtilityClass
public class FixedLengthResponseHelper {

    @Data
    public static class Response {
        private String gramId;
        private String serviceId;
        private int inHeaderTotal;
        private int inBodyTotal;
        private int outBodyTotal;
        private Map<String, String> outFields;
    }

    /**
     * 응답 데이터를 생성.
     *
     * @param gramId           gramId
     * @param serviceId        serviceId
     * @param inHeaderTotal    헤더 총 길이
     * @param inBodyTotal      요청 바디 총 길이
     * @param outFields        응답 바디 데이터
     * @param outBodyTotal     응답 바디 총 길이
     * @return Response 객체
     */
    public static Response createResponse(
            String gramId,
            String serviceId,
            int inHeaderTotal,
            int inBodyTotal,
            Map<String, String> outFields,
            int outBodyTotal) {

        Response response = new Response();
        response.setGramId(gramId);
        response.setServiceId(serviceId);
        response.setInHeaderTotal(inHeaderTotal);
        response.setInBodyTotal(inBodyTotal);
        response.setOutBodyTotal(outBodyTotal);
        response.setOutFields(outFields);

        return response;
    }
}
