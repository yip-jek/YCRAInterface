import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 输入管理
public class YCIInput {

	private Logger   m_logger     = null;
	private String[] m_paths      = null;			// 输入路径
	private File[]   m_pathFiles  = null;

	private LinkedBlockingQueue<InputReportFile> m_queFileList = null;

	public YCIInput(YCIConfig cfg) throws IOException {
		m_paths = cfg.GetInputPaths();

		Init();
	}

	private void Init() throws IOException {
		m_logger      = LogManager.getLogger(Object.class);
		m_queFileList = new LinkedBlockingQueue<InputReportFile>();

		m_pathFiles = new File[m_paths.length];
		for ( int i = 0; i < m_pathFiles.length; ++i ) {
			File path_file = new File(m_paths[i]);
			YCIGlobal.VerifyDirectoryFile(path_file);

			m_pathFiles[i] = path_file;
		}
		m_logger.info("Input file path(s): "+m_pathFiles.length);
	}

	public void TryGetFiles() {
		if ( m_queFileList.isEmpty() ) {
			for ( File path_file : m_pathFiles ) {
				for ( File in_file : path_file.listFiles() ) {
					m_queFileList.add(new InputReportFile(in_file));
				}
			}

			if ( !m_queFileList.isEmpty() ) {
				m_logger.info("[INPUT] Get file(s): "+m_queFileList.size());
			}
		}
	}

	public InputReportFile GetInputReportFile() {
		return m_queFileList.poll();
	}

}
