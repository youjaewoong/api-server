package api.server.restapi.response.common;

import java.util.Map;

public interface RestAPIResponseFormatter {

    RestAPIResponse formatResponse(Map<String, Object> outFields);
}
