import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

// 数据库访问类
public class YCIDao {

	public static final int DEFAULT_MAX_COMMIT = 20000;			// 默认最大批量commit数

	private int        m_maxCommit  = DEFAULT_MAX_COMMIT;
	private Connection m_connection = null;
	private String     m_sql        = null;

	public YCIDao(Connection conn) {
		m_connection = conn;
	}

	public YCIDao(Connection conn, String sql) {
		m_connection = conn;
		m_sql        = sql;
	}

	public boolean SetMaxCommit(int max) {
		if ( max > 0 ) {
			m_maxCommit = max;
			return true;
		}

		return false;
	}

	public void SetSql(String sql) {
		m_sql = sql;
	}

	private void NeedToCommit() throws SQLException {
		if ( !m_connection.getAutoCommit() ) {
			m_connection.commit();
		}
	}

	public void RollBack() throws SQLException {
		m_connection.rollback();
	}

	// 获取地市信息
	public YCIRegion[] GetRegionInfo() throws SQLException {
		Statement stat = m_connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs   = stat.executeQuery(m_sql);

		rs.last();

		int         row_size = rs.getRow();
		YCIRegion[] regions  = new YCIRegion[row_size];

		int index = 0;
		rs.beforeFirst();
		while ( rs.next() ) {
			YCIRegion yc_reg = new YCIRegion(rs.getString(1), rs.getString(2));
			regions[index++] = yc_reg;
		}

		NeedToCommit();
		stat.close();
		return regions;
	}

	// 获取英文表名与中文表名的对应关系Map
	public HashMap<String, String> GetReportTabName() throws SQLException {
		Statement stat = m_connection.createStatement();
		ResultSet rs   = stat.executeQuery(m_sql);

		HashMap<String, String> map_tabname = new HashMap<String, String>();
		while ( rs.next() ) {
			map_tabname.put(rs.getString(1), rs.getString(2));
		}

		NeedToCommit();
		stat.close();
		return map_tabname;
	}

	// 入库报表数据
	public void StoreReportData(ReportFileData[] datas) throws SQLException {
		PreparedStatement p_stat = m_connection.prepareStatement(m_sql);

		int counter = 0;
		ReportFileData report_dat = null;

		for ( int i = 0; i < datas.length; ++i ) {
			report_dat = datas[i];

			for ( int j = 0; j < report_dat.GetColumnSize(); ++j ) {
				p_stat.setObject(j+1, report_dat.GetColumnData(j));
			}
			p_stat.addBatch();

			// 是否达到最大提交数？
			if ( ++counter == m_maxCommit ) {
				counter = 0;

				p_stat.executeBatch();
				m_connection.commit();
			}
		}

		// 最后再提交一次
		if ( counter > 0 ) {
			p_stat.executeBatch();
			m_connection.commit();
		}

		p_stat.close();
	}

	// 报表状态记录是否已存在？
	public boolean HasReportState(YCIReportState state) throws SQLException {
		PreparedStatement p_stat = m_connection.prepareStatement(m_sql);
		p_stat.setObject(1, state.GetTabName_EN());
		p_stat.setObject(2, state.GetDate());
		p_stat.setObject(3, state.GetCity());

		ResultSet rs = p_stat.executeQuery();

		boolean has_state = false;
		while ( rs.next() ) {
			has_state = (rs.getInt(1) > 0);
		}

		NeedToCommit();
		p_stat.close();
		return has_state;
	}

	// 更新报表状态
	public void UpdateReportState(YCIReportState state) throws SQLException {
		PreparedStatement p_stat = m_connection.prepareStatement(m_sql);

		int index = 0;
		p_stat.setObject(++index, state.GetImportTime());
		p_stat.setObject(++index, state.GetTabName_CN());
		p_stat.setObject(++index, state.GetStatus());
		p_stat.setObject(++index, state.GetNum());
		p_stat.setObject(++index, state.GetRecords());
		p_stat.setObject(++index, state.GetDescribe());
		p_stat.setObject(++index, state.GetTabName_EN());
		p_stat.setObject(++index, state.GetDate());
		p_stat.setObject(++index, state.GetCity());
		p_stat.executeUpdate();

		m_connection.commit();
		p_stat.close();
	}

	// 插入报表状态
	public void InsertReportState(YCIReportState state) throws SQLException {
		PreparedStatement p_stat = m_connection.prepareStatement(m_sql);

		int index = 0;
		p_stat.setObject(++index, state.GetImportTime());
		p_stat.setObject(++index, state.GetTabName_CN());
		p_stat.setObject(++index, state.GetTabName_EN());
		p_stat.setObject(++index, state.GetStatus());
		p_stat.setObject(++index, state.GetDate());
		p_stat.setObject(++index, state.GetCity());
		p_stat.setObject(++index, state.GetNum());
		p_stat.setObject(++index, state.GetRecords());
		p_stat.setObject(++index, state.GetDescribe());
		p_stat.executeUpdate();

		m_connection.commit();
		p_stat.close();
	}

}
