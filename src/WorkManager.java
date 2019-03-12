import java.io.IOException;
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
	private String            m_backupPath     = null;
	private String            m_suspendPath    = null;
	private String            m_failPath       = null;
	private Connection        m_conn         = null;
	private String            m_sqlUpdJobState = null;
	private YCIWorker[]       m_workers        = null;

	public WorkManager(YCIConfig cfg, ConnectionFactory dbConnFactory, PolicyManager policyMgr, YCIInput input) throws SQLException, IOException {
		m_cfg           = cfg;
		m_dbConnFactory = dbConnFactory;
		m_conn          = m_dbConnFactory.CreateConnection();
		m_policyMgr     = policyMgr;
		m_input         = input;

		m_logger = LogManager.getLogger(Object.class);
		m_logger.info("WorkManager connected the DB.");

		m_backupPath  = YCIGlobal.SetFilePath(m_cfg.GetBackupPath());
		m_suspendPath = YCIGlobal.SetFilePath(m_cfg.GetSuspendPath());
		m_failPath    = YCIGlobal.SetFilePath(m_cfg.GetFailPath());
		Init();
	}

	private void Init() throws SQLException {
		m_workers = new YCIWorker[m_cfg.GetWorkers()];

		StringBuilder str = new StringBuilder();
		str.append("UPDATE ").append(m_cfg.GetDesReportStateTab()).append(" SET ");
		m_sqlUpdJobState = str.toString();

		// 禁止自动提交
		m_conn.setAutoCommit(false);
	}

	public String GetBackupPath() {
		return m_backupPath;
	}

	public String GetSuspendPath() {
		return m_suspendPath;
	}

	public String GetFailPath() {
		return m_failPath;
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

	// 检查所有工作线程存活状态
	// 一旦有工作线程死亡则返回 false
	public boolean CheckWorkersAlive() {
		int death_count = 0;
		for ( YCIWorker worker : m_workers ) {
			if ( !worker.IsThreadAlive() ) {
				++death_count;
			}
		}

		if ( death_count > 0 ) {
			m_logger.warn("Check workers alive: "+death_count+" worker(s) died (Total: "+m_workers.length+" workers)");
			m_logger.warn("Exiting");
			return false;
		}

		return true;
	}

	// 获取任务
	public YCIJob GetJob() {
		InputReportFile report_file = m_input.GetInputReportFile();
		if ( report_file == null ) {
			return null;
		}

		YCIMatchInfo info = m_policyMgr.GetMatch(report_file.GetFileName());
		return new YCIJob(report_file, info);
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
