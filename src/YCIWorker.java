import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 工作类
public class YCIWorker implements Runnable {

	private Logger        m_logger    = null;
	private Thread        m_thread    = null;
	private boolean       m_running   = false;
	private WorkManager   m_workMgr   = null;
	private int           m_id        = 0;
//	private WorkerState   m_state     = WorkerState.INIT;
	private Connection    m_dbConn    = null;

//	// 状态
//	public enum WorkerState {
//		INIT,			// 初始状态
//		START,			// 开始状态
//		IDLE,			// 空闲状态
//		BUSY,			// 忙碌状态
//		STOP,			// 停止状态
//		END				// 结束状态
//	}

	public YCIWorker(WorkManager workMgr, int id, Connection conn) {
		m_workMgr = workMgr;
		m_id      = id;
		m_dbConn  = conn;

		m_logger = LogManager.getLogger(Object.class);
		m_logger.info("Create worker: ID = ["+GetID()+"]");
		m_logger.info("YCIWorker (ID=["+GetID()+"]) connected the DB.");
	}

	@Override
	public void run() {
		while ( m_running ) {
			try {
				Thread.sleep(YCIGlobal.LOOP_SLEEP_TIME);

				DoJob();
			} catch (InterruptedException e) {
				e.printStackTrace();
				m_logger.error("YCIWorker (ID=["+m_id+"]) quit unexpectedly, cause: "+e);
				return;
			}
		}

		m_logger.info("YCIWorker (ID="+m_id+") end.");
	}

	private void DoJob() throws InterruptedException {
		YCIJob job = m_workMgr.GetJob();
		if ( job == null ) {
			Thread.sleep(YCIGlobal.EXTRA_SLEEP_TIME);
			return;
		}

		// TODO: ...
		m_workMgr.FinishJob(job);
	}

	// 开始
	public void Start() {
		if ( !m_running ) {
			m_running = true;

			m_thread = new Thread(this);
			m_thread.start();
		}
	}

	// 准备结束
	public void Prepare2Stop() {
		if ( m_running ) {
			m_running = false;
		}
	}

	// 等待结束
	public void Wait2Stop() throws InterruptedException {
		if ( m_thread.isAlive() ) {
			m_thread.join();
		}
	}

	public int GetID() {
		return m_id;
	}

}
