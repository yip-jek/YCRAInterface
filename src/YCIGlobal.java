import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class YCIGlobal {

	public static final String VERSION = "Version 2.2.0";			// 版本号

	public static final int LOOP_SLEEP_TIME  = 1000;				// 每一个循环的睡眠时间
	public static final int EXTRA_SLEEP_TIME = 60*1000;				// 额外的睡眠时间
	public static final int INTERVAL_TIME    = 60*5;				// 间隔时间（单位：秒）

	// 获取进程ID
	public static final int GetProcessID() {
		RuntimeMXBean rt_mxb = ManagementFactory.getRuntimeMXBean();
		return Integer.parseInt(rt_mxb.getName().split("@")[0]);
	}

	// 是否为Windows操作系统？
	public static final boolean IsWindowsOS() {
		return System.getProperty("os.name").toLowerCase().startsWith("win");
	}

	// 获取当前毫秒数
	public static long CurrentMilliTime() {
		return System.currentTimeMillis();
	}

	// 获取当前时间间隔（单位：秒）
	public static int CurrentIntervalTime(long last_time) {
		long l_interval = CurrentMilliTime() - last_time;
		return (int)(l_interval/1000);
	}

	// 获取当前时间（指定格式）
	public static String CurrentDateTime(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	// NULL字符串转换为空字符串
	public static String NullToEmpty(String s) {
		if ( s == null ) {
			return "";
		} else {
			return s;
		}
	}

	// 读取配置信息
	public static String ReadProperty(Properties prop, String key) throws IOException {
		final String PROP_VAL = prop.getProperty(key);
		if ( PROP_VAL == null || PROP_VAL.isEmpty() ) {
			throw new IOException("Configuration item \""+key+"\" not configured correctly!");
		}

		return PROP_VAL;
	}

	public static int ReadUIntProperty(Properties prop, String key) throws IOException {
		final String INT_VAL = ReadProperty(prop, key);
		int val = Integer.parseInt(INT_VAL);
		if ( val <= 0 ) {
			throw new IOException("Invalid number property in configuration \""+key+"\": "+INT_VAL);
		}

		return val;
	}

	// 验证是否为文件目录
	public static void VerifyDirectoryFile(File path) throws IOException {
		if ( !path.exists() ) {
			throw new IOException("Non-existing path: "+path.getPath());
		}

		if ( !path.isDirectory() ) {
			throw new IllegalArgumentException("Non-directory path: "+path.getPath());
		}
	}

	// 设置文件目录
	public static String SetFilePath(String path) throws IOException {
		File file_path = new File(path);
		VerifyDirectoryFile(file_path);
		return file_path.getPath();
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
			des_strs[i] = src_strs[i].trim();
		}

		return des_strs;
	}

	// 是否为以'['与']'包围的 String
	public static boolean IsSurroundWithBrackets(String s) {
		return (s.charAt(0) == '[' && s.charAt(s.length()-1) == ']');
	}

	// 替换点位符
	// 点位符格式：[...]
	public static String ReplacePlaceholder(String src, String placeholder, String replace) {
		// 点位符格式是否正确？
		if ( !IsSurroundWithBrackets(placeholder) ) {
			throw new IllegalArgumentException("Invalid placeholder: "+placeholder);
		}

		StringBuilder str_buf = new StringBuilder(placeholder);
		str_buf.insert(str_buf.length()-1, '\\');
		str_buf.insert(0, '\\');

		return src.replaceAll(str_buf.toString(), replace);
	}

}
