import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// ������
public class YCIConfig {

	private static final String YCI_WORKERS          = "YCI.Workers";
	private static final String DB_DRIVER            = "DB.Driver";
	private static final String DB_URL               = "DB.URL";
	private static final String DB_USER              = "DB.User";
	private static final String DB_PASSWORD          = "DB.Password";
	private static final String DES_REGION_SQL       = "DES.RegionSQL";
	private static final String DES_REPORT_STATE_TAB = "DES.ReportStateTab";
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

	private Properties m_propCfg        = null;
	private int        m_workers        = 0;
	private String     m_dbDriver       = null;
	private String     m_dbURL          = null;				// ���ݿ�URL
	private String     m_dbUsr          = null;				// ���ݿ��û���
	private String     m_dbPwd          = null;				// ���ݿ�����
	private String     m_sqlRegion      = null;				// ������ϢSQL���
	private String     m_tabReportState = null;				// ����״̬��
//	private String     m_hdfsHost       = null;				// HDFS����
//	private int        m_hdfsPort       = 0;				// HDFS�˿�
//	private String     m_hdfsPath       = null;				// HDFS·��
//	private String     m_hiveZkquorum   = null;
//	private String     m_hiveKrb5       = null;
//	private String     m_hiveUserkey    = null;
//	private String     m_hivePrincipal  = null;
//	private String     m_hiveJaas       = null;
//	private String     m_hiveLocation   = null;				// HIVEλ��
	private String     m_policy         = null;				// ����

	public YCIConfig(Properties prop) throws IOException {
		m_propCfg = prop;

		ReadWorkConfig();
		ReadDBConfig();
//		ReadHdfsConfig();
//		ReadHiveConfig();
		ReadDesConfig();
		ReadPolicyConfig();

		ShowConfig();
	}

	private void ReadWorkConfig() throws IOException {
		final String CFG_WORKERS = YCIGlobal.ReadProperty(m_propCfg, YCI_WORKERS);

		m_workers = Integer.parseInt(CFG_WORKERS);
		if ( m_workers <= 0 ) {
			throw new IOException("Invalid number of worker process in configuration \""+YCI_WORKERS+"\": "+m_workers);
		}
	}

	// ��ȡ���ݿ�����
	private void ReadDBConfig() throws IOException {
		m_dbDriver = YCIGlobal.ReadProperty(m_propCfg, DB_DRIVER);
		m_dbURL    = YCIGlobal.ReadProperty(m_propCfg, DB_URL);
		m_dbUsr    = YCIGlobal.ReadProperty(m_propCfg, DB_USER);
		m_dbPwd    = YCIGlobal.ReadProperty(m_propCfg, DB_PASSWORD);
	}

	private void ReadDesConfig() throws IOException {
		m_sqlRegion      = YCIGlobal.ReadProperty(m_propCfg, DES_REGION_SQL);
		m_tabReportState = YCIGlobal.ReadProperty(m_propCfg, DES_REPORT_STATE_TAB);
	}

//	// ��ȡHDFS����
//	private void ReadHdfsConfig() {
//		// HDFS���ÿ�Ϊ�գ�
//		m_hdfsHost = m_propCfg.getProperty(HDFS_HOST);
//		m_hdfsPath = m_propCfg.getProperty(HDFS_PATH);
//
//		final String PORT = m_propCfg.getProperty(HDFS_PORT);
//		if ( PORT != null ) {
//			m_hdfsPort = Integer.parseInt(PORT);
//		}
//	}

//	// ��ȡHIVE����
//	private void ReadHiveConfig() {
//		// HIVE���ÿ�Ϊ�գ�
//		m_hiveZkquorum  = m_propCfg.getProperty(HIVE_ZKQUORUM);
//		m_hiveKrb5      = m_propCfg.getProperty(HIVE_KRB5);
//		m_hiveUserkey   = m_propCfg.getProperty(HIVE_USER_KEYTAB);
//		m_hivePrincipal = m_propCfg.getProperty(HIVE_PRINCIPAL);
//		m_hiveJaas      = m_propCfg.getProperty(HIVE_JAAS);
//		m_hiveLocation  = m_propCfg.getProperty(HIVE_LOCATION);
//	}

	// ��ȡPolicy����
	private void ReadPolicyConfig() throws IOException {
		m_policy = YCIGlobal.ReadProperty(m_propCfg, YCI_POLICY);
	}

	// ���������Ϣ
	private void ShowConfig() {
		Logger logger = LogManager.getLogger(Object.class);

		// Workers
		logger.info("[CONFIG] "+YCI_WORKERS+" = ["+m_workers+"]");

		// DB
		logger.info("[CONFIG] "+DB_DRIVER+"   = ["+m_dbDriver+"]");
		logger.info("[CONFIG] "+DB_URL+"      = ["+m_dbURL+"]");
		logger.info("[CONFIG] "+DB_USER+"     = ["+m_dbUsr+"]");
		logger.info("[CONFIG] "+DB_PASSWORD+" = ["+m_dbPwd+"]");

		// DES
		logger.info("[CONFIG] "+DES_REGION_SQL+" = ["+m_sqlRegion+"]");
		logger.info("[CONFIG] "+DES_REPORT_STATE_TAB+" = ["+m_tabReportState+"]");

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
	}

	public int GetWorkers() {
		return m_workers;
	}

	public String GetDBDriver() {
		return m_dbDriver;
	}

	// ��ȡ���ݿ�URL
	public String GetDBURL() {
		return m_dbURL;
	}

	// ��ȡ���ݿ��û���
	public String GetDBUser() {
		return m_dbUsr;
	}

	// ��ȡ���ݿ�����
	public String GetDBPassword() {
		return m_dbPwd;
	}

//	// ��ȡHDFS����
//	public String GetHdfsHost() {
//		return m_hdfsHost;
//	}
//
//	// ��ȡHDFS�˿�
//	public int GetHdfsPort() {
//		return m_hdfsPort;
//	}
//
//	// ��ȡHDFS·��
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

	public String GetRegionSQL() {
		return m_sqlRegion;
	}

	public String GetDesReportStateTab() {
		return m_tabReportState;
	}

	public String GetPolicy() {
		return m_policy;
	}

}
