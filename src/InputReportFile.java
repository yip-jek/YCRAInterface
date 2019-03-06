import java.io.File;

// 输入报表文件类
public class InputReportFile {

	private String m_backupPath = null;
	private File   m_file       = null;

	public InputReportFile(File file, String backup_path) {
		m_backupPath = backup_path;
		m_file       = file;
	}

	public void Open() {
	}

	public void Close() {
	}

	public void Backup() {
	}

	public String GetFileName() {
		return m_file.getName();
	}

	public int GetDataSize() {
		return 0;
	}

	public ReportFileData GetOneData(int index) {
		return null;
	}

}
