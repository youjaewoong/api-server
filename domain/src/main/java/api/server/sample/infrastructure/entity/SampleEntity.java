package api.server.sample.infrastructure.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
