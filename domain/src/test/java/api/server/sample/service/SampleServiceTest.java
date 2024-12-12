package api.server.sample.service;

import common.standard.exception.business.BusinessErrorCodeException;
import api.server.common.library.PageCustomHelper;
import api.server.common.model.ListResponse;
import api.server.common.model.PageResponse;
import api.server.sample.SampleClient;
import api.server.sample.SampleDataGenerator;
import api.server.sample.SampleSearchClient;
import api.server.sample.infrastructure.SampleCommandRepository;
import api.server.sample.infrastructure.SampleQueryRepository;
import api.server.sample.request.CreateSample;
import api.server.sample.request.DeleteSample;
import api.server.sample.request.SampleRequest;
import api.server.sample.request.UpdateSample;
import api.server.sample.response.SampleFeignResponse;
import api.server.sample.response.SampleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest(classes = {SampleService.class})
@DisplayName("샘플 서비스 테스트")
class SampleServiceTest {

	@Autowired
	private SampleService sampleService;
	@MockBean
	SampleQueryRepository query;
	@MockBean
	private SampleCommandRepository command;
	@MockBean
	private SampleClient sampleClient;
	@MockBean
	private SampleSearchClient sampleSearchClient;

	@Nested
	@DisplayName("샘플 조회 메소드")
	class ListPageSample {
		@Test
		@DisplayName("pageRowSize 1에 해당되는 반환값 확인")
		void listPageSample() {

			// give
			SampleRequest sampleRequest = SampleRequest.builder()
					.pageRowSize(1)
					.pageIndex(1)
					.build();

			PageCustomHelper.setPageable(sampleRequest, "reg_dt desc");

			List<SampleResponse> response = SampleDataGenerator
					.getSampleResponses(sampleRequest);

			given(query.selectSample(sampleRequest)).willReturn(response);

			// when - 테스트 대상 메소드 호출
			PageResponse<SampleResponse> result =
					new PageResponse<>(query.selectSample(sampleRequest));

			// then - 반환 값이 주어진 값과 일치하는지 확인
			assertThat(result).isNotNull()
					.hasFieldOrPropertyWithValue("totalCount", 1L);

		}

		@Test
		@DisplayName("pageRowSize 10에 해당되는 반환값 확인")
		void listBasicSample() {

			// give
			SampleRequest sampleRequest = SampleRequest.builder()
					.pageRowSize(10)
					.pageIndex(1)
					.build();

			List<SampleResponse> response =
					SampleDataGenerator.getSampleResponses(sampleRequest);

			given(query.selectSample(sampleRequest)).willReturn(response);

			// when
			ListResponse<SampleResponse> result =
					new ListResponse<>(query.selectSample(sampleRequest));

			// then
			// response size 2이기 떄문에 2L 반환
			assertThat(result).isNotNull()
					.hasFieldOrPropertyWithValue("totalCount", 2L);
		}
	}

	@Nested
	@DisplayName("샘플 정보 조회 메소드")
	class GetSample {
		@Test
		@DisplayName("정보가 존재 할 경우 반환값 확인")
		void getSample() {

			// give
			SampleRequest sampleRequest = SampleRequest
					.builder().id("1").build();

			List<SampleResponse> response =
					SampleDataGenerator.getSampleResponses(sampleRequest);

			given(query.selectSample(sampleRequest)).willReturn(response);

			// when & then
			assertThrows(BusinessErrorCodeException.class, () ->
					sampleService.findBySampleId(sampleRequest));

		}

		@Test
		@DisplayName("정보가 존재하지 않을 경우 Exception 확인")
		void getSampleError() {

			// give
			// 존재하지 않는 99의 id값
			SampleRequest sampleRequest = SampleRequest
					.builder().id("99").build();

			List<SampleResponse> response =
					SampleDataGenerator.getSampleResponses(sampleRequest);

			given(query.selectSample(sampleRequest)).willReturn(response);

			// when & then
			assertThrows(BusinessErrorCodeException.class, () ->
					sampleService.findBySampleId(sampleRequest));

		}
	}

