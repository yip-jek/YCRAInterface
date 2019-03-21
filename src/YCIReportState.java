
// 报表状态类
public class YCIReportState {

	public static final String IMPORT_TIME_FORMAT = "yyyyMMddHHmmss";		// 导入时间格式
	public static final String UNKNOWN_STATE      = "-1";					// 未知状态
	public static final String UNKNOWN_STATE_DESC = "未知状态";				// 未知状态描述

	// 报表状态
	public enum ReportState {
		NOT_STORE,					// 0-未入库
		STORING,					// 1-正在入库
		STORE_FAILED,				// 2-入库失败
		STORE_SUCCEED				// 3-入库完成
	}

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
		Init(job);
	}

	private void Init(YCIJob job) {
		InputReportFile input_file = job.GetReportFile();
		YCIMatchInfo info   = job.GetMatchInfo();
		YCIPolicy    policy = info.GetPolicy();

		m_importTime = YCIGlobal.CurrentDateTime(IMPORT_TIME_FORMAT);
		m_cnTabName  = YCIGlobal.NullToEmpty(job.GetCNTabName());
		m_enTabName  = YCIGlobal.NullToEmpty(policy.GetDesTable());
		m_date       = YCIGlobal.NullToEmpty(info.GetDate());
		m_city       = YCIGlobal.NullToEmpty(info.GetCity());
		m_num        = String.valueOf(info.GetSeq());
		m_records    = String.valueOf(input_file.GetLineCount());

		YCIJob.ResultType type = job.GetResult();
		if ( type == YCIJob.ResultType.SUCCESS ) {
			m_status   = String.valueOf(ReportState.STORE_SUCCEED.ordinal());
			m_describe = ReportState.STORE_SUCCEED.toString();
		} else if ( type == YCIJob.ResultType.FAIL ) {
			m_status   = String.valueOf(ReportState.STORE_FAILED.ordinal());
			m_describe = ReportState.STORE_FAILED.toString();
		} else {	// 未知状态
			m_status   = UNKNOWN_STATE;
			m_describe = UNKNOWN_STATE_DESC;
		}
	}

	public String GetInfo() {
		StringBuilder buf = new StringBuilder("IMPORT_TIME=");
		buf.append(m_importTime).append(", TABLE_NAME=").append(m_enTabName);
		buf.append(", STATUS=").append(m_status).append(", DATE=").append(m_date);
		buf.append(", CITY=").append(m_city).append(", NUM=").append(m_num);
		buf.append(", RECORDS=").append(m_records).append(", DESCRIBE=").append(m_describe);

		return buf.toString();
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
