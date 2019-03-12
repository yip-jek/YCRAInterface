
// 文件名匹配类
public class YCIFileName {

	public static final String PLACEHOLDER_REGION   = "[XX]";				// 占位符：地市
	public static final String PLACEHOLDER_DATE_MON = "[YYYYMM]";			// 点位符：月账期
	public static final String PLACEHOLDER_DATE_DAY = "[YYYYMMDD]";			// 点位符：日账期
	public static final String PLACEHOLDER_SEQ      = "[NNN]";				// 点位符：序号

	public static final int INVALID_SEQ = -1;		// 无效的序号

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

	public String GetOriginalFileName() {
		String original_filename = m_fileName;
		original_filename = original_filename.replaceAll("\\[", "");
		original_filename = original_filename.replaceAll("\\]", "");
		return original_filename;
	}

	public YCIMatchInfo Match(YCIPolicy policy, String file_name) {
		if ( file_name.matches(m_fileRegex) ) {
			return new YCIMatchInfo(policy, m_fnCity, GetMatchDate(file_name), GetMatchSeq(file_name));
		}

		return null;
	}

	private String GetMatchDate(String file_name) {
		// 此处要通过 PLACEHOLDER_DATE_MON 来判别是否存在
		if ( m_fileName.indexOf(PLACEHOLDER_DATE_MON) >= 0 ) {
			// 去除中括号
			final String HOLDER_DATE_MON = PLACEHOLDER_DATE_MON.substring(1, PLACEHOLDER_DATE_MON.length()-1);

			int index = GetOriginalFileName().indexOf(HOLDER_DATE_MON);
			return file_name.substring(index, index+HOLDER_DATE_MON.length());
		} else if ( m_fileName.indexOf(PLACEHOLDER_DATE_DAY) >= 0 ) {
			// 去除中括号
			final String HOLDER_DATE_DAY = PLACEHOLDER_DATE_DAY.substring(1, PLACEHOLDER_DATE_DAY.length()-1);

			int index = GetOriginalFileName().indexOf(HOLDER_DATE_DAY);
			return file_name.substring(index, index+HOLDER_DATE_DAY.length());
		} else {
			return null;
		}
	}

	private int GetMatchSeq(String file_name) {
		// 此处要通过 PLACEHOLDER_SEQ 来判别是否存在
		if ( m_fileName.indexOf(PLACEHOLDER_SEQ) >= 0 ) {
			// 去除中括号
			final String HOLDER_SEQ = PLACEHOLDER_SEQ.substring(1, PLACEHOLDER_SEQ.length()-1);

			int index = GetOriginalFileName().indexOf(HOLDER_SEQ);
			return Integer.parseInt(file_name.substring(index, index+HOLDER_SEQ.length()));
		} else {
			return INVALID_SEQ;
		}
	}

}
