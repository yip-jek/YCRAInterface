import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// ¹¤×÷Àà
public class YCIWorker implements Runnable {

	private static final int EX_SLEEP_TIME = 60*1000;

	private Logger        m_logger    = null;
	private Thread        m_thread    = null;
	private boolean       m_running   = false;
	private WorkManager   m_workMgr   = null;
	private int           m_id        = 0;
//	private WorkerState   m_state     = WorkerState.INIT;
	private Connection    m_dbConn    = null;

//	// ×´Ì¬
//	public enum WorkerState {
//		INIT,			// ³õÊ¼×´Ì¬
//		START,			// ¿ªÊ¼×´Ì¬
//		IDLE,			// ¿ÕÏÐ×´Ì¬
//		BUSY,			// Ã¦Âµ×´Ì¬
//		STOP,			// Í£Ö¹×´Ì¬
//		END				// ½áÊø×´Ì¬
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
				Thread.sleep(1000);

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
		if ( null == job ) {
			Thread.sleep(EX_SLEEP_TIME);
			return;
		}

		// TODO: ...
		m_workMgr.FinishJob(job);
	}

	// ¿ªÊ¼
	public void Start() {
		if ( !m_running ) {
			m_running = true;

			m_thread = new Thread(this);
			m_thread.start();
		}
	}

	// ×¼±¸½áÊø
	public void Prepare2Stop() {
		if ( m_running ) {
			m_running = false;
		}
	}

	// µÈ´ý½áÊø
	public void Wait2Stop() throws InterruptedException {
		if ( m_thread.isAlive() ) {
			m_thread.join();
		}
	}

	public int GetID() {
		return m_id;
	}

}
