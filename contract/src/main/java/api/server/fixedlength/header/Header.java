package api.server.fixedlength.header;

import api.server.fixedlength.request.FixedLengthRequest;

public interface Header {

    String buildHeader(FixedLengthRequest request);
}
