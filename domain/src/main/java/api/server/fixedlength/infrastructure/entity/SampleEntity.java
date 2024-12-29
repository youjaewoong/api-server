package api.server.fixedlength.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleEntity {

	/**
	 * 고유 ID
	 */
	private int id;

	/**
	 * 이름
	 */
	private String name;

	/**
	 * 제목
	 */
	private String title;

	/**
	 * 내용
	 */
	private String contents;

	/**
	 * 타입
	 */
	private String type;

	/**
	 * 작성일
	 */
	private LocalDateTime regDt;

	/**
	 * 수정일
	 */
	private LocalDateTime editDt;
}
