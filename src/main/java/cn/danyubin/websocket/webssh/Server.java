package cn.danyubin.websocket.webssh;

public class Server {

	private String hostName;
	private String userName;
	private String passWord;
	
	public Server(String hostName, String userName, String passWord) {
		super();
		this.hostName = hostName;
		this.userName = userName;
		this.passWord = passWord;
	}
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassWord() {
		return passWord;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	
	
}
