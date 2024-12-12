package api.server.sample.mapper;

import api.server.common.config.MapstructConfig;
import api.server.sample.request.CreateSample;
import api.server.sample.request.UpdateSample;
import api.server.sample.response.SampleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = MapstructConfig.class)
public interface SampleCommandMapper {

    SampleCommandMapper INSTANCE = Mappers.getMapper(SampleCommandMapper.class);

    UpdateSample toUpdateSample(SampleResponse entity);

    CreateSample toCreateSample(SampleResponse entity);
}
