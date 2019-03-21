// 匹配信息类
public class YCIMatchInfo {

	// 特殊字段名
	public static final String FIELD_CURRENT_TIME = "CURRENT_TIME";		// 当前时间
	public static final String FIELD_REGION       = "CITY";				// 地市
	public static final String FIELD_DATE         = "DATE";				// 日期
	public static final String FIELD_SEQ          = "SEQ";				// 序号

	// 地市字段替换
	public static final String REGION_REPLACE_ETOC = "REGION_ETOC";		// 地市替换：英->中
	public static final String REGION_REPLACE_CTOE = "REGION_CTOE";		// 地市替换：中->英
	public static final int    INVALID_FIELD_INDEX = -1;				// 无效的字段索引

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

	// 获取地市替换字段类型及索引，若有则返回“类型:索引”，没有则返回null
	// 格式1：[目的字段名:REGION_CTOE:源字段所在索引位置]
	// 格式2：[目的字段名:REGION_ETOC:源字段所在索引位置]
	// 注：索引位置从1开始
	public String TryGetReplaceRegion() {
		String[] fields = m_policy.GetDesFields();
		for ( String f : fields ) {
			if ( YCIGlobal.IsSurroundWithBrackets(f) ) {
				String[] sections = YCIGlobal.SplitTrim(f.substring(1, f.length()-1), ":", 3);
				if ( sections.length == 3 && (sections[1].equals(REGION_REPLACE_CTOE)
					|| sections[1].equals(REGION_REPLACE_ETOC)) && Integer.parseInt(sections[2]) > 0 ) {
					return (sections[1]+":"+sections[2]);
				}
			}
		}

		return null;
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
		StringBuilder buf_head   = new StringBuilder("insert into ");
		StringBuilder buf_tail   = new StringBuilder("values(");
		String[]      des_fields = m_policy.GetDesFields();
		String[]      des_sp     = null;

		buf_head.append(m_policy.GetDesTable()).append('(');

		for ( String des_fd : des_fields ) {
			// 格式：[目标表字段名:特殊字段名(:格式表达式)]
			if ( YCIGlobal.IsSurroundWithBrackets(des_fd) ) {
				des_sp = YCIGlobal.SplitTrim(des_fd.substring(1, des_fd.length()-1), ":", 2);

				buf_head.append(des_sp[0]);
				try {
					buf_tail.append(ValueOfSpecialField(des_sp[1]));
				} catch ( IllegalArgumentException e ) {
					throw new IllegalArgumentException("Invalid field definition: "+des_fd+", "+e);
				}
			} else {
				buf_head.append(des_fd);
				buf_tail.append("?");
			}

			buf_head.append(", ");
			buf_tail.append(", ");
		}

		// 删除尾部的：", "
		buf_head.delete(buf_head.length()-2, buf_head.length());
		buf_tail.delete(buf_tail.length()-2, buf_tail.length());

		buf_head.append(") ");
		buf_tail.append(')');
		buf_head.append(buf_tail);

		return buf_head.toString();
	}

	private String ValueOfSpecialField(String field) {
		switch ( field ) {
		case FIELD_REGION:
			return ("'"+m_city+"'");
		case FIELD_DATE:
			return ("'"+m_date+"'");
		case FIELD_SEQ:
			return String.valueOf(m_seq);
		default:
			return ValueOfThreeSectionsField(field);
		}
	}

	private String ValueOfThreeSectionsField(String fmt) {
		String[] fields = YCIGlobal.SplitTrim(fmt, ":", 2);
		switch ( fields[0] ) {
		case FIELD_CURRENT_TIME:
			return ("'"+YCIGlobal.CurrentDateTime(fields[1])+"'");
		case REGION_REPLACE_CTOE:
		case REGION_REPLACE_ETOC:
			return "?";
		default:
			throw new IllegalArgumentException("Unsupported field format: "+fmt);
		}
	}

}
