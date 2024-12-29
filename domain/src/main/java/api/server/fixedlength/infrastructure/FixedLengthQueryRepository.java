package api.server.fixedlength.infrastructure;

import api.server.fixedlength.infrastructure.entity.SampleAddressInfoEntity;
import api.server.fixedlength.infrastructure.entity.SampleEntity;
import api.server.fixedlength.infrastructure.entity.SampleGeoEntity;
import api.server.sample.request.SampleRequest;
import api.server.sample.response.SampleResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FixedLengthQueryRepository {

    List<SampleResponse> selectSample(SampleRequest sampleResponse);

    Long selectCountSample(SampleRequest sampleResponse);

    SampleGeoEntity selectSampleByGeo();

    SampleAddressInfoEntity selectSampleByAddress();

    List<SampleEntity> selectSampleByEntities(SampleRequest sampleResponse);

    List<SampleResponse> selectSample();

}
