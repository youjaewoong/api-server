package api.server.sample.infrastructure;

import org.apache.ibatis.annotations.Mapper;

import api.server.sample.request.CreateSample;
import api.server.sample.request.DeleteSample;
import api.server.sample.request.UpdateSample;

@Mapper
public interface SampleCommandRepository {

	int updateSample(UpdateSample updateSample);

	int insertSample(CreateSample createSample);

	int deleteSample(DeleteSample deleteSample);

}
