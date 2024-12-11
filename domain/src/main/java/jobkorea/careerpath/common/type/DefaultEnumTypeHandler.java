package jobkorea.careerpath.common.type;

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

@SuppressWarnings("unused")
public class DefaultEnumTypeHandler<E extends GenericEnum<?>> extends BaseTypeHandler<E> {

	private final Map<String, E> enumValueDirectory;
	private final Map<String, E> enumNameDirectory;

	public DefaultEnumTypeHandler(Class<E> type) {
		if (type == null) {
			throw new IllegalArgumentException("Type argument cannot be null");
		}

		E[] enums = type.getEnumConstants();

		enumValueDirectory = Arrays
				.stream(enums)
				.collect(Collectors.toMap(e -> e.getValue().toString(), e -> e));

		enumNameDirectory = Arrays
				.stream(enums)
				.collect(Collectors.toMap(GenericEnum::name, e -> e));
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
		if (jdbcType == null) {
			ps.setString(i, parameter.name());
		} else {
			ps.setObject(i, parameter.name(), jdbcType.TYPE_CODE); // see r3589
		}
	}

	@Override
	public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		if (value == null && rs.wasNull()) {
			return null;
		}
		return enumValueDirectory.get(value);
	}

	@Override
	public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		if (value == null && rs.wasNull()) {
			return null;
		}
		return enumValueDirectory.get(value);
	}

	@Override
	public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		if (value == null && cs.wasNull()) {
			return null;
		}
		return enumValueDirectory.get(value);
	}
}
