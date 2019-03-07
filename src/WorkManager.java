import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 工作线程管理类
public class WorkManager {

	private Logger            m_logger         = null;
	private YCIConfig         m_cfg            = null;
	private ConnectionFactory m_dbConnFactory  = null;
	private PolicyManager     m_policyMgr      = null;
	private YCIInput          m_input          = null;
	private Connection        m_dbConn         = null;
	private String            m_sqlUpdJobState = null;
	private YCIWorker[]       m_workers        = null;

	public WorkManager(YCIConfig cfg, ConnectionFactory dbConnFactory, PolicyManager policyMgr, YCIInput input) throws SQLException {
		m_cfg           = cfg;
		m_dbConnFactory = dbConnFactory;
		m_dbConn        = m_dbConnFactory.CreateConnection();
		m_policyMgr     = policyMgr;
		m_input         = input;

		m_logger = LogManager.getLogger(Object.class);
		m_logger.info("WorkManager connected the DB.");

		Init();
	}

	private void Init() {
		m_workers = new YCIWorker[m_cfg.GetWorkers()];

		StringBuffer str = new StringBuffer();
		str.append("UPDATE ").append(m_cfg.GetDesReportStateTab()).append(" SET ");
		m_sqlUpdJobState = str.toString();
	}

	public void StartAll() throws SQLException {
		int       worker_id   = 0;
		final int MAX_WORKERS = m_cfg.GetWorkers();

		for ( int i = 0; i < MAX_WORKERS; ++i ) {
			Connection db_conn    = m_dbConnFactory.CreateConnection();
			YCIWorker  yci_worker = new YCIWorker(this, ++worker_id, db_conn);

			m_workers[i] = yci_worker;
			yci_worker.Start();
		}

		m_logger.info("All workers started.");
	}

	public void Prepare2StopAll() {
		for ( YCIWorker worker : m_workers ) {
			worker.Prepare2Stop();
			m_logger.info("Worker [ID="+worker.GetID()+"] is preparing to stop ...");
		}
	}

	public void Wait2StopAll() throws InterruptedException {
		for ( YCIWorker worker : m_workers ) {
			worker.Wait2Stop();
			m_logger.info("Worker [ID="+worker.GetID()+"] is stopped!");
		}

		m_logger.info("All workers stopped.");
	}

	// 获取任务
	public YCIJob GetJob() {
		InputReportFile report_file = m_input.GetInputReportFile();
		if ( report_file == null ) {
			return null;
		}

		YCIJob job = new YCIJob();
		job.report_file = report_file;
		job.policy      = m_policyMgr.GetMatchPolicy(report_file.GetFileName());
		return job;
	}

	// 工作任务
	public synchronized void FinishJob(YCIJob job) {
		UpdateJobState(job);
	}

	// 更新任务状态
	private void UpdateJobState(YCIJob job) {
		;
	}

}
