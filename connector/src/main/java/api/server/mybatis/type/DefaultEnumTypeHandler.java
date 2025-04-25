package api.server.mybatis.type;

import api.server.enums.GenericEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
public class DefaultEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

	private final Class<E> type;

	public DefaultEnumTypeHandler(Class<E> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}
		this.type = type;
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
		if (jdbcType == null) {
			ps.setString(i, parameter.name());
		} else {
			ps.setObject(i, parameter.name(), jdbcType.TYPE_CODE);
		}
	}

	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String s = rs.getString(columnName);
		return s == null ? null : Enum.valueOf(type, s);
	}

	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String s = rs.getString(columnIndex);
		return s == null ? null : Enum.valueOf(type, s);
	}

	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String s = cs.getString(columnIndex);
		return s == null ? null : Enum.valueOf(type, s);
	}
}