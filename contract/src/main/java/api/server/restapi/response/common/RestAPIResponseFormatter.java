package api.server.restapi.response.common;

import java.util.Map;

public interface RestAPIResponseFormatter {

RestAPIResponse formatResponse(String gramId,
                               String serviceId,
                               Map<String, Object> outFields);
}
