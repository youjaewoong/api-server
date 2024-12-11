package jobkorea.careerpath.sample.mapper;

import jobkorea.careerpath.common.config.MapstructConfig;
import jobkorea.careerpath.sample.request.CreateSample;
import jobkorea.careerpath.sample.request.UpdateSample;
import jobkorea.careerpath.sample.response.SampleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(config = MapstructConfig.class)
public interface SampleCommandMapper {

    SampleCommandMapper INSTANCE = Mappers.getMapper(SampleCommandMapper.class);

    UpdateSample toUpdateSample(SampleResponse entity);

    CreateSample toCreateSample(SampleResponse entity);
}
