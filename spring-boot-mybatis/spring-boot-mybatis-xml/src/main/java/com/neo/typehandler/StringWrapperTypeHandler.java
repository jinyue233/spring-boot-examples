package com.neo.typehandler;

import com.neo.model.StringWrapper;
import org.apache.ibatis.type.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(StringWrapper.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StringWrapperTypeHandler extends BaseTypeHandler<StringWrapper> {
    public StringWrapperTypeHandler(){
        System.out.println("================StringWrapperTypeHandler===================");
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, StringWrapper parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public StringWrapper getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return new StringWrapper(rs.getString(columnName));
    }

    @Override
    public StringWrapper getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return new StringWrapper(rs.getString(columnIndex));
    }

    @Override
    public StringWrapper getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return new StringWrapper(cs.getString(columnIndex));
    }

}
