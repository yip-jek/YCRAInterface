import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class YCIGlobal {

	public static final String VERSION          = "Version 1.3.0";			// 版本
	public static final int    LOOP_SLEEP_TIME  = 1000;						// 每一个循环的睡眠时间
	public static final int    EXTRA_SLEEP_TIME = 60*1000;					// 额外的睡眠时间

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
		if ( PROP_VAL == null || PROP_VAL.isEmpty() ) {
			throw new IOException("Configuration item \""+key+"\" not configured correctly!");
		} else {
			return PROP_VAL;
		}
	}

	// 是否为文件目录
	public static void CheckDirectoryFile(File path) throws IOException {
		if ( !path.exists() ) {
			throw new IOException("Non-existing path: "+path.getPath());
		}

		if ( !path.isDirectory() ) {
			throw new IllegalArgumentException("Non-directory path: "+path.getPath());
		}
	}

	// 移动文件到指定目录
	public static void MoveFile(File src_file, String des_path) throws IOException {
		final String FILE_PATH = des_path + File.separator + src_file.getName();
		if ( !src_file.renameTo(new File(FILE_PATH)) ) {
			throw new IOException("Move file \""+src_file.getPath()+"\" to \""+FILE_PATH+"\" failed!");
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

	// 替换点位符
	// 点位符格式：[...]
	public static String ReplacePlaceholder(String src, String placeholder, String replace) {
		// 点位符格式是否正确？
		if ( placeholder.charAt(0) != '[' || placeholder.charAt(placeholder.length()-1) != ']' ) {
			throw new IllegalArgumentException("Invalid placeholder: "+placeholder);
		}

		StringBuilder str_buf = new StringBuilder(placeholder);
		str_buf.insert(str_buf.length()-1, '\\');
		str_buf.insert(0, '\\');

		return src.replaceAll(str_buf.toString(), replace);
	}

}
