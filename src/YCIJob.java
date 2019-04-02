import java.io.IOException;
import java.util.ArrayList;

// 工作任务类
public class YCIJob {

	private int             m_workerID   = 0;					// 所属Worker ID
	private InputReportFile m_reportFile = null;				// 输入报表文件
	private YCIMatchInfo    m_matchInfo  = null;				// 匹配信息
	private String          m_cnTabName  = null;				// 中文表名
	private ResultType      m_resultType = ResultType.UNKNOWN;

	// 结果类型
	public enum ResultType {
		UNKNOWN,
		SUCCESS,
		SUSPEND,
		FAIL
	}

	public YCIJob(int id, InputReportFile file, YCIMatchInfo info, String cn_tabname) {
		m_workerID   = id;
		m_reportFile = file;
		m_matchInfo  = info;
		m_cnTabName  = cn_tabname;
	}

	public int GetWorkerID() {
		return m_workerID;
	}

	public InputReportFile GetReportFile() {
		return m_reportFile;
	}

	public YCIMatchInfo GetMatchInfo() {
		return m_matchInfo;
	}

	public String GetCNTabName() {
		return m_cnTabName;
	}

	public String GetJobInfo() {
		StringBuilder buf = new StringBuilder("WORKER_ID=");
		buf.append(m_workerID).append(", FILE_PATH=").append(m_reportFile.GetFilePath());
		buf.append(", FILE_LENGTH=").append(m_reportFile.GetFileLength());

		if ( HasMatchInfo() ) {
			YCIPolicy p = m_matchInfo.GetPolicy();

			buf.append(", DATE=").append(m_matchInfo.GetDate()).append(", CITY=");
			buf.append(m_matchInfo.GetCity()).append(", NUM=").append(m_matchInfo.GetSeq());
			buf.append(", FILE_ENCODING=").append(p.GetSrcFileEncoding()).append(", COLUMN_SIZE=");
			buf.append(p.GetSrcColumnSize()).append(", DES_TABLE=").append(p.GetDesTable());
		} else {
			buf.append(", MATCH_INFO=<NULL>");
		}

		return buf.toString();
	}

	public boolean HasMatchInfo() {
		return (m_matchInfo != null);
	}

	public boolean IsReportFileEmpty() {
		return (m_reportFile.GetFileLength() == 0);
	}

	public ArrayList<ReportFileData> ReadFileData() throws IOException, YCIException {
		YCIPolicy policy       = m_matchInfo.GetPolicy();
		String    reg_separtor = policy.GetSrcRegexSeparator();
		int       column_size  = policy.GetSrcColumnSize();

		ReportFileData            data      = null;
		ArrayList<ReportFileData> list_data = new ArrayList<ReportFileData>();

		m_reportFile.Open(policy.GetSrcFileEncoding());
		while ( (data = m_reportFile.ReadData(reg_separtor, column_size)) != null ) {
			list_data.add(data);
		}

		m_reportFile.Close();
		return list_data;
	}

	// 设置结果
	public void SetResult(ResultType type) {
		m_resultType = type;
	}

	public ResultType GetResult() {
		return m_resultType;
	}

}
