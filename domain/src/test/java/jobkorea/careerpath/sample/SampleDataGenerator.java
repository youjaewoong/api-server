package jobkorea.careerpath.sample;

import jobkorea.careerpath.sample.enums.SampleType;
import jobkorea.careerpath.sample.mapper.SampleCommandMapper;
import jobkorea.careerpath.sample.request.CreateSample;
import jobkorea.careerpath.sample.request.SampleRequest;
import jobkorea.careerpath.sample.request.UpdateSample;
import jobkorea.careerpath.sample.response.SampleResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SampleDataGenerator {
	public static List<SampleResponse> getSampleResponses(SampleRequest sampleRequest) {
		List<SampleResponse> response = listSampleData().stream()
				.filter(rs -> {
					if (sampleRequest.getId() != null) {
						return sampleRequest.getId()
								.equals(String.valueOf(rs.getId()));
					}
					return true;
				}).limit(sampleRequest.getPageRowSize())
				.collect(Collectors.toList());

		return response;
	}

	public static UpdateSample getUpdateSample() {
		return SampleCommandMapper.INSTANCE
				.toUpdateSample(listSampleData().get(0));
	}

	public static CreateSample getCreateSample() {
		return SampleCommandMapper.INSTANCE
				.toCreateSample(listSampleData().get(0));
	}

	public static List<SampleResponse> listSampleData() {
		return List.of(
				SampleResponse.builder()
						.id(1)
						.name("홍길동")
						.title("제목 입니다.")
						.contents("내용 입니다.")
						.type(SampleType.APPLY)
						.regDt(LocalDateTime.now())
						.editDt(LocalDateTime.now())
						.build(),
				SampleResponse.builder()
						.id(2)
						.name("손오공")
						.title("제목 입니다.")
						.contents("내용 입니다.")
						.type(SampleType.APPLY)
						.regDt(LocalDateTime.now())
						.editDt(LocalDateTime.now())
						.build());
	}
}
