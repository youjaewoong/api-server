package api.server.fixedlength.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleSourceEntity {
	
	/**
	 * 문자타입
	 */
	private String str;
}
