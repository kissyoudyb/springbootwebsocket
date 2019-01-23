package cn.danyubin.websocket.webssh;

import java.io.IOException;
import java.io.InputStream;

import javax.websocket.Session;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class SshWriteThread implements Runnable {

	// 定义一个flag,来停止线程
	private boolean isStop = false;
	// 输入流数据
	private InputStream in;
	// 用于输出数据
	//private WebSocketSession websocketSession;
	private Session websocketSession;
	
	private static final String ENCODING = "UTF-8";
	
	
	
	public SshWriteThread(InputStream in, Session websocketSession) {
		super();
		this.in = in;
		this.websocketSession = websocketSession;
	}

	// 停止线程
	public void stopThread() {
		this.isStop = true;
	}
	
	@Override
	public void run() {
		// 线程运行中 且 session不为空 且 session是打开状态
		while(!isStop && websocketSession != null && websocketSession.isOpen()) {
			// 写数据到客户端
			writeToWeb(in);
		}
	}
	
	private void writeToWeb(InputStream in) {
		try {
			// 定义一个缓存
			// 一个UDP的用户数据报的数据字段长度为8192字节
			byte[] buff = new byte[8192];
			
			int len = 0;
			StringBuffer sb = new StringBuffer();
			while( (len = in.read(buff)) > 0) {
				// 设定从0开始
				sb.setLength(0);
				
				// 读缓冲区里的数据，进行补码
				for (int i = 0; i < len; i++) {
					// 进行补码操作
					char c = (char) (buff[i] & 0xff);
					sb.append(c);
				}
				
				String line = new String(sb.toString().getBytes("ISO-8859-1"), ENCODING);
				// 写数据到客户端
				//websocketSession.sendMessage(new TextMessage(line));
				websocketSession.getBasicRemote().sendText(line);
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}

}
