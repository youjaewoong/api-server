package api.server.common.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;


/**
 * YesNoBooleanTypeHandler 클래스는 MyBatis에서 Boolean 타입을 데이터베이스의 'Y' 또는 'N' 값과 매핑하는 TypeHandler입니다.
 *
 * 주요 기능:
 * - Boolean 값을 데이터베이스의 'Y' 또는 'N'으로 변환하여 저장.
 * - 데이터베이스에서 가져온 'Y' 또는 'N' 값을 Boolean으로 변환.
 */
public class YesNoBooleanTypeHandler extends BaseTypeHandler<Boolean> {

	/**
	 * PreparedStatement에 Boolean 값을 설정합니다.
	 *
	 * @param ps PreparedStatement 객체
	 * @param i 매핑할 파라미터 인덱스
	 * @param parameter 설정할 Boolean 값
	 * @param jdbcType JDBC 타입
	 * @throws SQLException SQL 실행 중 오류가 발생한 경우
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType)
			throws SQLException {
		// Boolean 값을 "Y" 또는 "N"으로 변환하여 설정
		ps.setString(i, convert(parameter));
	}

	/**
	 * ResultSet에서 컬럼 이름으로 Boolean 값을 가져옵니다.
	 *
	 * @param rs ResultSet 객체
	 * @param columnName 컬럼 이름
	 * @return Boolean 값 또는 null
	 * @throws SQLException SQL 실행 중 오류가 발생한 경우
	 */
	@Override
	public Boolean getNullableResult(ResultSet rs, String columnName)
			throws SQLException {
		// 컬럼 값을 "Y" 또는 "N"으로 가져와 Boolean으로 변환
		return convert(rs.getString(columnName));
	}

	/**
	 * ResultSet에서 컬럼 인덱스로 Boolean 값을 가져옵니다.
	 *
	 * @param rs ResultSet 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return Boolean 값 또는 null
	 * @throws SQLException SQL 실행 중 오류가 발생한 경우
	 */
	@Override
	public Boolean getNullableResult(ResultSet rs, int columnIndex)
			throws SQLException {
		// 컬럼 값을 "Y" 또는 "N"으로 가져와 Boolean으로 변환
		return convert(rs.getString(columnIndex));
	}

	/**
	 * CallableStatement에서 컬럼 인덱스로 Boolean 값을 가져옵니다.
	 *
	 * @param cs CallableStatement 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return Boolean 값 또는 null
	 * @throws SQLException SQL 실행 중 오류가 발생한 경우
	 */
	@Override
	public Boolean getNullableResult(CallableStatement cs, int columnIndex)
			throws SQLException {
		// 컬럼 값을 "Y" 또는 "N"으로 가져와 Boolean으로 변환
		return convert(cs.getString(columnIndex));
	}

	/**
	 * Boolean 값을 "Y" 또는 "N"으로 변환합니다.
	 *
	 * @param value Boolean 값
	 * @return "Y" 또는 "N"
	 */
	private String convert(Boolean value) {
		return Boolean.TRUE.equals(value) ? "Y" : "N";
	}

	/**
	 * "Y" 또는 "N" 값을 Boolean으로 변환합니다.
	 *
	 * @param value 데이터베이스에서 가져온 값
	 * @return Boolean 값
	 */
	private Boolean convert(String value) {
		return "Y".equals(value);
	}
}