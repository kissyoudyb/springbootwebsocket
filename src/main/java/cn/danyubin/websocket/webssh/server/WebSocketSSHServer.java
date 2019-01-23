package cn.danyubin.websocket.webssh.server;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import cn.danyubin.websocket.webssh.Server;
import cn.danyubin.websocket.webssh.SshClient;

@ServerEndpoint("/websocket/test")
@Component
public class WebSocketSSHServer {
	
	private static Log logger = LogFactory.getLog(WebSocketSSHServer.class);
	
	// ssh客户端
	private SshClient client;
	
	// 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    // concurrent包的线程安全Set,用来存放每个客户端对应的WebSocket对象
    private static CopyOnWriteArraySet<WebSocketSSHServer> webSocketSSHServerSet = new CopyOnWriteArraySet<WebSocketSSHServer>();
    
    /**
     * 连接建立成功调用的方法
     * @param websocketSession
     */
    @OnOpen
    public void onOpen(Session websocketSession) {
    	
    	// 配置服务器信息
    	//Server server = new Server("192.168.86.154", "root", "123456");
    	Server server = new Server("192.168.86.156", "root", "123456");
    	
    	// 初始化客户端
    	this.client = new SshClient(server, websocketSession);
    	
    	// 连接服务器
    	this.client.connect();
    	
    	// 加入Set
    	webSocketSSHServerSet.add(this);
    	// 在线数加1
        addOnlineCount();
        
        logger.info("有新窗口开始监听,当前在线client数为" + getOnlineCount());
    }
    
    /**
     * 连接关闭调用的方法
     * @param websocketSession
     */
    @OnClose
    public void onClose(Session websocketSession) {
    	
    	// 从Set中删除
    	webSocketSSHServerSet.remove(this);
    	// 在线数减1
    	subOnlineCount();
    	
    	logger.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    	// 关闭连接
    	if (this.client != null) {
    		this.client.disconnect();
    	}
    }
    
    @OnMessage
    public void onMessage(String message, Session websocketSession) {
    	logger.info("收到来自窗口的信息:"+message);
    	try {
    		//当客户端不为空的情况
    		if (client != null) {
    			// receive a close cmd ?
    			// 因为字符是一个一个接收的所以这个地方永远不会进入
//    			if (message.equals("exit")) {
//    				
//    				if (client != null) {
//    					client.disconnect();
//    				}
//    				
//    				if(websocketSession != null) {
//    					websocketSession.close();
//    				}
//    				
//    				// 从Set中删除
//    		    	webSocketSSHServerSet.remove(this);
//    		    	// 在线数减1
//    		    	subOnlineCount();
//    		    	logger.info("有一连接关闭！当前在线人数为" + getOnlineCount());
//    				return ;
//    			}
    			//写入前台传递过来的命令，发送到目标服务器上
    			client.write(message);
    		}
    	} catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
    		
    		try {
				//websocketSession.sendMessage(new TextMessage("An error occured, websocket is closed."));
    			websocketSession.getBasicRemote().sendText("An error occured, websocket is closed.");
				websocketSession.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
    }
    
    /**
     * 发生错误
     * @param websocketSession
     * @param error
     */
    @OnError
    public void onError(Session websocketSession, Throwable error) {
        logger.error("发生错误");
        error.printStackTrace();
    }

    public static synchronized int getOnlineCount() {
    	return onlineCount;
    }
    
    public static synchronized void addOnlineCount() {
    	WebSocketSSHServer.onlineCount++;
    }
    
    public static synchronized void subOnlineCount() {
    	WebSocketSSHServer.onlineCount--;
    }
}
