package api.server.sample.service;

import api.server.common.exception.custom.BusinessException;
import api.server.common.library.PageCustomHelper;
import api.server.common.model.ListResponse;
import api.server.common.model.PageResponse;
import api.server.sample.SampleClient;
import api.server.sample.SampleSearchClient;
import api.server.sample.enmus.SampleErrorCode;
import api.server.sample.infrastructure.SampleCommandRepository;
import api.server.sample.infrastructure.SampleQueryRepository;
import api.server.sample.infrastructure.entity.SampleSourceEntity;
import api.server.sample.mapper.SampleQueryMapper;
import api.server.sample.request.CreateSample;
import api.server.sample.request.DeleteSample;
import api.server.sample.request.SampleRequest;
import api.server.sample.request.UpdateSample;
import api.server.sample.response.SampleAddressInfoResponse;
import api.server.sample.response.SampleDetailResponse;
import api.server.sample.response.SampleFeignResponse;
import api.server.sample.response.SampleResponse;
import common.standard.response.GenericCollectionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SampleService {

	private final SampleQueryRepository query;
	private final SampleCommandRepository command;
	private final SampleClient sampleClient;
	private final SampleSearchClient sampleSearchClient;

	/**
	 * 목록 정보 조회(페이지 단위)
	 * <pre>
	 * pageHelpler 는 order by 값이 필수여야 정상동작합니다.
	 *  - regDt desc or 범위 내 숫자
	 * response type
	 *  - has : return Object
	 *  - none : return Object
	 * 메서드 명 (controller & service)
	 *  - findAll{서비스명}
	 * 메서드 명 (mapper & mybatis)
	 *  - select{서비스명}
	 * </pre>
	 */
	public PageResponse<SampleResponse> findAllSample(SampleRequest sampleRequest) {

		// 페이징 처리 시
		PageCustomHelper.setPageable(sampleRequest, "reg_dt desc");
		// SampleType enums 적용
		return new PageResponse<>(SampleQueryMapper.INSTANCE
				.toResponse(query.selectSampleByEntities(sampleRequest)));
		//return new PageResponse<>(query.selectSample(sampleRequest));
	}

	/**
	 * 목록 정보 조회
	 * <pre>
	 * response type
	 *  - has : return Object
	 *  - none : return Object
	 * 메서드 명
	 * controller & service
	 *  - find{서비스명}
	 * mapper & mybatis
	 *  - select{서비스명}
	 * </pre>
	 */
	public ListResponse<SampleResponse> findSample(SampleRequest sampleRequest) {

		// SampleType enums 적용
		return new ListResponse<>(SampleQueryMapper.INSTANCE
				.toResponse(query.selectSampleByEntities(sampleRequest)));
		//return new ListResponse<>(query.selectSample(sampleRequest));
	}

	/**
	 * 단일 정보 조회
	 * <pre>
	 * response type
	 *  - has : return Object
	 *  - none : throw BusinessErrorCodeException
	 * 메서드 명
	 * controller & service
	 *  - findBy{서비스명}{타겟}
	 * mapper & mybatis
	 *  - selectBy{서비스명}{타겟}
	 *  - select{서비스명} (optional)
	 * </pre>
	 */
	public SampleResponse findBySampleId(SampleRequest sampleRequest) {
		// SampleType enums 적용
		// optional
		return SampleQueryMapper.INSTANCE
				.toResponse(query.selectSampleByEntities(sampleRequest))
				.stream()
				.findAny()
				.orElseThrow(() -> new BusinessException(SampleErrorCode.DATA_NOT_FOUND));
	}

	/**
	 * 목록 카운트
	 * <pre>
	 * response type
	 *  - has : return Long
	 *  - none : return Long
	 * 메서드 명
	 * controller & service
	 *  - count{서비스명}
	 * mapper & mybatis
	 *  - selectCount{서비스명}
	 * </pre>
	 */
	public Long countSample(SampleRequest sampleRequest) {
		return query.selectCountSample(sampleRequest);
	}

	/**
	 * 정보 확인
	 * <pre>
	 * response type
	 *  - has : return Object
	 *  - none : throw BusinessErrorCodeException
	 * 메서드 명 (controller & service)
	 *  - isBy{서비스명}{타겟} (조건이 1건 일 경우)
	 *  - isBy{서비스명}Info (조건이 2건이상 일경우)
	 * 메서드 명 (mapper & mybatis)
	 *  - checkBy{서비스명}{타겟} (조건이 1건 일 경우)
	 *  - checkBy{서비스명}Info (조건이 2건이상 일경우)
	 *  - select{서비스명} (optional)
	 * </pre>
	 */
	public Boolean isBySampleId(SampleRequest sampleRequest) {

		// optional
		return query.selectSample(sampleRequest)
				.stream()
				.findAny()
				.isPresent();
	}

	/**
	 * 등록
	 * <pre>
	 * response type
	 *  - void
	 * 메서드 명 (controller & service)
	 *  - save{서비스명}
	 * 메서드 명 (mapper & mybatis)
	 *  - insert{서비스명}
	 * </pre>
	 */
	public void saveSample(CreateSample createSample) {
		command.insertSample(createSample);
	}

	/**
	 * 수정
	 * <pre>
	 * response type
	 *  - has : void
	 *  - none : throw BusinessErrorCodeException
	 * 메서드 명 (controller & service)
	 *  - modify{서비스명}
	 * 메서드 명 (mapper & mybatis)
	 *  - update{서비스명}
	 * </pre>
	 */
	public void modifySample(UpdateSample updateSample) {

		if (command.updateSample(updateSample) == 0) {
			throw new BusinessException(SampleErrorCode.DATA_NOT_FOUND);
		}
	}

	/**
	 * 여러 정보 삭제
	 * <pre>
	 * response type
	 *  - has : void
	 *  - none : throw BusinessErrorCodeException
	 * 메서드 명 (controller & service)
	 *  - remove{서비스명}
	 * 메서드 명 (mapper & mybatis)
	 *  - delete{서비스명}
	 * </pre>
	 */
	public void removeSample(DeleteSample deleteSample) {

		if (command.deleteSample(deleteSample) == 0) {
			throw new BusinessException(SampleErrorCode.DATA_NOT_FOUND);
		}
	}

	/**
	 * 	단일 정보 삭제
	 * <pre>
	 * response type
	 *  - has : void
	 *  - none : throw BusinessErrorCodeException
	 * 메서드 명 (controller & service)
	 *  - removeBy{서비스명}{타겟}
	 * 메서드 명 (mapper & mybatis)
	 *  - deleteBySample{타겟}
	 *  - deleteSample (optional)
	 * </pre>
	 */
	public void removeBySampleId(String id) {

		// optional
		if (command.deleteSample(new DeleteSample(List.of(id))) == 0) {
			throw new BusinessException(SampleErrorCode.DATA_NOT_FOUND);
		}
	}

	/**
	 * FeignClient 통한 외부 API 조회
	 */
	public ListResponse<SampleFeignResponse> findSampleFeign() {
		return new ListResponse<>(sampleClient.selectSampleFeign());
	}

	/**
	 * 검색 API 데이터 호출
	 */
	public Boolean findSampleSearch() {

		//String where = URLEncoder.encode("Subject='급구'", Charset.forName("EUC-KR"));
		String search = sampleSearchClient.selectSampleSearch(
				"Title,Content",
				"albamon.vMon_Konan_Community&", null, 10);
		String volumes = sampleSearchClient.selectVolume();

		log.debug("search search >>> {}", search);
		log.debug("search volumes >>> {}", volumes);
		return true;
	}

	/**
	 * 문자열 "99" to 숫자 99
	 * entity to entity
	 */
	public Long stringToLong() {
		return SampleQueryMapper.INSTANCE.toLong(SampleSourceEntity
				.builder()
				.str("99")
				.build()).getLon();
	}

	/**
	 * list to list
	 * entity -> response
	 */
	public GenericCollectionResponse<SampleResponse> listToList(SampleRequest sampleRequest) {

		List<SampleResponse> response = SampleQueryMapper.INSTANCE.toResponse(query.selectSampleByEntities(sampleRequest));
		return GenericCollectionResponse.<SampleResponse>builder()
				.pageIndex(sampleRequest.getPageIndex())
				.pageRowSize(sampleRequest.getPageRowSize())
				.totalCount(response.size())
				.collection(response)
				.build();
	}

	/**
	 * 추가정보 확장
	 * info to detail
	 * entity -> response
	 */
	public SampleDetailResponse infoToDetail() {

		SampleResponse sampleResponse = query.selectSample()
				.stream()
				.findAny()
				.orElseThrow(() -> new BusinessException(SampleErrorCode.DATA_NOT_FOUND));

		// 주소정보 호출 및 response 변환
		SampleAddressInfoResponse addressResponse =
				SampleQueryMapper.INSTANCE.toResponse(
						query.selectSampleByAddress());

		// 위치정보 호출 및 response 변환
		addressResponse.setGeo(SampleQueryMapper.INSTANCE.toResponse(
				query.selectSampleByGeo()));

		// optional
		return SampleDetailResponse.builder()
				.userInformation(sampleResponse)
				.addressInformation(addressResponse)
				.build();
	}
}
