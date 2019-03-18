import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;

// 输入报表文件类
public class InputReportFile {

	private File           m_file      = null;
	private BufferedReader m_reader    = null;
	private int            m_lineCount = 0;

	public InputReportFile(File file) {
		m_file = file;
	}

	public void MoveTo(String path) throws IOException {
		YCIGlobal.MoveFile(m_file, path);
	}

	public String GetFilePath() {
		return m_file.getPath();
	}

	public String GetFileName() {
		return m_file.getName();
	}

	public long GetFileLength() {
		return m_file.length();
	}

	public int GetLintCount() {
		return m_lineCount;
	}

	public int GetLineNumber() throws IOException {
		LineNumberReader line_reader = null;
		line_reader = new LineNumberReader(new FileReader(m_file));
		line_reader.skip(Long.MAX_VALUE);
		line_reader.close();
		return (line_reader.getLineNumber() + 1);
	}

	public void Open(String charset_name) throws FileNotFoundException, UnsupportedEncodingException {
		FileInputStream   file_input   = new FileInputStream(m_file);
		InputStreamReader input_reader = new InputStreamReader(file_input, charset_name);

		m_lineCount = 0;
		m_reader    = new BufferedReader(input_reader);
	}

	public void Close() throws IOException {
		m_reader.close();
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
