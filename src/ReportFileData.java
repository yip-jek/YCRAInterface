
// 报表数据类
public class ReportFileData {

	private String[] m_data = null;

	public ReportFileData(String line, String regex_separator) {
		SplitData(line, regex_separator);
	}

	private void SplitData(String line, String regex_separator) {
		m_data = YCIGlobal.SplitTrim(line, regex_separator, -1);
	}

	public boolean Verify(int column_size) {
		return (m_data.length == column_size);
	}

	public int GetColumnSize() {
		return m_data.length;
	}

	public String GetColumnData(int index) {
		return m_data[index];
	}

	public void SetColumnData(int index, String dat) {
		m_data[index] = dat;
	}

}
