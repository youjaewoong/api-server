package jobkorea.careerpath.sample.service;

import jobkorea.careerpath.sample.enums.SampleType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("샘플 리팩토링 테스트")
@Slf4j
class SampleRefactorTest {

	List<String> list = new ArrayList<>();
	StringBuilder sbUrl = new StringBuilder();

	@Nested
	@DisplayName("IF문 개선")
	class IfRefactor {

		@Test
		@DisplayName("switch 문으로 enum 값 추출")
		void beforImproveByIf() {
			for (SampleType sampleType : SampleType.values()) {
				list.add(getSampleUrlV0(sampleType));
			}
			log.info("result 1 >>> {} ", list);
			assertThat(list).isNotNull();
		}

		@Test
		@DisplayName("map 으로 enum 값 추출")
		void afterImproveByIfV1() {
			for (SampleType sampleType : SampleType.values()) {
				list.add(getSampleUrlV1(sampleType));
			}
			log.info("result 2 >>> {} ", list);
			assertThat(list).isNotNull();
		}

		@Test
		@DisplayName("enum 으로 enum 값 추출")
		void afterImproveByIfV2() {
			for (SampleType sampleType : SampleType.values()) {
				list.add(getSampleUrlV2(sampleType));
			}
			log.info("result 3 >>> {}", list);
			assertThat(list).isNotNull();
		}
	}
	//개선 전
	private String getSampleUrlV0(SampleType sampleType) {
		switch (sampleType) {
			case NONE:
				sbUrl.append("/1/미접수.com");
				break;
			case APPLY:
				sbUrl.append("/2/접수.com");
				break;
			case APPROVAL:
				sbUrl.append("/3/승인.com");
				break;
		}
		return sbUrl.toString();
	}
	//개선 case1 map
	private String getSampleUrlV1(SampleType sampleType) {
		var sampleMap = Map.of(
				sampleType.NONE, "/1/미접수.com",
				sampleType.APPLY, "/2/접수.com",
				sampleType.APPROVAL, "/3/승인.com");
		return sbUrl.append(sampleMap.get(sampleType)).toString();
	}
	//개선 case2 enum
	private String getSampleUrlV2(SampleType sampleType) {
		return sbUrl.append(sampleType.url()).toString();
	}
}
