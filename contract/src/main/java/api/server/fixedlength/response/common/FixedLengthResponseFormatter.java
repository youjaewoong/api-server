package api.server.fixedlength.response.common;

import java.util.Map;

public interface FixedLengthResponseFormatter {

    FixedLengthResponse formatResponse(String gramId,
                                   String serviceId,
                                   int inHeaderTotal,
                                   int inBodyTotal,
                                   Map<String, Object> outFields,
                                   int outBodyTotal);

}
