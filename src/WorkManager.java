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

	public WorkManager(YCIConfig cfg, ConnectionFactory dbConnFactory, PolicyManager policyMgr, YCIInput input) throws SQLException, IOException {
		m_cfg           = cfg;
		m_dbConnFactory = dbConnFactory;
		m_policyMgr     = policyMgr;
		m_input         = input;

		Init(m_dbConnFactory.CreateConnection());
	}

	private void Init(Connection conn) throws SQLException, IOException {
		m_logger = LogManager.getLogger(Object.class);
		m_logger.info("WorkManager connected the DB.");

		m_backupPath  = YCIGlobal.SetFilePath(m_cfg.GetBackupPath());
		m_suspendPath = YCIGlobal.SetFilePath(m_cfg.GetSuspendPath());
		m_failPath    = YCIGlobal.SetFilePath(m_cfg.GetFailPath());
		m_workers     = new YCIWorker[m_cfg.GetWorkers()];
		m_dao         = new YCIDao(conn, m_cfg.GetReportTabNameSql());
		m_mapTabName  = m_dao.GetReportTabName();

		// 禁止自动提交
		conn.setAutoCommit(false);

		InitSql();
	}

	private void InitSql() {
		// SQL: select
		StringBuilder buffer = new StringBuilder("select count(0) from ");
		buffer.append(m_cfg.GetTabReportState()).append(" where TABLE_NAME = ? and DATETIME = ? and CITY = ?");
		m_sqlSelState = buffer.toString();

		// SQL: update
		buffer.setLength(0);
		buffer.append("update ").append(m_cfg.GetTabReportState()).append("set IMPORT_TIME = ?");
		buffer.append(", EXCEL_NAME = ?, STAUTS = ?, NUM = ?, RECORD_NUM = ?, DESCRIBE = ?");
		buffer.append(" where TABLE_NAME = ? and DATETIME = ? and CITY = ?");
		m_sqlUpdState = buffer.toString();

		// SQL: insert
		buffer.setLength(0);
		buffer.append("insert into ").append(m_cfg.GetTabReportState()).append("(IMPORT_TIME, ");
		buffer.append("EXCEL_NAME, TABLE_NAME, STAUTS, DATETIME, CITY, NUM, RECORD_NUM, DESCRIBE)");
		buffer.append(" values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
		m_sqlInsState = buffer.toString();
	}

	public int GetMaxCommit() {
		return m_cfg.GetMaxCommit();
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
	public void FinishJob(YCIJob job) {
		UpdateReportState(new YCIReportState(job));
	}

	// 更新报表状态
	private synchronized void UpdateReportState(YCIReportState state) {
		m_dao.SetSql(m_sqlSelState);
		if ( m_dao.HasReportState(state) ) {
			m_dao.SetSql(m_sqlUpdState);
			m_dao.UpdateReportState(state);
		} else {
			m_dao.SetSql(m_sqlInsState);
			m_dao.InsertReportState(state);
		}
	}

}
