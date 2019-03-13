import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 配置类
public class YCIConfig {

	private static final String YCI_WORKERS          = "YCI.Workers";
	private static final String DB_DRIVER            = "DB.Driver";
	private static final String DB_URL               = "DB.URL";
	private static final String DB_USER              = "DB.User";
	private static final String DB_PASSWORD          = "DB.Password";
	private static final String DB_MAX_COMMIT        = "DB.MaxCommit";
	private static final String DES_REGION_SQL       = "DES.RegionSQL";
	private static final String DES_TAB_NAME_SQL     = "DES.ReportTabNameSQL";
	private static final String DES_TAB_REPORT_STATE = "DES.TabReportState";
//	private static final String HDFS_HOST            = "HDFS.host";
//	private static final String HDFS_PORT            = "HDFS.port";
//	private static final String HDFS_PATH            = "HDFS.path";
//	private static final String HIVE_ZKQUORUM        = "HIVE.zkquorum";
//	private static final String HIVE_KRB5            = "HIVE.krb5";
//	private static final String HIVE_USER_KEYTAB     = "HIVE.user_keytab";
//	private static final String HIVE_PRINCIPAL       = "HIVE.principal";
//	private static final String HIVE_JAAS            = "HIVE.jaas";
//	private static final String HIVE_LOCATION        = "HIVE.location";
	private static final String YCI_POLICY           = "YCI.Policy";
	private static final String INPUT_PATH_SIZE      = "INPUT.path_size";
	private static final String INPUT_PATH_PREFIX    = "INPUT.path_";
	private static final String OUTPUT_BACKUP_PATH   = "OUTPUT.backup_path";
	private static final String OUTPUT_SUSPEND_PATH  = "OUTPUT.suspend_path";
	private static final String OUTPUT_FAIL_PATH     = "OUTPUT.fail_path";

	private Properties m_propCfg        = null;
	private int        m_workers        = 0;
	private String     m_dbDriver       = null;
	private String     m_dbURL          = null;				// 数据库URL
	private String     m_dbUsr          = null;				// 数据库用户名
	private String     m_dbPwd          = null;				// 数据库密码
	private int        m_maxCommit      = 0;				// 最大提交数
	private String     m_sqlRegion      = null;				// 地市信息SQL语句
	private String     m_sqlTabName     = null;				// 报表名称SQL语句
	private String     m_tabReportState = null;				// 报表状态表
//	private String     m_hdfsHost       = null;				// HDFS主机
//	private int        m_hdfsPort       = 0;				// HDFS端口
//	private String     m_hdfsPath       = null;				// HDFS路径
//	private String     m_hiveZkquorum   = null;
//	private String     m_hiveKrb5       = null;
//	private String     m_hiveUserkey    = null;
//	private String     m_hivePrincipal  = null;
//	private String     m_hiveJaas       = null;
//	private String     m_hiveLocation   = null;				// HIVE位置
	private String     m_policy         = null;				// 策略
	private String[]   m_paths          = null;				// 输入路径
	private String     m_backupPath     = null;				// 备份路径
	private String     m_suspendPath    = null;				// 挂起路径
	private String     m_failPath       = null;				// 失败路径

	public YCIConfig(Properties prop) throws IOException {
		m_propCfg = prop;

		ReadWorkConfig();
		ReadDBConfig();
//		ReadHdfsConfig();
//		ReadHiveConfig();
		ReadDesConfig();
		ReadPolicyConfig();
		ReadInputConfig();
		ReadOutputConfig();

		ShowConfig();
	}

	private void ReadWorkConfig() throws IOException {
		m_workers = YCIGlobal.ReadUIntProperty(m_propCfg, YCI_WORKERS);
	}

