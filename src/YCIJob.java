import java.io.IOException;

// 工作任务类
public class YCIJob {

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

	public YCIJob(InputReportFile file, YCIMatchInfo info, String cn_tabname) {
		m_reportFile = file;
		m_matchInfo  = info;
		m_cnTabName  = cn_tabname;
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
		StringBuilder buf = new StringBuilder("FILE_PATH=");
		buf.append(m_reportFile.GetFilePath()).append(", FILE_LENGTH=").append(m_reportFile.GetFileLength());

		if ( HasMatchInfo() ) {
			YCIPolicy p = m_matchInfo.GetPolicy();
			buf.append(", POLICY_ID=").append(p.GetID()).append(", SRC_FILE=").append(p.GetSrcFile());
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

	public ReportFileData[] ReadFileData() throws IOException, YCIException {
		YCIPolicy        policy       = m_matchInfo.GetPolicy();
		String           reg_separtor = policy.GetSrcRegexSeparator();
		int              column_size  = policy.GetSrcColumnSize();
		ReportFileData   data         = null;
		ReportFileData[] report_datas = new ReportFileData[m_reportFile.GetLineNumber()];

		m_reportFile.Open(policy.GetSrcFileEncoding());
		for ( int i = 0; (data = m_reportFile.ReadData(reg_separtor, column_size)) != null; ++i ) {
			report_datas[i] = data;
		}

		m_reportFile.Close();
		return report_datas;
	}

	// 设置结果
	public void SetResult(ResultType type) {
		m_resultType = type;
	}

	public ResultType GetResult() {
		return m_resultType;
	}

}
