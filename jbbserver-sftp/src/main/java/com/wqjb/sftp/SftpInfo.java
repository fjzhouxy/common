package com.wqjb.sftp;
/**  
 * <p>package:com.wqjb.sftp
 * <p>Title: SftpInfo.java</p>
 * <p>Description: FTP用户登录信息</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 魏桥金保</p>  
 * @author zhouxy  
 * @date 2019年6月20日  下午7:14:05
 * @version 1.0  
 */
public class SftpInfo {
	/** FTP 登录用户名*/  
    private String username;
    /** FTP 登录密码*/  
    private String password;
    /** FTP 服务器地址IP地址*/  
    private String host;
    /** FTP 服务器端口*/  
    private int port;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

}