	// 读取数据库配置
	private void ReadDBConfig() throws IOException {
		m_dbDriver = YCIGlobal.ReadProperty(m_propCfg, DB_DRIVER);
		m_dbURL    = YCIGlobal.ReadProperty(m_propCfg, DB_URL);
		m_dbUsr    = YCIGlobal.ReadProperty(m_propCfg, DB_USER);
		m_dbPwd    = YCIGlobal.ReadProperty(m_propCfg, DB_PASSWORD);

		if ( m_propCfg.containsKey(DB_MAX_COMMIT) ) {
			m_maxCommit = YCIGlobal.ReadUIntProperty(m_propCfg, DB_MAX_COMMIT);
		}
	}

	private void ReadDesConfig() throws IOException {
		m_sqlRegion      = YCIGlobal.ReadProperty(m_propCfg, DES_REGION_SQL);
		m_sqlTabName     = YCIGlobal.ReadProperty(m_propCfg, DES_TAB_NAME_SQL);
		m_tabReportState = YCIGlobal.ReadProperty(m_propCfg, DES_TAB_REPORT_STATE);
	}

//	// 读取HDFS配置
//	private void ReadHdfsConfig() {
//		// HDFS配置可为空！
//		m_hdfsHost = m_propCfg.getProperty(HDFS_HOST);
//		m_hdfsPath = m_propCfg.getProperty(HDFS_PATH);
//
//		final String PORT = m_propCfg.getProperty(HDFS_PORT);
//		if ( PORT != null ) {
//			m_hdfsPort = Integer.parseInt(PORT);
//		}
//	}

//	// 读取HIVE配置
//	private void ReadHiveConfig() {
//		// HIVE配置可为空！
//		m_hiveZkquorum  = m_propCfg.getProperty(HIVE_ZKQUORUM);
//		m_hiveKrb5      = m_propCfg.getProperty(HIVE_KRB5);
//		m_hiveUserkey   = m_propCfg.getProperty(HIVE_USER_KEYTAB);
//		m_hivePrincipal = m_propCfg.getProperty(HIVE_PRINCIPAL);
//		m_hiveJaas      = m_propCfg.getProperty(HIVE_JAAS);
//		m_hiveLocation  = m_propCfg.getProperty(HIVE_LOCATION);
//	}

	// 读取Policy配置
	private void ReadPolicyConfig() throws IOException {
		m_policy = YCIGlobal.ReadProperty(m_propCfg, YCI_POLICY);
	}

	// 读取输入路径配置
	private void ReadInputConfig() throws IOException {
		final int PATH_SIZE = YCIGlobal.ReadUIntProperty(m_propCfg, INPUT_PATH_SIZE);

		m_paths = new String[PATH_SIZE];
		for ( int i = 0; i < PATH_SIZE; ++i ) {
			m_paths[i] = new String(YCIGlobal.ReadProperty(m_propCfg, INPUT_PATH_PREFIX+(i+1)));
		}
	}

	// 读取备份路径
	private void ReadOutputConfig() throws IOException {
		m_backupPath  = YCIGlobal.ReadProperty(m_propCfg, OUTPUT_BACKUP_PATH);
		m_suspendPath = YCIGlobal.ReadProperty(m_propCfg, OUTPUT_SUSPEND_PATH);
		m_failPath    = YCIGlobal.ReadProperty(m_propCfg, OUTPUT_FAIL_PATH);
	}

