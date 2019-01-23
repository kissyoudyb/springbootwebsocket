package cn.danyubin.websocket.webssh;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.springframework.web.socket.WebSocketSession;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class SshClient {

	// 服务器信息
	private Server server;
	// 与web客户端连接的websocket会话
	//private WebSocketSession websocketSession;
	private javax.websocket.Session websocketSession;
	// ssh2Connection
	private Connection conn = null;
	// ssh2Session
	private Session ssh2Session = null;
	
	private SshWriteThread writeThread = null;
	private BufferedWriter out = null;
	
	public SshClient(Server server, javax.websocket.Session websocketSession) {
		super();
		this.server = server;
		this.websocketSession = websocketSession;
	}
	
	public boolean connect() {
		try {
			String hostName = this.server.getHostName();
			String userName = this.server.getUserName();
			String passWord = this.server.getPassWord();
			
			conn = new Connection(hostName, 22);
			conn.connect();
			
			boolean isAuthenticate = conn.authenticateWithPassword(userName, passWord);
			
			if(isAuthenticate) {
				// 打开连接
				ssh2Session = conn.openSession();
				
				ssh2Session.requestPTY("xterm", 180, 60, 0, 0, null);
				
				// 启动shell
				ssh2Session.startShell();
				
				// 向客户端写数据
				startWriterThread();
				
				// 输出流
				out = new BufferedWriter(new OutputStreamWriter(ssh2Session.getStdin(), "utf-8"));
				
				return true;
			} else {
				System.out.println("用户名和密码校验出错，连接服务器失败...");
				//this.disconnect();
				return false;
			}
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			//this.disconnect();
			return false;
		}
	}
	
	/**
	 * 开启线程,新建一个websocket连接，接收服务器端的数据到webclient
	 */
	private void startWriterThread() {
		// 启动多线程，来获取我们运行的结果
		// 第一个参数输入流
		// 第二个参数，输出流，这个直接输出的是控制台
		writeThread = new SshWriteThread(ssh2Session.getStdout(), websocketSession);
		new Thread(writeThread).start();
	}
	
	/**
	 * 关闭连接
	 */
	public void disconnect() {
		
		if(out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(ssh2Session != null) {
			ssh2Session.close();
		}
		
		if(conn != null) {
			conn.close();
		}
		
		if(writeThread != null) {
			writeThread.stopThread();
		}
	}
	
	/**
	 * 写数据到服务器端，让机器执行命令
	 * @param cmd
	 * @return 
	 */
	public boolean write(String cmd) {
		try {
			this.out.write(cmd);
			this.out.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
}
