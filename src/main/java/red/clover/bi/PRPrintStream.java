package red.clover.bi;

import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.hop.core.logging.ILogChannel;

public class PRPrintStream extends PrintStream {

	private ILogChannel log;

	public PRPrintStream(OutputStream out, ILogChannel log) {
		super(out);
		this.log = log;
	}

	/**
	 * Method println
	 * 
	 * @param
	 **/
	public void println(String string) {
		log.logBasic(string);
	}


	public void print(String string) {
		log.logBasic(string);
	}
}
