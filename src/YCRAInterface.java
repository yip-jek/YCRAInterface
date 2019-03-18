import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class YCRAInterface {

	private String m_log4j2file = null;
	private String m_cfgfile    = null;

	private Logger            m_logger        = null;
	private YCIConfig         m_yciconfig     = null;			// 配置类
	private PolicyManager     m_policyMgr     = null;
	private YCSignal          m_ycsignal      = null;			// 信号类
	private ConnectionFactory m_dbConnFactory = null;
	private YCIInput          m_input         = null;			// 输入
	private WorkManager       m_workMgr       = null;			// 工作线程管理

	public YCRAInterface(String log4j2_file, String cfg_file) {
		m_log4j2file = log4j2_file;
		m_cfgfile    = cfg_file;
	}

	public Logger GetLogger() {
		return m_logger;
	}

	public void Run() throws IOException, InterruptedException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Init();
		Exec();
	}

	// 初始化
	private void Init() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		CreateLogger();
		LoadConfig();
		CreateDBFactory();
		InitPolicy();
		InitSignal();
		PrepareInput();
		PrepareWorker();

		m_logger.info("Init OK.");
	}

	// 创建日志
	private void CreateLogger() throws IOException {
		FileInputStream     file_is = new FileInputStream(m_log4j2file);
		BufferedInputStream buf_is  = new BufferedInputStream(file_is);
		ConfigurationSource cfg_src = new ConfigurationSource(buf_is);

		Configurator.initialize(null, cfg_src);
		m_logger = LogManager.getLogger(Object.class);

		OutputLogHeader();
	}

	// 输出日志头信息
	private void OutputLogHeader() {
		m_logger.info("PID=["+YCIGlobal.GetProcessID()+"]");
		m_logger.info(this.getClass().getName()+" "+YCIGlobal.VERSION);
	}

	// 载入配置信息
	private void LoadConfig() throws IOException {
		Properties prop_cfg = new Properties();
		prop_cfg.load(new FileInputStream(m_cfgfile));

		m_yciconfig = new YCIConfig(prop_cfg);
		m_logger.info("Load config OK.");
	}

	// 载入策略信息
	private void InitPolicy() throws FileNotFoundException, IOException, SQLException {
		Connection  conn    = m_dbConnFactory.CreateConnection();
		YCIDao      dao     = new YCIDao(conn, m_yciconfig.GetRegionSql());
		YCIRegion[] regions = dao.GetRegionInfo();
		m_dbConnFactory.ReleaseConnection(conn);
		m_logger.info("Get the size of region(s): "+regions.length);

		m_policyMgr = new PolicyManager(m_yciconfig.GetPolicy(), regions);
	}

	// 初始化信号
	private void InitSignal() {
		m_ycsignal = new YCSignal();

		m_logger.info("Set signal OK.");
	}

	// 输入的准备
	private void PrepareInput() throws IOException {
		m_input = new YCIInput(m_yciconfig);

		m_logger.info("Input is ready.");
	}

	// 创建数据库连接工厂
	private void CreateDBFactory() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		m_dbConnFactory = new ConnectionFactory(m_yciconfig);

		m_logger.info("ConnectionFactory is created.");
	}

	// 准备工作线程
	private void PrepareWorker() throws SQLException, IOException {
		m_workMgr = new WorkManager(m_yciconfig, m_dbConnFactory, m_policyMgr, m_input);

		m_logger.info("WorkManager is ready.");
	}

	// 执行
	private void Exec() throws InterruptedException, SQLException {
		Begin();

		do {
			Thread.sleep(YCIGlobal.LOOP_SLEEP_TIME);

			Do();
		} while ( !m_ycsignal.IsQuitSignal() && m_workMgr.VerifyWorkersAlive() );

		End();
	}

	private void Begin() throws SQLException {
		m_workMgr.StartAll();

		m_logger.info("Created the size of connections: "+m_dbConnFactory.GetConnectionSize());
	}

	private void Do() {
		m_input.TryGetFiles();
		m_workMgr.Show();
	}

	private void End() throws InterruptedException, SQLException {
		m_workMgr.Prepare2StopAll();
		m_workMgr.Wait2StopAll();

		m_logger.info("Release all connections ...");
		m_dbConnFactory.ReleaseAllConnection();
	}

	public static void main(String[] args) {

		if ( args.length != 2 ) {
			System.out.println("[ERROR] Incorrect parameters!");
			return;
		}

		YCRAInterface yci = new YCRAInterface(args[0], args[1]);

		try {
			yci.Run();
		} catch (IOException | InterruptedException | InstantiationException | IllegalAccessException
			| ClassNotFoundException | SQLException e) {
			e.printStackTrace();

			if ( yci.GetLogger() != null ) {
				yci.GetLogger().error(e);
			}
			return;
		} catch ( Exception e ) {
			e.printStackTrace();

			if ( yci.GetLogger() != null ) {
				yci.GetLogger().error(e);
			}
			return;
		}

		if ( yci.GetLogger() != null ) {
			yci.GetLogger().info(YCRAInterface.class.getName()+" quit!");
		}
	}
}
