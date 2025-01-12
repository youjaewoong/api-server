package api.server.common.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import common.standard.enums.GenericEnum;

/**
 * MyBatis에서 GenericEnum 인터페이스를 구현하는 Enum 타입을 처리하기 위한 TypeHandler입니다.
 *
 * 주요 기능:
 * - Enum의 name 또는 value를 사용하여 데이터베이스와 매핑.
 * - 데이터베이스의 값과 Enum 간 변환을 지원.
 * - MyBatis에서 자동으로 적용되도록 설정 가능.
 *
 * @param <E> GenericEnum 인터페이스를 구현하는 Enum 타입
 */
@SuppressWarnings("unused")
public class DefaultEnumTypeHandler<E extends GenericEnum<?>> extends BaseTypeHandler<E> {

	/**
	 * Enum의 value를 기준으로 Enum 객체를 찾는 매핑 디렉터리
	 */
	private final Map<String, E> enumValueDirectory;

	/**
	 * Enum의 name을 기준으로 Enum 객체를 찾는 매핑 디렉터리
	 */
	private final Map<String, E> enumNameDirectory;

	/**
	 * 생성자. Enum 클래스 타입을 받아 Enum과 데이터베이스 간의 매핑을 준비합니다.
	 *
	 * @param type Enum 클래스 타입
	 */
	public DefaultEnumTypeHandler(Class<E> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}

		// Enum의 value 값을 기준으로 매핑을 생성
		E[] enums = type.getEnumConstants();
		enumValueDirectory = Arrays
				.stream(enums)
				.collect(Collectors.toMap(e -> e.getValue().toString(), e -> e));

		// Enum의 name 값을 기준으로 매핑을 생성
		enumNameDirectory = Arrays
				.stream(enums)
				.collect(Collectors.toMap(GenericEnum::name, e -> e));
	}

	/**
	 * PreparedStatement에 Enum 값을 설정합니다.
	 *
	 * @param ps        PreparedStatement 객체
	 * @param i         매핑할 파라미터 인덱스
	 * @param parameter 설정할 Enum 값
	 * @param jdbcType  JDBC 타입
	 * @throws SQLException PreparedStatement 설정 중 오류 발생 시
	 */
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
		if (jdbcType == null) {
			ps.setString(i, parameter.name());
		} else {
			ps.setObject(i, parameter.name(), jdbcType.TYPE_CODE);
		}
	}

	/**
	 * ResultSet에서 컬럼 이름으로 Enum 값을 읽어옵니다.
	 *
	 * @param rs         ResultSet 객체
	 * @param columnName 컬럼 이름
	 * @return 매핑된 Enum 값 또는 null
	 * @throws SQLException ResultSet에서 값 읽기 중 오류 발생 시
	 */
	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		if (value == null && rs.wasNull()) {
			return null;
		}
		return enumValueDirectory.get(value);
	}

	/**
	 * ResultSet에서 컬럼 인덱스로 Enum 값을 읽어옵니다.
	 *
	 * @param rs          ResultSet 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return 매핑된 Enum 값 또는 null
	 * @throws SQLException ResultSet에서 값 읽기 중 오류 발생 시
	 */
	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		if (value == null && rs.wasNull()) {
			return null;
		}
		return enumValueDirectory.get(value);
	}

	/**
	 * CallableStatement에서 컬럼 인덱스로 Enum 값을 읽어옵니다.
	 *
	 * @param cs          CallableStatement 객체
	 * @param columnIndex 컬럼 인덱스
	 * @return 매핑된 Enum 값 또는 null
	 * @throws SQLException CallableStatement에서 값 읽기 중 오류 발생 시
	 */
	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		if (value == null && cs.wasNull()) {
			return null;
		}
		return enumValueDirectory.get(value);
	}
}