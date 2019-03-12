import java.io.IOException;

// 工作任务类
public class YCIJob {

	private InputReportFile m_reportFile = null;				// 输入报表文件
	private YCIMatchInfo    m_matchInfo  = null;				// 匹配信息
	private ResultType      m_resultType = ResultType.UNKNOWN;

	// 结果类型
	public enum ResultType {
		UNKNOWN,
		SUCCESS,
		SUSPEND,
		FAIL
	}

	public YCIJob(InputReportFile file, YCIMatchInfo info) {
		m_reportFile = file;
		m_matchInfo  = info;
	}

	public InputReportFile GetReportFile() {
		return m_reportFile;
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

	public String GetStoreSql() {
		return m_matchInfo.CreateStoreSql();
	}

	// 设置结果
	public void SetResult(ResultType type) {
		m_resultType = type;
	}

	public ResultType GetResult() {
		return m_resultType;
	}

}
