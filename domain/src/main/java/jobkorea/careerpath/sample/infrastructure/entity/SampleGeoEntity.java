package jobkorea.careerpath.sample.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleGeoEntity {
	/**
	 * 위도
	 */
	private String lat;
	/**
	 * 경도
	 */
	private String lng;
}
