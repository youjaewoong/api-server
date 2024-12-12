package api.server.sample.infrastructure;

import api.server.sample.infrastructure.entity.SampleAddressInfoEntity;
import api.server.sample.infrastructure.entity.SampleEntity;
import api.server.sample.infrastructure.entity.SampleGeoEntity;
import api.server.sample.request.SampleRequest;
import api.server.sample.response.SampleResponse;
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
