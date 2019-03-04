import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// 数据库访问类
public class YCIDao {

	// 获取地市信息
	public static YCIRegion[] FetchRegionInfo(Connection conn, String sql) throws SQLException {
		Statement stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs   = stat.executeQuery(sql);

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

}
