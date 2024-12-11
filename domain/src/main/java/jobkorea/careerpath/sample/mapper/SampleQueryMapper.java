package jobkorea.careerpath.sample.mapper;

import common.standard.enums.GenericEnumFieldsResolver;
import jobkorea.careerpath.common.config.MapstructConfig;
import jobkorea.careerpath.sample.enums.SampleType;
import jobkorea.careerpath.sample.infrastructure.entity.*;
import jobkorea.careerpath.sample.response.SampleAddressInfoResponse;
import jobkorea.careerpath.sample.response.SampleGeoResponse;
import jobkorea.careerpath.sample.response.SampleResponse;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 변경 대상 및 포맷 작업이 없을 경우 처리하지 않아도 됩니다.
 */
@Mapper(config = MapstructConfig.class)
public interface SampleQueryMapper {

	SampleQueryMapper INSTANCE = Mappers.getMapper(SampleQueryMapper.class);

	/**
	 * str(String) 변수의 값을 lon(Long)변수에 할당
	 * source -> target 할당
	 */
	@Mapping(source = "str", target = "lon")
	SampleTargetEntity toLong(SampleSourceEntity entity);

	/**
	 * zipcode 에 () 중괄호 처리
	 * sampleTypeDescriptions 에 arrays 처리
	 */
	@Mapping(target = "zipcode", expression = "java(mapToSampleFormat(entity.getZipcode()))")
	@Mapping(target = "sampleTypeDescriptions", source = "entity.code", qualifiedByName = "mapToDescriptionFromSampleType")
	SampleAddressInfoResponse toResponse(SampleAddressInfoEntity entity);

	/**
	 * list -> list SampleResponse 변경
	 * source -> target 할당
	 */
	@Mapping(target = "name", expression = "java(mapToSampleFormat(entity.getName()))")
	@Mapping(target = "type", expression = "java(mapToSampleType(entity.getType()))")
	@Named("toSampleResponse")
	SampleResponse toSampleResponse(SampleEntity entity);

	@IterableMapping(qualifiedByName = "toSampleResponse")
	@Named("toResponse")
	List<SampleResponse> toResponse(List<SampleEntity> entity);

	SampleGeoResponse toResponse(SampleGeoEntity entity);

	/**
	 * string format
	 */
	@Named("mapToSampleFormat")
	default String mapToSampleFormat(String zipCode) {
		return String.format("(%s)", zipCode);
	}

	/**
	 * SampleType enum key 로 해당 type 호출
	 */
	@Named("mapToSampleType")
	default SampleType mapToSampleType(String value) {
		return GenericEnumFieldsResolver.toEnumType(SampleType.class,
				SampleType.valueOf(value).getValue(), SampleType.NONE);
	}

	@Named("mapToDescriptionFromSampleType")
	default List<String> mapToDescriptionFromSampleType(String code) {
		if ("NONE".equals(code)) {
			List<String> descriptions = new ArrayList<>();
			for (SampleType sampleType : SampleType.values() ) {
				descriptions.add(sampleType.getDescription());
			}
			return descriptions;
		}
		return Collections.emptyList();
	}

}
