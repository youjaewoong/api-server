package jobkorea.careerpath.sample.infrastructure;

import org.apache.ibatis.annotations.Mapper;

import jobkorea.careerpath.sample.request.CreateSample;
import jobkorea.careerpath.sample.request.DeleteSample;
import jobkorea.careerpath.sample.request.UpdateSample;

@Mapper
public interface SampleCommandRepository {

	int updateSample(UpdateSample updateSample);

	int insertSample(CreateSample createSample);

	int deleteSample(DeleteSample deleteSample);

}
