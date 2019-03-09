
// 文件名匹配类
public class YCIFileName {

	public static final String PLACEHOLDER_REGION   = "[XX]";				// 占位符：地市
	public static final String PLACEHOLDER_DATE_MON = "[YYYYMM]";			// 点位符：月账期
	public static final String PLACEHOLDER_DATE_DAY = "[YYYYMMDD]";		// 点位符：日账期
	public static final String PLACEHOLDER_SEQ      = "[NNN]";				// 点位符：序号

	private String m_fnCity    = null;				// 文件名地市
	private String m_fileName  = null;				// 文件名
	private String m_fileRegex = null;				// 文件名正则表达式

	public YCIFileName(String city, String file_name) {
		m_fnCity   = city;
		m_fileName = file_name;

		SetRegex(file_name);
	}

	private void SetRegex(String file_name) {
		String reg_file = file_name;
		reg_file = YCIGlobal.ReplacePlaceholder(reg_file, PLACEHOLDER_DATE_MON, "[0-9]{6}");
		reg_file = YCIGlobal.ReplacePlaceholder(reg_file, PLACEHOLDER_DATE_DAY, "[0-9]{8}");
		reg_file = YCIGlobal.ReplacePlaceholder(reg_file, PLACEHOLDER_SEQ, "[0-9]{3}");

		m_fileRegex = reg_file;
	}

	public String GetCity() {
		return m_fnCity;
	}

	public String GetFileName() {
		return m_fileName;
	}

	public boolean IsMatch(String file_name) {
		return file_name.matches(m_fileRegex);
	}

}
