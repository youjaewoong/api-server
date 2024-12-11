package jobkorea.careerpath.sample.infrastructure;

import jobkorea.careerpath.sample.infrastructure.entity.SampleAddressInfoEntity;
import jobkorea.careerpath.sample.infrastructure.entity.SampleEntity;
import jobkorea.careerpath.sample.infrastructure.entity.SampleGeoEntity;
import jobkorea.careerpath.sample.request.SampleRequest;
import jobkorea.careerpath.sample.response.SampleResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SampleQueryRepository {

    List<SampleResponse> selectSample(SampleRequest sampleResponse);

    Long selectCountSample(SampleRequest sampleResponse);

    SampleGeoEntity selectSampleByGeo();

    SampleAddressInfoEntity selectSampleByAddress();

    List<SampleEntity> selectSampleByEntities(SampleRequest sampleResponse);

    List<SampleResponse> selectSample();

}
