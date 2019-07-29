import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 工作线程管理类
public class WorkManager {

	private Logger                  m_logger        = null;
	private YCIConfig               m_cfg           = null;
	private ConnectionFactory       m_dbConnFactory = null;
	private PolicyManager           m_policyMgr     = null;
	private YCIInput                m_input         = null;
	private String                  m_backupPath    = null;
	private String                  m_suspendPath   = null;
	private String                  m_failPath      = null;
	private String                  m_sqlSelState   = null;
	private String                  m_sqlUpdState   = null;
	private String                  m_sqlInsState   = null;
	private HashMap<String, String> m_mapTabName    = null;
	private YCIDao                  m_dao           = null;
	private YCIWorker[]             m_workers       = null;

	private long    m_currMilliTime    = 0;			// 当前毫秒数
	private Integer m_jobGetCounter    = 0;			// 获取Job计数
	private Integer m_jobFinishCounter = 0;			// 完成Job计数

	public WorkManager(YCIConfig cfg, ConnectionFactory dbConnFactory, PolicyManager policyMgr, YCIInput input) throws SQLException, IOException {
		m_cfg           = cfg;
		m_dbConnFactory = dbConnFactory;
		m_policyMgr     = policyMgr;
		m_input         = input;

		Init();
	}

	private void Init() throws SQLException, IOException {
		m_logger = LogManager.getLogger(Object.class);

		m_backupPath  = YCIGlobal.SetFilePath(m_cfg.GetBackupPath());
		m_suspendPath = YCIGlobal.SetFilePath(m_cfg.GetSuspendPath());
		m_failPath    = YCIGlobal.SetFilePath(m_cfg.GetFailPath());
		m_workers     = new YCIWorker[m_cfg.GetWorkers()];

		InitDao();
		InitSql();

		m_currMilliTime = YCIGlobal.CurrentMilliTime();
	}

	private void InitDao() throws SQLException {
		Connection conn = m_dbConnFactory.CreateConnection();
		ValidateConnection(conn);

		m_dao        = new YCIDao(conn, m_cfg.GetReportTabNameSql());
		m_mapTabName = m_dao.GetReportTabName();
		m_logger.info("Get the size of report table name(s): "+m_mapTabName.size());
	}

	private void ValidateConnection(Connection conn) throws SQLException {
		if ( conn.isClosed() ) {
			throw new SQLException("The DB connection of workmanager is closed!");
		} else {
			m_logger.info("WorkManager connected the DB.");

			// 禁止自动提交
			conn.setAutoCommit(false);
		}
	}

