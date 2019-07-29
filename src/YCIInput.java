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

	private LinkedBlockingQueue<InputReportFile> m_queFound   = null;		// 找到的文件列表
	private LinkedBlockingQueue<InputReportFile> m_queProcess = null;		// 正在处理的文件列表

	public YCIInput(YCIConfig cfg) throws IOException {
		m_paths = cfg.GetInputPaths();

		Init();
	}

	private void Init() throws IOException {
		m_logger     = LogManager.getLogger(Object.class);
		m_queFound   = new LinkedBlockingQueue<InputReportFile>();
		m_queProcess = new LinkedBlockingQueue<InputReportFile>();

		m_pathFiles = new File[m_paths.length];
		for ( int i = 0; i < m_pathFiles.length; ++i ) {
			File path_file = new File(m_paths[i]);
			YCIGlobal.VerifyDirectoryFile(path_file);

			m_pathFiles[i] = path_file;
		}
		m_logger.info("Input file path(s): "+m_pathFiles.length);
	}

	public void TryGetFiles() {
		// No file found or no file in processing
		if ( m_queProcess.isEmpty() && m_queFound.isEmpty() ) {
			for ( File path_file : m_pathFiles ) {
				for ( File in_file : path_file.listFiles() ) {
					m_queFound.add(new InputReportFile(in_file));
				}
			}

			if ( !m_queFound.isEmpty() ) {
				m_logger.info("[INPUT] Get file(s): "+m_queFound.size());
			}
		}
	}

	public InputReportFile GetInputFile() {
		InputReportFile report_file = m_queFound.poll();
		if ( report_file != null ) {
			m_queProcess.add(report_file);
			m_logger.info("[INPUT] Process file: "+report_file.GetFilePath());
		}

		return report_file;
	}

	public void DoneInputFile(InputReportFile file) throws YCIException {
		if ( !m_queProcess.remove(file) ) {
			throw new YCIException("No such input report file: "+file.GetFilePath());
		}
	}

}
