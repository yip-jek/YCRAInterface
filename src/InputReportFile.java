import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

// 输入报表文件类
public class InputReportFile {

	private File           m_file      = null;
	private String         m_path      = null;
	private String         m_name      = null;
	private long           m_length    = 0;
	private BufferedReader m_reader    = null;
	private int            m_lineCount = 0;

	public InputReportFile(File file) {
		m_file = file;

		Init();
	}

	private void Init() {
		m_path   = m_file.getPath();
		m_name   = m_file.getName();
		m_length = m_file.length();
	}

	public void MoveTo(String path) throws IOException {
		YCIGlobal.MoveFile(m_file, path);
	}

	public String GetFilePath() {
		return m_path;
	}

	public String GetFileName() {
		return m_name;
	}

	public long GetFileLength() {
		return m_length;
	}

	public int GetLineCount() {
		return m_lineCount;
	}

	public void Open(String charset_name) throws IOException {
		FileInputStream   file_input   = new FileInputStream(m_file);
		InputStreamReader input_reader = new InputStreamReader(file_input, charset_name);

		m_lineCount = 0;
		m_reader    = new BufferedReader(input_reader);

		SkipHeaderLine();
	}

	public void Close() throws IOException {
		m_reader.close();
	}

	private void SkipHeaderLine() throws IOException {
		// Skip the header line
		m_reader.readLine();
	}

	public ReportFileData ReadData(String regex_separator, int column_size) throws IOException, YCIException {
		String line = m_reader.readLine();
		if ( line != null ) {
			ReportFileData report_data = new ReportFileData(line, regex_separator);

			++m_lineCount;
			if ( !report_data.Verify(column_size) ) {
				throw new YCIException("File \""+m_file.getPath()+"\" line "+m_lineCount+", column size does not "
						+ "match: Data_col_size="+report_data.GetColumnSize()+", Policy_col_size="+column_size);
			}

			return report_data;
		}

		return null;
	}

}
