package api.server.sample.mapper;

import api.server.sample.request.CreateSample;
import api.server.sample.request.UpdateSample;
import api.server.sample.response.SampleResponse;
import org.mapstruct.factory.Mappers;

public interface SampleCommandMapper {

    SampleCommandMapper INSTANCE = Mappers.getMapper(SampleCommandMapper.class);

    UpdateSample toUpdateSample(SampleResponse entity);
    CreateSample toCreateSample(SampleResponse entity);
}
