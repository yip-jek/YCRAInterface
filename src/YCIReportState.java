
// 报表状态类
public class YCIReportState {

	private String m_importTime = null;			// 导入时间
	private String m_cnTabName  = null;			// 中文报表名
	private String m_enTabName  = null;			// 英文报表名
	private String m_status     = null;			// 状态
	private String m_date       = null;			// 账期
	private String m_city       = null;			// 地市
	private String m_num        = null;			// 批次
	private String m_records    = null;			// 记录数
	private String m_describe   = null;			// 描述信息

	public YCIReportState(YCIJob job) {
	}

	public String GetImportTime() {
		return m_importTime;
	}

	public String GetTabName_CN() {
		return m_cnTabName;
	}

	public String GetTabName_EN() {
		return m_enTabName;
	}

	public String GetStatus() {
		return m_status;
	}

	public String GetDate() {
		return m_date;
	}

	public String GetCity() {
		return m_city;
	}

	public String GetNum() {
		return m_num;
	}

	public String GetRecords() {
		return m_records;
	}

	public String GetDescribe() {
		return m_describe;
	}

}
