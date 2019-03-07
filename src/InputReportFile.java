import java.io.File;

// 输入报表文件类
public class InputReportFile {

	private File   m_file       = null;
	private String m_backupPath = null;

	public InputReportFile(File file, String backup_path) {
		m_file       = file;
		m_backupPath = backup_path;
	}

	public void Backup() {
	}

	public String GetFileName() {
		return m_file.getName();
	}

	public ReportFileData GetData() {
		return null;
	}

}
