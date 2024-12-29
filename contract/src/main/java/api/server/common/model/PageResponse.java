package api.server.common.model;

import java.util.ArrayList;
import java.util.List;

import com.github.pagehelper.PageInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * 페이지 포함 조회
 */
@Getter
@Setter
public class PageResponse<T> extends SearchCriteria {

	private long totalCount = 0L;

	private List<T> collection;

	public PageResponse(List<T> contents) {

		PageInfo<T> page = new PageInfo<>(contents);
		this.collection = page.getList();
		this.setTotalCount(page.getTotal());
		super.setPageIndex(page.getPageNum());
		super.setPageRowSize(page.getPageSize());
	}

}
