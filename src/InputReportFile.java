import java.io.File;
import java.io.IOException;

// 输入报表文件类
public class InputReportFile {

	private File   m_file       = null;
	private String m_backupPath = null;

	public InputReportFile(File file, String backup_path) {
		m_file       = file;
		m_backupPath = backup_path;
	}

	public void Backup() throws IOException {
		final String BKFILE_PATH = m_backupPath + File.separator + GetFileName();
		if ( !m_file.renameTo(new File(BKFILE_PATH)) ) {
			throw new IOException("Backup file \""+GetFilePath()+"\" to \""+BKFILE_PATH+"\" failed!");
		}
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
