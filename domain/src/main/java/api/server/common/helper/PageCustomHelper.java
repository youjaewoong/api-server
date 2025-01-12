package api.server.common.helper;

import api.server.common.model.SearchCriteria;
import com.github.pagehelper.page.PageMethod;

public class PageCustomHelper {

	private PageCustomHelper() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * 페이지목록 조회를 위한 필터 설정
	 * @param searchCriteria 페이지목록 필터조건
	 */
	public static final void setPageable(SearchCriteria searchCriteria, String orderBy) {

		// rowSize가 1보다 작은면 전체 목록 조회
		if (searchCriteria.getPageRowSize() > 0) {
			PageMethod.startPage(searchCriteria.getPageIndex(), searchCriteria.getPageRowSize());
		}
		PageMethod.orderBy(orderBy);
	}
}
