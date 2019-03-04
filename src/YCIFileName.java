
// 文件名匹配类
public class YCIFileName {

	private String m_fnCity    = null;				// 文件名地市
	private String m_fileName  = null;				// 文件名
	private String m_fileRegex = null;				// 文件名正则表达式

	public YCIFileName(String city, String file_name) {
		m_fnCity   = city;
		m_fileName = file_name;

		Regex(file_name);
	}

	private void Regex(String file_name) {
		;
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