	@Nested
	@DisplayName("샘플 정보 확인 메소드")
	class CheckSampleById {

		@Test
		@DisplayName("정보가 있을 경우 반환값 true 확인")
		void checkSampleById() {

			// give
			SampleRequest sampleRequest = SampleRequest
					.builder().id("1").build();

			List<SampleResponse> response =
					SampleDataGenerator.getSampleResponses(sampleRequest);

			given(query.selectSample(sampleRequest)).willReturn(response);

			// when
			Boolean result = sampleService.isBySampleId(sampleRequest);

			// then
			assertTrue(result);
		}

		@Test
		@DisplayName("정보가 없을 경우 반환값 false 확인")
		void checkSampleByIdFalse() {

			// give
			// 존재하지 않는 99의 id값
			SampleRequest sampleRequest = SampleRequest
					.builder().id("99").build();

			List<SampleResponse> response = Collections.emptyList();

			given(query.selectSample(sampleRequest)).willReturn(response);

			// when
			Boolean result = sampleService.isBySampleId(sampleRequest);

			// then
			assertFalse(result);
		}

	}

	@Nested
	@DisplayName("샘플 수정 메소드")
	class UpdateSamples {
		@Test
		@DisplayName("수정 처리가 된 경우 반환값 1에 대한 실행 확인")
		void updateSample() {

			// give
			UpdateSample updateSample =
					SampleDataGenerator.getUpdateSample();

			given(command.updateSample(updateSample)).willReturn(1);

			// when & then
			sampleService.modifySample(updateSample);
			verify(command, times(1)).updateSample(updateSample);
		}

		@Test
		@DisplayName("수정 처리가 실패 한 경우 반환값 0에 대한 exception 확인")
		void updateSampleThrow() {

			// give
			UpdateSample updateSample =
					SampleDataGenerator.getUpdateSample();

			// 수정건이 없으므로 리턴값 0
			given(command.updateSample(updateSample)).willReturn(0);

			// when & then
			assertThrows(BusinessErrorCodeException.class, () ->
					sampleService.modifySample(updateSample));
		}
	}

	@Test
	@DisplayName("추가 처리가 된 경우 반환값 1에 대한 실행 확인")
	void insertSample() {

		// give
		CreateSample createSample = SampleDataGenerator.getCreateSample();

		given(command.insertSample(createSample)).willReturn(1);

		// when & then
		sampleService.saveSample(createSample);
		verify(command, times(1)).insertSample(createSample);
	}

	@Nested
	@DisplayName("샘플 삭제 메소드")
	class DeleteSamples {

		@Test
		@DisplayName("삭제 처리가 된 경우 반환값 1에 대한 실행 확인")
		void deleteSample() {

			// give
			DeleteSample deleteSample = new DeleteSample(List.of("1"));

			given(command.deleteSample(deleteSample)).willReturn(1);

			// when & then
			sampleService.removeSample(deleteSample);
			verify(command, times(1)).deleteSample(deleteSample);
		}

		@Test
		@DisplayName("삭제 처리가 실패 한 경우 반환값 0에 대한 exception 확인")
		void deleteSampleThrow() {

			// give
			DeleteSample deleteSample = new DeleteSample(List.of("99"));

			given(command.deleteSample(deleteSample)).willReturn(0);

			// when & then
			assertThrows(BusinessErrorCodeException.class, () ->
					sampleService.removeSample(deleteSample));
		}

	}

	@Test
	@DisplayName("외부데이터 조회(페인)")
	void listFeignSample() {

		// give
		List<SampleFeignResponse> response = Collections.emptyList();

		given(sampleClient.selectSampleFeign()).willReturn(response);

		// when
		ListResponse<SampleFeignResponse> result =
				sampleService.findSampleFeign();

		// then
		assertThat(result).isNotNull()
				.hasFieldOrPropertyWithValue("totalCount", 0L);

	}

	@Test
	@DisplayName("검색 조회")
	void search() {

		// give

		// when
		Boolean result = sampleService.findSampleSearch();

		// then
		assertTrue(result);
	}
}
