package api.server.fixedlength.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleAddressInfoEntity {
	
	/**
	 * 주소1
	 */
	private String street;
	/**
	 * 주소2
	 */
	private String suite;
	/**
	 * 도시
	 */
	private String city;
	/**
	 * 우편번호
	 */
	private String zipcode;
	/**
	 * 위치
	 */
	private SampleGeoEntity geo;
	/**
	 * 샘플코드
	 */
	@Builder.Default
	private String code = "NONE";
}
