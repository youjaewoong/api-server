package api.server.common.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * DisabledDataSource
 *
 * 데이터베이스가 비활성화된 상태(`db.active=false`)에서 사용되는 DataSource 구현체입니다.
 */
@Slf4j
public class DisabledDataSource implements DataSource {

    private final boolean dbActive;

    public DisabledDataSource(boolean dbActive) {
        this.dbActive = dbActive;
    }

    /**
     * 데이터베이스 연결을 요청할 때 예외를 발생시킵니다.
     *
     * @return 연결 객체 (항상 예외 발생)
     * @throws SQLException "데이터베이스가 비활성화되었습니다."
     */
    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLException("데이터베이스가 비활성화되었습니다. db.active=" + dbActive + ".");
    }
    /**
     * 사용자 이름과 비밀번호로 데이터베이스 연결을 요청할 때 예외를 발생시킵니다.
     *
     * @param username 사용자 이름
     * @param password 비밀번호
     * @return 연결 객체 (항상 예외 발생)
     * @throws SQLException "데이터베이스가 비활성화되었습니다."
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLException("사용자 이름과 비밀번호로 데이터베이스 연결을 요청할 때 예외를 발생시킵니다.");
    }


    /**
     * DataSource의 unwrap 메서드 호출 시 예외를 발생시킵니다.
     *
     * @param iface 인터페이스 타입
     * @param <T>   반환 타입
     * @return 항상 예외 발생
     * @throws SQLException "데이터베이스가 비활성화되었습니다."
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    /**
     * DataSource가 특정 인터페이스를 구현하는지 확인할 때 항상 false를 반환합니다.
     *
     * @param iface 인터페이스 타입
     * @return false (항상 구현되지 않음)
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    /**
     * 로그 기록 작성을 요청할 때 null을 반환합니다.
     *
     * @return 항상 null
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    /**
     * 로그 기록 작성을 설정하려고 할 때 예외를 발생시킵니다.
     *
     * @param out PrintWriter 객체
     * @throws SQLException "지원되지 않는 작업입니다."
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLException("지원되지 않는 작업입니다.");
    }

    /**
     * 로그인 타임아웃 설정 요청 시 예외를 발생시킵니다.
     *
     * @param seconds 타임아웃(초)
     * @throws SQLException "지원되지 않는 작업입니다."
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException("지원되지 않는 작업입니다.");
    }

    /**
     * 로그인 타임아웃을 요청할 때 기본값 0을 반환합니다.
     *
     * @return 항상 0
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    /**
     * DataSource의 상위 로거 객체를 반환합니다.
     *
     * @return 기본 로거 객체
     */
    @Override
    public Logger getParentLogger() {
        return Logger.getLogger("DisabledDataSource");
    }
}