	private void InitSql() {
		// SQL: select
		StringBuilder buffer = new StringBuilder("select count(0) from ");
		buffer.append(m_cfg.GetTabReportState()).append(" where TABLE_NAME = ? and DATETIME = ? and CITY = ?");
		m_sqlSelState = buffer.toString();
		m_logger.info("[WorkManager] Select state SQL: "+m_sqlSelState);

		// SQL: update
		buffer.setLength(0);
		buffer.append("update ").append(m_cfg.GetTabReportState()).append(" set IMPORT_TIME = ?");
		buffer.append(", EXCEL_NAME = ?, STAUTS = ?, NUM = ?, RECORD_NUM = ?, DESCRIBE = ?");
		buffer.append(" where TABLE_NAME = ? and DATETIME = ? and CITY = ?");
		m_sqlUpdState = buffer.toString();
		m_logger.info("[WorkManager] Update state SQL: "+m_sqlUpdState);

		// SQL: insert
		buffer.setLength(0);
		buffer.append("insert into ").append(m_cfg.GetTabReportState()).append("(IMPORT_TIME, ");
		buffer.append("EXCEL_NAME, TABLE_NAME, STAUTS, DATETIME, CITY, NUM, RECORD_NUM, DESCRIBE)");
		buffer.append(" values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
		m_sqlInsState = buffer.toString();
		m_logger.info("[WorkManager] Insert state SQL: "+m_sqlInsState);
	}

	private void CountGetJob() {
		synchronized(m_jobGetCounter) {
			++m_jobGetCounter;
		}
	}

	private void CountFinishJob() {
		synchronized(m_jobFinishCounter) {
			++m_jobFinishCounter;
		}
	}

	public int GetMaxCommit() {
		return m_cfg.GetMaxCommit();
	}

	public YCIRegion[] GetRegions() {
		return m_policyMgr.GetRegions();
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

	public void Show() {
		if ( YCIGlobal.CurrentIntervalTime(m_currMilliTime) >= YCIGlobal.INTERVAL_TIME ) {
			m_currMilliTime = YCIGlobal.CurrentMilliTime();

			StringBuilder buf = new StringBuilder("[WorkManager] GetJob(s)=");
			buf.append(m_jobGetCounter).append(", FinishJob(s)=").append(m_jobFinishCounter);
			buf.append(", Worker(s)=").append(m_workers.length).append(", Connection(s)=");
			buf.append(m_dbConnFactory.GetConnectionSize()).append(", Policy(s)=").append(m_policyMgr.GetPolicySize());
			m_logger.info(buf.toString());
		}
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
			m_logger.info("Worker [ID="+worker.GetID()+"] stopped!");
		}

		m_logger.info("All workers stopped.");
	}

	// 检查所有工作线程存活状态
	// 一旦有工作线程死亡则返回 false
	public boolean VerifyWorkersAlive() {
		int death_count = 0;
		for ( YCIWorker worker : m_workers ) {
			if ( !worker.IsThreadAlive() ) {
				++death_count;
			}
		}

		if ( death_count > 0 ) {
			m_logger.warn("Verify workers alive: "+death_count+" worker(s) died (Total: "+m_workers.length+" workers)");
			m_logger.warn("Exiting");
			return false;
		}

		return true;
	}

	// 获取任务
	public YCIJob GetJob(int worker_id) {
		InputReportFile report_file = m_input.GetInputFile();
		if ( report_file == null ) {
			return null;
		}

		YCIJob       job  = null;
		YCIMatchInfo info = m_policyMgr.GetMatch(report_file.GetFileName());
		if ( info != null ) {
			String cn_tname = m_mapTabName.get(info.GetPolicy().GetDesTable());
			job = new YCIJob(worker_id, report_file, info, cn_tname);
		} else {
			job = new YCIJob(worker_id, report_file, info, null);
		}

		CountGetJob();
		return job;
	}

	// 工作任务
	public void FinishJob(YCIJob job) throws SQLException, YCIException {
		YCIJob.ResultType type = job.GetResult();

		// Done with the input report file
		m_input.DoneInputFile(job.GetReportFile());

		if ( type == YCIJob.ResultType.UNKNOWN ) {
			m_logger.error("Finish job: ResultType="+type+" ["+job.GetJobInfo()+"]");
		} else if ( type == YCIJob.ResultType.SUSPEND ) {
			m_logger.warn("Finish job: ResultType="+type+" ["+job.GetJobInfo()+"]");
		} else {
			m_logger.info("Finish job: ResultType="+type+" ["+job.GetJobInfo()+"]");

			YCIReportState state = new YCIReportState(job);
			try {
				UpdateState(state);
			} catch ( SQLException e ) {
				e.printStackTrace();
				m_logger.warn("[WorkManager] DB rolling back ...");

				try {
					m_dao.RollBack();
				} catch ( SQLException re ) {
					re.printStackTrace();
					m_logger.error("[WorkManager] Roll back failed: "+re);
				}

				throw e;
			}
		}

		CountFinishJob();
	}

	// 更新状态
	private synchronized void UpdateState(YCIReportState state) throws SQLException {
		m_dao.SetSql(m_sqlSelState);
		if ( m_dao.HasReportState(state) ) {
			m_logger.info("Update report state: "+state.GetInfo());

			m_dao.SetSql(m_sqlUpdState);
			m_dao.UpdateReportState(state);
		} else {
			m_logger.info("Insert report state: "+state.GetInfo());

			m_dao.SetSql(m_sqlInsState);
			m_dao.InsertReportState(state);
		}
	}

}
