import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

// 信号类
@SuppressWarnings("restriction")
public class YCSignal implements SignalHandler {

	private boolean QUIT_SIGNAL_SET = false;

	public YCSignal() {
		Signal.handle(new Signal("INT"), this);
		Signal.handle(new Signal("TERM"), this);
	}

	@Override
	public void handle(Signal arg0) {
		Logger logger = LogManager.getLogger(Object.class);
		logger.warn("Signal "+arg0.getName()+" is received!");

		QUIT_SIGNAL_SET = true;
	}

	// 是否为退出的信号
	public boolean IsQuitSignal() {
		return QUIT_SIGNAL_SET;
	}

}
