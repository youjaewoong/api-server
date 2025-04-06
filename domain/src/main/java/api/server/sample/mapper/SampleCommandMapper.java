package api.server.sample.mapper;

import api.server.sample.request.CreateSample;
import api.server.sample.request.UpdateSample;
import api.server.sample.response.SampleResponse;

public interface SampleCommandMapper {

    UpdateSample toUpdateSample(SampleResponse entity);
    CreateSample toCreateSample(SampleResponse entity);
}
