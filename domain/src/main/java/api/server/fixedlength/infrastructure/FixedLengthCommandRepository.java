package api.server.fixedlength.infrastructure;

import api.server.sample.request.CreateSample;
import api.server.sample.request.DeleteSample;
import api.server.sample.request.UpdateSample;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FixedLengthCommandRepository {

	int updateSample(UpdateSample updateSample);

	void insertSample(CreateSample createSample);

	int deleteSample(DeleteSample deleteSample);

}
