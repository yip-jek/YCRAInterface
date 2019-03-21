import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 工作类
public class YCIWorker implements Runnable {

	private Logger      m_logger  = null;
	private Thread      m_thread  = null;
	private boolean     m_running = false;
	private WorkManager m_workMgr = null;
	private int         m_id      = 0;
	private YCIDao      m_dao     = null;

	public YCIWorker(WorkManager workMgr, int id, Connection conn) throws SQLException {
		m_workMgr = workMgr;
		m_id      = id;

		Init(conn);
	}

	private void Init(Connection conn) throws SQLException {
		m_logger = LogManager.getLogger(Object.class);
		m_logger.info("Create worker: ID = ["+GetID()+"]");

		ValidateConnection(conn);
		m_dao = new YCIDao(conn);
		m_dao.SetMaxCommit(m_workMgr.GetMaxCommit());
	}

	private void ValidateConnection(Connection conn) throws SQLException {
		if ( conn.isClosed() ) {
			throw new SQLException("The DB connection of Worker [ID="+GetID()+"] is closed!");
		} else {
			m_logger.info("Worker [ID="+GetID()+"] connected the DB.");

			// 禁止自动提交，进行事务操作
			conn.setAutoCommit(false);
		}
	}

	@Override
	public void run() {
		while ( m_running ) {
			try {
				Thread.sleep(YCIGlobal.LOOP_SLEEP_TIME);

				DoJob();
			} catch (InterruptedException | IOException | SQLException e) {
				e.printStackTrace();
				m_logger.error("Worker [ID="+GetID()+"] quit unexpectedly, cause: "+e);
				return;
			}
		}

		m_logger.info("Worker [ID="+GetID()+"] end.");
	}

	private void DoJob() throws InterruptedException, IOException, SQLException {
		YCIJob job = m_workMgr.GetJob(GetID());
		if ( job == null ) {
			Thread.sleep(YCIGlobal.EXTRA_SLEEP_TIME);
			return;
		}

		YCIJob.ResultType type        = null;
		InputReportFile   report_file = job.GetReportFile();

		// 是否有匹配的策略？
		if ( job.HasMatchInfo() ) {
			m_logger.info("[Worker ID="+GetID()+"] Handle file: "+report_file.GetFilePath());

			try {
				if ( job.IsReportFileEmpty() ) {
					m_logger.warn("[Worker ID="+GetID()+"] Empty file: "+report_file.GetFilePath());
				} else {
					StoreData(job);
				}

				// 备份
				type = YCIJob.ResultType.SUCCESS;
				Backup(report_file);
			} catch ( YCIException e ) {
				e.printStackTrace();
				m_logger.error("[Worker ID="+GetID()+"] "+e);

				// 失败
				type = YCIJob.ResultType.FAIL;
				Fail(report_file);
			}
		} else {
			// 没有匹配的策略，文件挂起
			m_logger.warn("[Worker ID="+GetID()+"] No match policy!");
			type = YCIJob.ResultType.SUSPEND;
			Suspend(report_file);
		}

		job.SetResult(type);
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

	public boolean IsThreadAlive() {
		return m_thread.isAlive();
	}

	private void StoreData(YCIJob job) throws YCIException, IOException {
		InputReportFile  report_file  = job.GetReportFile();
		ReportFileData[] report_datas = job.ReadFileData();
		m_logger.info("[Worker ID="+GetID()+"] File \""+report_file.GetFilePath()+"\", read line(s): "+report_datas.length);

		YCIMatchInfo info      = job.GetMatchInfo();
		final String STORE_SQL = info.CreateStoreSql();
		m_logger.info("[Worker ID="+GetID()+"] Store SQL: "+STORE_SQL);
		m_dao.SetSql(STORE_SQL);

		NeedToReplaceRegion(info, report_datas);
		try {
			m_dao.StoreReportData(report_datas);
		} catch (SQLException e) {
			e.printStackTrace();
			m_logger.error("Worker [ID="+GetID()+"] "+e);

			m_logger.warn("Worker [ID="+GetID()+"]: DB rolling back ...");
			try {
				m_dao.RollBack();
			} catch ( SQLException re ) {
				re.printStackTrace();
				m_logger.error("Worker [ID="+GetID()+"] Roll back failed: "+re);
			}

			String       error = null;
			SQLException next  = e.getNextException();
			if ( next != null ) {
				m_logger.error("Worker [ID="+GetID()+"] "+next);
				error = next.toString();
			} else {
				error = e.toString();
			}

			throw new YCIException("Store report data failed! Cause SQLException: "+error);
		}
	}

	// 替换地市信息
	private void NeedToReplaceRegion(YCIMatchInfo info, ReportFileData[] datas) throws YCIException {
		String replace_type = info.TryGetReplaceRegion();
		if ( replace_type != null ) {
			String[] sections       = YCIGlobal.SplitTrim(replace_type, ":", 2);
			int      index          = Integer.parseInt(sections[1]) - 1;
			String   src_region     = datas[0].GetColumnData(index);
			String   replace_region = FindRegionInfo(sections[0], src_region);

			for ( ReportFileData report_data : datas ) {
				report_data.SetColumnData(index, replace_region);
			}
		}
	}

	// 找出对应地市信息
	private String FindRegionInfo(String type, String src) throws YCIException {
		YCIRegion[] regions = m_workMgr.GetRegions();

		if ( type.equals(YCIMatchInfo.REGION_REPLACE_CTOE) ) {		// 地市：中->英
			for ( YCIRegion reg : regions ) {
				if ( src.equals(reg.GetCityName()) ) {
					return reg.GetCityCode();
				}
			}
		} else if ( type.equals(YCIMatchInfo.REGION_REPLACE_ETOC) ) {	// 地市：英->中
			for ( YCIRegion reg : regions ) {
				if ( src.equals(reg.GetCityCode()) ) {
					return reg.GetCityName();
				}
			}
		} else {
			throw new YCIException("Unsupported region replace type: "+type);
		}

		throw new YCIException("Can not find the region information!");
	}

	// 备份
	private void Backup(InputReportFile report_file) throws IOException {
		m_logger.info("[Worker ID="+GetID()+"] Backup file \""+report_file.GetFilePath()+"\" to path: "+m_workMgr.GetBackupPath());
		report_file.MoveTo(m_workMgr.GetBackupPath());
	}

	// 挂起
	private void Suspend(InputReportFile report_file) throws IOException {
		m_logger.warn("[Worker ID="+GetID()+"] Suspend file \""+report_file.GetFilePath()+"\" to path: "+m_workMgr.GetSuspendPath());
		report_file.MoveTo(m_workMgr.GetSuspendPath());
	}

	// 失败
	private void Fail(InputReportFile report_file) throws IOException {
		m_logger.warn("[Worker ID="+GetID()+"] Suspend file \""+report_file.GetFilePath()+"\" to path: "+m_workMgr.GetFailPath());
		report_file.MoveTo(m_workMgr.GetFailPath());
	}

}
