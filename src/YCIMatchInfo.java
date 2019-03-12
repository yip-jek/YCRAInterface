
// 匹配信息类
public class YCIMatchInfo {

	// 特殊字段名
	public static final String FIELD_CURRENT_TIME = "CURRENT_TIME";		// 当前时间
	public static final String FIELD_REGION       = "CITY";				// 地市
	public static final String FIELD_DATE         = "DATE";				// 日期
	public static final String FIELD_SEQ          = "SEQ";				// 序号

	private YCIPolicy m_policy = null;			// 策略
	private String    m_city   = null;			// 地市
	private String    m_date   = null;			// 日期
	private int       m_seq    = 0;				// 序号

	public YCIMatchInfo(YCIPolicy p, String city, String date, int seq) {
		m_policy = p;
		m_city   = city;
		m_date   = date;
		m_seq    = seq;
	}

	public YCIPolicy GetPolicy() {
		return m_policy;
	}

	public String GetCity() {
		return m_city;
	}

	public String GetDate() {
		return m_date;
	}

	public int GetSeq() {
		return m_seq;
	}

	public String CreateStoreSql() {
		StringBuilder buf_head   = new StringBuilder("INSERT INTO ");
		StringBuilder buf_tail   = new StringBuilder('(');
		String[]      des_fields = m_policy.GetDesFields();
		String[]      des_sp     = null;

		buf_head.append(m_policy.GetDesTable()).append('(');

		for ( String des_fd : des_fields ) {
			if ( YCIGlobal.IsSurroundWithBrackets(des_fd) ) {
				des_sp = YCIGlobal.SplitTrim(des_fd, ":", 2);
			} else {
				;
			}
		}

		buf_head.append(") ");
		buf_tail.append(')');
		buf_head.append(buf_tail);

		return buf_head.toString();
	}


}
