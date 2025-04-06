package api.server.payment.van;

import api.server.payment.gateway.AbstractApp;
import api.server.payment.gateway.AbstractWorker;
import api.server.payment.gateway.AppWorker;
import api.server.payment.my.MyException;

import java.util.Hashtable;
import java.util.Vector;

/************************************************
 * AppVANManager: VAN 시스템의 클라이언트 연결 관리 클래스
 *
 * @author 케이알파트너스
 ************************************************/
public class AppVANManager {

    // Singleton 패턴을 위한 정적 변수, 클래스의 유일한 인스턴스를 저장
    static private AppVANManager instance;

    // 시스템 이름과 관련된 클라이언트 연결 풀을 저장하는 Hashtable
    private Hashtable<String, Vector> vanConnections = new Hashtable<String, Vector>();

    // 사용 중인 연결의 수를 추적하기 위한 변수 (현재 코드에서 사용되지 않음)
    private int checkedOut = 0;

    /**
     * Singleton 인스턴스를 반환하는 메서드 
     * - 처음 호출 시 인스턴스를 생성하고 이후 동일한 인스턴스를 반환
     * - Thread-Safe를 보장하기 위해 synchronized 선언
     *
     * @return AppVANManager 인스턴스
     * @throws MyException 예외 처리
     */
    static synchronized public AppVANManager getInstance() throws MyException {
        if (instance == null) {
            instance = new AppVANManager();
        }
        return instance;
    }

    /**
     * 생성자: VAN 연결이 활성화되어 있는지 확인 후 연결 풀 생성
     *
     * @throws MyException VAN 연결 초기화 실패 시 예외 발생
     */
    public AppVANManager() throws MyException {
        // VAN 사용 여부 확인 (설정값이 "Y"가 아닌 경우 기능을 활성화하지 않음)
        if (!AbstractApp.g_appCfg.getVANEnable().equals("Y")) {
            System.out.println("[AppVANManager] AppVANManager is not enabled!!!");
            return;
        }
        // 연결 풀 생성
        createPools();
    }

    /**
     * 시스템 이름에 해당하는 VAN 클라이언트를 풀에서 가져옴
     * - 필요 시 새로운 연결 객체를 생성하여 반환
     *
     * @param name 시스템 이름
     * @param appWorker 클라이언트 작업을 처리할 AbstractWorker 객체
     * @return AppVANClient 객체 (사용 가능한 연결)
     * @throws MyException 오류 발생 시 예외 처리
     */
    public synchronized AppVANClient getConnection(String name, AbstractWorker appWorker) throws MyException {
        AppVANClient con = null;

        try {
            // PVC(Virtual Circuit) 타입인 경우
            // if (AbstractApp.g_appCfg.getVANType(name).equals("PVC")) {
            if (name.equals("PVC")) {
                if (vanConnections.size() > 0) {
                    // 해당 시스템의 연결 풀에서 사용 가능한 클라이언트를 가져옴
                    Vector freeConnections = (Vector) vanConnections.get(name);
                    if (freeConnections.size() > 0) {
                        // 기존 객체 재사용
                        con = (AppVANClient) freeConnections.firstElement();
                        freeConnections.removeElementAt(0);
                        con.checkConnection(appWorker);
                    } else {
                        // 새로운 연결 생성 및 초기화
                        Class cls = Class.forName(AbstractApp.g_appCfg.getVANClass(name));
                        con = (AppVANClient) cls.newInstance();
                        con.init(this, name, "VWorker99"); // 기본 워커 ID 사용
                    }
                } else {
                    // 연결 풀 정보 자체가 없는 경우 예외 발생
                    throw new MyException("[AppVANManager::getConnection]", MyException.SY25, "", MyException.sysErrMsg);
                }
            } else {
                // SVC(Service Circuit) 타입인 경우 직접 생성
                Class cls = Class.forName(AbstractApp.g_appCfg.getVANClass(name));
                con = (AppVANClient) cls.newInstance();
                con.init(this, name, "VWorker99");
            }
        } catch (Exception e) {
            throw new MyException("[AppVANManager::getConnection]", MyException.SY20, "Exception:" + e, MyException.sysErrMsg);
        }

        return con; // 결과 클라이언트 반환
    }

    /**
     * 사용 완료된 VAN 클라이언트를 연결 풀에 반환
     * - VWorker99 객체일 경우 연결을 닫음
     *
     * @param name 시스템 이름
     * @param appWorker 사용자의 작업자 객체
     * @param con 반환할 연결 객체
     * @throws MyException 예외 처리
     */
    public synchronized void freeConnection(String name, AbstractWorker appWorker, AppVANClient con) throws MyException {
        if (con.getID().equals("VWorker99")) {
            // 기본 워커는 종료 처리
            con.closeConnection();
        } else {
            // 유효성 확인 후 연결을 풀에 추가
            con.checkConnection(appWorker);
            Vector freeConnections = (Vector) vanConnections.get(name);
            freeConnections.addElement(con);
            notifyAll(); // 대기 중인 쓰레드를 깨움
        }
    }

    /**
     * 특정 시스템 이름에 대한 모든 연결 닫기
     *
     * @param name 시스템 이름
     */
    public synchronized void allCloseConnection(String name) {
        Vector freeConnections = (Vector) vanConnections.get(name);
        for (int i = 0; i < freeConnections.size(); i++) {
            AppVANClient con = (AppVANClient) freeConnections.elementAt(i);
            con.closeConnection(); // 모든 연결 객체를 닫음
        }
    }

    /**
     * 연결 풀 생성
     * - 설정 파일(config.xml)에 정의된 시스템 목록에 따라 연결 풀을 초기화
     * - PVC 시스템의 경우 미리 정해진 갯수만큼 연결을 생성
     *
     * @throws MyException 초기화 실패 시 예외 발생
     */
    private void createPools() throws MyException {

    }

    public void closeConnection(String vanName, AppWorker appWorker, AppVANClient vanConn) {
    }
}