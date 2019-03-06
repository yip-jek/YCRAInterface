import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

// 输入管理
public class YCIInput {

	private String[] m_paths      = null;			// 输入路径
	private String   m_backupPath = null;			// 备份路径
	private File[]   m_pathFiles  = null;

	private LinkedBlockingQueue<InputReportFile> m_queFileList = null;

	public YCIInput(YCIConfig cfg) throws IOException {
		m_paths      = cfg.GetInputPaths();
		m_backupPath = cfg.GetBackupPath();

		Init();
	}

	private void Init() throws IOException {
		m_pathFiles = new File[m_paths.length];
		for ( int i = 0; i < m_pathFiles.length; ++i ) {
			File path_file = new File(m_paths[i]);
			if ( !path_file.exists() ) {
				throw new IOException("Non-existing path: "+m_paths[i]);
			}
			if ( !path_file.isDirectory() ) {
				throw new IllegalArgumentException("Non-directory path: "+m_paths[i]);
			}

			m_pathFiles[i] = path_file;
		}

		m_queFileList = new LinkedBlockingQueue<InputReportFile>();
	}

	public void Exec() {
		if ( m_queFileList.isEmpty() ) {
			for ( File path_file : m_pathFiles ) {
				for ( File in_file : path_file.listFiles() ) {
					m_queFileList.add(new InputReportFile(in_file, m_backupPath));
				}
			}
		}
	}

	public InputReportFile GetInputReportFile() {
		return m_queFileList.poll();
	}

}
