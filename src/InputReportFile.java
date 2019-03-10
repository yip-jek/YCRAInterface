import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

// 输入报表文件类
public class InputReportFile {

	private File           m_file   = null;
	private BufferedReader m_reader = null;

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

	public void Open(String charset_name) throws FileNotFoundException, UnsupportedEncodingException {
		FileInputStream   file_input   = new FileInputStream(m_file);
		InputStreamReader input_reader = new InputStreamReader(file_input, charset_name);
		m_reader = new BufferedReader(input_reader);
	}

	public void Close() throws IOException {
		m_reader.close();
	}

	public ReportFileData ReadData() throws IOException {
		String line = m_reader.readLine();
		if ( line != null ) {
			return new ReportFileData(line);
		}

		return null;
	}

}
