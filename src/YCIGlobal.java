import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class YCIGlobal {

	public static final String VERSION = "Version 1.2.0";			// 版本

	// 获取进程ID
	public static final int GetProcessID() {
		RuntimeMXBean rt_mxb = ManagementFactory.getRuntimeMXBean();
		return Integer.parseInt(rt_mxb.getName().split("@")[0]);
	}

	// 是否为Windows操作系统？
	public static final boolean IsWindowsOS() {
		return System.getProperty("os.name").toLowerCase().startsWith("win");
	}

	// 获取当前时间（指定格式）
	public static String CurrentDateTime(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	// 读取配置信息
	public static String ReadProperty(Properties prop, String key) throws IOException {
		final String PROP_VAL = prop.getProperty(key);
		if ( null == PROP_VAL || PROP_VAL.isEmpty() ) {
			throw new IOException("Configuration item \""+key+"\" not configured correctly!");
		} else {
			return PROP_VAL;
		}
	}

	// 拆分并去除首尾空白符
	public static String[] SplitTrim(String src, String regex, int limit) {
		String[] src_strs = src.split(regex, limit);
		String[] des_strs = new String[src_strs.length];

		for ( int i = 0; i < src_strs.length; ++i ) {
			des_strs[i] = new String(src_strs[i].trim());
		}

		return des_strs;
	}

}
