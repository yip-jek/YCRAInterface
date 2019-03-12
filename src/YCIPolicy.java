import java.io.IOException;
import java.util.Properties;

// 策略
public class YCIPolicy {

	private static final String PREFIX_POLICY       = "Policy_";
	private static final String SRC_FILE            = "src_file";
	private static final String SRC_FILE_ENCODING   = "src_file_encoding";
	private static final String SRC_COLUMN_SIZE     = "src_file_column_size";
	private static final String SRC_REGEX_SEPARATOR = "src_file_regex_separator";
	private static final String DES_TABLE           = "des_table";
	private static final String DES_FIELDS          = "des_fields";
	private static final String DES_FIELD_SEPARATOR = ",";

	private PolicyManager m_policyMgr         = null;
	private int           m_id                = 0;				// 策略ID
	private YCIFileName[] m_fileNames         = null;
	private String        m_srcFileEncoding   = null;			// 源文件编码格式
	private int           m_srcColumnSize     = 0;				// 源文件数据列数
	private String        m_srcRegexSeparator = null;			// 源文件数据分隔符（正则）
	private String        m_desTable          = null;			// 目标表名
	private String[]      m_desFields         = null;			// 目标表字段名称

	public YCIPolicy(PolicyManager policyMgr, int id, Properties prop) throws IOException {
		m_policyMgr = policyMgr;
		m_id        = id;

		Load(prop);
	}

	private void Load(Properties prop) throws IOException {
		final String POLICY_WITH_ID = PREFIX_POLICY + m_id;

		GenerateFileName(YCIGlobal.ReadProperty(prop, POLICY_WITH_ID+"."+SRC_FILE));

		m_srcFileEncoding   = YCIGlobal.ReadProperty(prop, POLICY_WITH_ID+"."+SRC_FILE_ENCODING);
		m_srcRegexSeparator = YCIGlobal.ReadProperty(prop, POLICY_WITH_ID+"."+SRC_REGEX_SEPARATOR);
		m_desTable          = YCIGlobal.ReadProperty(prop, POLICY_WITH_ID+"."+DES_TABLE);

		m_srcColumnSize = Integer.parseInt(YCIGlobal.ReadProperty(prop, POLICY_WITH_ID+"."+SRC_COLUMN_SIZE));
		if ( m_srcColumnSize <= 0 ) {
			throw new IOException("Invalid size of column in configuration \""+POLICY_WITH_ID+"."+SRC_COLUMN_SIZE+"\": "+m_srcColumnSize);
		}

		final String FIELDS = YCIGlobal.ReadProperty(prop, POLICY_WITH_ID+"."+DES_FIELDS);
		m_desFields = YCIGlobal.SplitTrim(FIELDS, DES_FIELD_SEPARATOR, 0);
	}

	private void GenerateFileName(String src_file) {
		// 是否存在“地市占位符”？
		if ( src_file.indexOf(YCIFileName.PLACEHOLDER_REGION) >= 0 ) {
			YCIRegion[] regions = m_policyMgr.GetRegions();

			m_fileNames = new YCIFileName[regions.length];
			for ( int i = 0; i < m_fileNames.length; ++i ) {
				String city = regions[i].GetCity();
				m_fileNames[i] = new YCIFileName(city, YCIGlobal.ReplacePlaceholder(src_file, YCIFileName.PLACEHOLDER_REGION, city));
			}
		} else {		// 没有地市信息
			m_fileNames    = new YCIFileName[1];
			m_fileNames[0] = new YCIFileName("", src_file);
		}
	}

	public int GetID() {
		return m_id;
	}

	public String GetSrcFileEncoding() {
		return m_srcFileEncoding;
	}

	public int GetSrcColumnSize() {
		return m_srcColumnSize;
	}

	public String GetSrcRegexSeparator() {
		return m_srcRegexSeparator;
	}

	public String GetDesTable() {
		return m_desTable;
	}

	public String[] GetDesFields() {
		return m_desFields;
	}

	public YCIMatchInfo MatchFile(String file_name) {
		for ( YCIFileName fname : m_fileNames ) {
			YCIMatchInfo info = fname.Match(this, file_name);
			if ( info != null ) {
				return info;
			}
		}

		return null;
	}

}
