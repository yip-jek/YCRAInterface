import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConnectionFactory {

	private YCIConfig             m_cfg      = null;
	private ArrayList<Connection> m_listConn = null;

	public ConnectionFactory(YCIConfig cfg) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		m_cfg = cfg;

		Init();
	}

	private void Init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class.forName(m_cfg.GetDBDriver()).newInstance();

		m_listConn = new ArrayList<Connection>();
	}

	public Connection CreateConnection() throws SQLException {
		Connection new_conn = DriverManager.getConnection(m_cfg.GetDBURL(), m_cfg.GetDBUser(), m_cfg.GetDBPassword());
		m_listConn.add(new_conn);
		return new_conn;
	}

	public boolean ReleaseConnection(Connection conn) throws SQLException {
		if ( m_listConn.contains(conn) ) {
			conn.close();
			m_listConn.remove(conn);
			return true;
		} else {
			return false;
		}
	}

	public void ReleaseAllConnection() throws SQLException {
		if ( !m_listConn.isEmpty() ) {
			final int MAX_SIZE = m_listConn.size();
			for ( int i = 0; i < MAX_SIZE; ++i ) {
				m_listConn.get(i).close();
			}

			m_listConn.clear();
		}
	}

	public int GetConnectionSize() {
		return m_listConn.size();
	}

}
