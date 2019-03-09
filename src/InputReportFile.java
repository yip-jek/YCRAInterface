import java.io.File;
import java.io.IOException;

// 输入报表文件类
public class InputReportFile {

	private File m_file = null;

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

	public ReportFileData GetData() {
		return null;
	}

}
