import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 策略管理类
public class PolicyManager {

	public static final String TOTAL_POLICY = "TOTAL_POLICY";

	private Logger      m_logger  = null;
	private YCIPolicy[] m_policys = null;
	private YCIRegion[] m_regions = null;

	public PolicyManager(String policy_file, YCIRegion[] regions) throws FileNotFoundException, IOException {
		m_logger  = LogManager.getLogger(Object.class);
		m_regions = regions;

		ReadPolicy(policy_file);
	}

	private void ReadPolicy(String policy_file) throws FileNotFoundException, IOException {
		Properties prop_policy = new Properties();
		prop_policy.load(new FileInputStream(policy_file));

		// 获取总策略size
		final int TOTAL = YCIGlobal.ReadUIntProperty(prop_policy, TOTAL_POLICY);
		m_logger.info("TOTAL_POLICY = ["+TOTAL+"]");

		m_policys = new YCIPolicy[TOTAL];
		for ( int i = 0; i < TOTAL; ++i ) {
			m_policys[i] = new YCIPolicy(this, i+1, prop_policy);
		}
		m_logger.info("Read policy size: "+m_policys.length);
	}

	public YCIRegion[] GetRegions() {
		return m_regions;
	}

	public int GetPolicySize() {
		return m_policys.length;
	}

	// 获取文件名匹配的策略
	// 若没有对应的匹配策略，则return null
	public YCIMatchInfo GetMatch(String file_name) {
		for ( YCIPolicy policy : m_policys ) {
			YCIMatchInfo info = policy.MatchFile(file_name);
			if ( info != null ) {
				return info;
			}
		}

		return null;
	}

}
