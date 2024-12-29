package api.server.common.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 페이지가 미포함 조회
 */
@Getter
@Setter
public class ListResponse<T> {

	private long totalCount;

	private List<T> collection;

	public ListResponse(List<T> list) {
		this.totalCount = list.size();
		this.collection = list;
	}
}