	// 输出配置信息
	private void ShowConfig() {
		Logger logger = LogManager.getLogger(Object.class);

		// Workers
		logger.info("[CONFIG] "+YCI_WORKERS+" = ["+m_workers+"]");

		// DB
		logger.info("[CONFIG] "+DB_DRIVER+"   = ["+m_dbDriver+"]");
		logger.info("[CONFIG] "+DB_URL+"      = ["+m_dbURL+"]");
		logger.info("[CONFIG] "+DB_USER+"     = ["+m_dbUsr+"]");
		logger.info("[CONFIG] "+DB_PASSWORD+" = ["+m_dbPwd+"]");

		if ( m_propCfg.containsKey(DB_MAX_COMMIT) ) {
			logger.info("[CONFIG] "+DB_MAX_COMMIT+" = ["+m_maxCommit+"]");
		}

		// DES
		logger.info("[CONFIG] "+DES_REGION_SQL+" = ["+m_sqlRegion+"]");
		logger.info("[CONFIG] "+DES_TAB_NAME_SQL+" = ["+m_sqlTabName+"]");
		logger.info("[CONFIG] "+DES_TAB_REPORT_STATE+" = ["+m_tabReportState+"]");

//		// HDFS
//		logger.info("[CONFIG] "+HDFS_HOST+" = ["+m_hdfsHost+"]");
//		logger.info("[CONFIG] "+HDFS_PORT+" = ["+m_hdfsPort+"]");
//		logger.info("[CONFIG] "+HDFS_PATH+" = ["+m_hdfsPath+"]");
//
//		// HIVE
//		logger.info("[CONFIG] "+HIVE_ZKQUORUM+"    = ["+m_hiveZkquorum+"]");
//		logger.info("[CONFIG] "+HIVE_KRB5+"        = ["+m_hiveKrb5+"]");
//		logger.info("[CONFIG] "+HIVE_USER_KEYTAB+" = ["+m_hiveUserkey+"]");
//		logger.info("[CONFIG] "+HIVE_PRINCIPAL+"   = ["+m_hivePrincipal+"]");
//		logger.info("[CONFIG] "+HIVE_JAAS+"        = ["+m_hiveJaas+"]");
//		logger.info("[CONFIG] "+HIVE_LOCATION+"    = ["+m_hiveLocation+"]");

		// Policy
		logger.info("[CONFIG] "+YCI_POLICY+" = ["+m_policy+"]");

		// Input
		logger.info("[CONFIG] "+INPUT_PATH_SIZE+" = ["+m_paths.length+"]");
		for ( int i = 0; i < m_paths.length; ++i ) {
			logger.info("[CONFIG] "+INPUT_PATH_PREFIX+(i+1)+" = ["+m_paths[i]+"]");
		}

		// Output
		logger.info("[CONFIG] "+OUTPUT_BACKUP_PATH+" = ["+m_backupPath+"]");
		logger.info("[CONFIG] "+OUTPUT_SUSPEND_PATH+" = ["+m_suspendPath+"]");
		logger.info("[CONFIG] "+OUTPUT_FAIL_PATH+" = ["+m_failPath+"]");
	}

	public int GetWorkers() {
		return m_workers;
	}

	public String GetDBDriver() {
		return m_dbDriver;
	}

	// 获取数据库URL
	public String GetDBURL() {
		return m_dbURL;
	}

	// 获取数据库用户名
	public String GetDBUser() {
		return m_dbUsr;
	}

	// 获取数据库密码
	public String GetDBPassword() {
		return m_dbPwd;
	}

	// 获取最大提交数
	public int GetMaxCommit() {
		return m_maxCommit;
	}

//	// 获取HDFS主机
//	public String GetHdfsHost() {
//		return m_hdfsHost;
//	}
//
//	// 获取HDFS端口
//	public int GetHdfsPort() {
//		return m_hdfsPort;
//	}
//
//	// 获取HDFS路径
//	public String GetHdfsPath() {
//		return m_hdfsPath;
//	}
//
//	public String GetHiveZkquorum() {
//		return m_hiveZkquorum;
//	}
//
//	public String GetHiveKrb5() {
//		return m_hiveKrb5;
//	}
//
//	public String GetHiveUserKey() {
//		return m_hiveUserkey;
//	}
//
//	public String GetHivePrincipal() {
//		return m_hivePrincipal;
//	}
//
//	public String GetHiveJaas() {
//		return m_hiveJaas;
//	}
//
//	public String GetHiveLocation() {
//		return m_hiveLocation;
//	}

	public String GetRegionSql() {
		return m_sqlRegion;
	}

	public String GetReportTabNameSql() {
		return m_sqlTabName;
	}

	public String GetTabReportState() {
		return m_tabReportState;
	}

	public String GetPolicy() {
		return m_policy;
	}

	public String[] GetInputPaths() {
		return m_paths;
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

}
