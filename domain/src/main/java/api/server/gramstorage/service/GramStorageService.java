package api.server.gramstorage.service;

import api.server.gramstorage.request.GramInfoRequest;

import java.util.List;

/**
 * GramStorageService
 * JSON 데이터를 파일로 저장, 읽기 및 조회하는 서비스를 제공합니다.
 */
public interface GramStorageService {

	/**
	 * JSON 데이터를 파일로 저장합니다.
	 *
	 * @param gramId 저장할 파일의 ID (파일명으로 사용됨)
	 * @param data   저장할 데이터 (JSON 포맷)
	 */
	public void uploadExcel(String gramId, GramInfoRequest data);
	/**
	 * JSON 파일에서 데이터를 읽어옵니다.
	 *
	 * @param gramId 읽을 파일의 ID (파일명으로 사용됨)
	 * @return GramInfoRequest 객체로 반환
	 */
	public GramInfoRequest findGramInfo(String gramId);

	/**
	 * 저장된 모든 전문 ID 리스트를 조회합니다.
	 *
	 * @return 저장된 전문 ID 리스트
	 */
	public List<String> findAllGramList();
}
