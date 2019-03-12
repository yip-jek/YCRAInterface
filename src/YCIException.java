
// 自定义异常类
public class YCIException extends Throwable {

	private static final long serialVersionUID = -872274400493654024L;

	public static final int INVALID_ERRORCODE = 0;

	private int m_errorCode = INVALID_ERRORCODE;

	public YCIException(String msg) {
		super(msg);
	}

	public YCIException(String msg, int error_code) {
		super(msg);

		m_errorCode = error_code;
	}

	public int GetErrorCode() {
		return m_errorCode;
	}

	@Override
	public String toString() {
		// error code 是否有效？
		if ( m_errorCode != 0 ) {
			return (super.toString()+" (ERROR_CODE="+m_errorCode+")");
		} else {
			return super.toString();
		}
	}

}
