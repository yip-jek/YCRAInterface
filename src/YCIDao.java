import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// 数据库访问类
public class YCIDao {

	public static final int DEFAULT_MAX_COMMIT = 20000;			// 默认最大批量commit数

	private int        m_maxCommit  = DEFAULT_MAX_COMMIT;
	private Connection m_connection = null;
	private String     m_sql        = null;

	public YCIDao(Connection conn, String sql) {
		m_connection = conn;
		m_sql        = sql;
	}

	public void SetMaxCommit(int max) {
		if ( max > 0 ) {
			m_maxCommit = max;
		} else {
			throw new IllegalArgumentException("[DAO] Set max commit failed! Invalid max commit: "+max);
		}
	}

	// 获取地市信息
	public YCIRegion[] FetchRegionInfo() throws SQLException {
		Statement stat = m_connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs   = stat.executeQuery(m_sql);

		rs.last();

		int         row_size = rs.getRow();
		YCIRegion[] regions  = new YCIRegion[row_size];

		int index = 0;
		rs.beforeFirst();
		while ( rs.next() ) {
			YCIRegion yc_reg = new YCIRegion(rs.getString(1));
			regions[index++] = yc_reg;
		}

		stat.close();
		return regions;
	}

	// 入库报表数据
	public void StoreReportData(ReportFileData[] datas) throws SQLException {
		PreparedStatement prepare_stat = m_connection.prepareStatement(m_sql);

		int counter = 0;
		ReportFileData report_dat = null;

		for ( int i = 0; i < datas.length; ++i ) {
			report_dat = datas[i];

			for ( int j = 0; j < report_dat.GetColumnSize(); ++j ) {
				prepare_stat.setObject(j+1, report_dat.GetColumnData(j));
			}
			prepare_stat.addBatch();

			// 是否达到最大提交数？
			if ( ++counter == m_maxCommit ) {
				counter = 0;

				prepare_stat.executeBatch();
				m_connection.commit();
			}
		}

		// 最后再提交一次
		if ( counter > 0 ) {
			prepare_stat.executeBatch();
			m_connection.commit();
		}

		prepare_stat.close();
	}

}
