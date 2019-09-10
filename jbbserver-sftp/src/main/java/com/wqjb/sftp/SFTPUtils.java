package com.wqjb.sftp;
 
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
/**
 * <p>package:com.wqjb.sftp
 * <p>Title: SFTPUtils.java</p>
 * <p>Description: sftp连接工具类</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 魏桥金保</p>  
 * @author zhouxy  
 * @date 2019年9月6日  下午4:42:27
 * @version 1.0
 */
public class SFTPUtils {
	private transient Logger log = LoggerFactory.getLogger(this.getClass());
    
    private ChannelSftp sftp;
      
    private Session session;
    /** FTP 登录用户名*/  
    private String username;
    /** FTP 登录密码*/  
    private String password;
    /** 私钥 */  
    private String privateKey;
    /** FTP 服务器地址IP地址*/  
    private String host;
    /** FTP 端口*/
    private int port = 22;
    private static Map<String,SFTPUtils>mapinstance=new  HashMap<String,SFTPUtils>();
//    /** 
//     * 构造基于密码认证的sftp对象 
//     * @param userName 
//     * @param password 
//     * @param host 
//     * @param port 
//     */  
//    public SFTPUtils(String username, String password, String host, int port) {
//        this.username = username;
//        this.password = password;
//        this.host = host;
//        this.port = port;
//    }
//  
//    /** 
//     * 构造基于秘钥认证的sftp对象
//     * @param userName
//     * @param host
//     * @param port
//     * @param privateKey
//     */
//    public SFTPUtils(String username, String host, int port, String privateKey) {
//        this.username = username;
//        this.host = host;
//        this.port = port;
//        this.privateKey = privateKey;
//    }
  
    
    private SFTPUtils(String ftpUser) {
		Yaml yaml = new Yaml();
		URL url = SFTPUtils.class.getClassLoader().getResource("application.yml");
		if (url != null) {
			Map<?, ?> map = null;
			try {
				map = (Map<?, ?>) yaml.load(new FileInputStream(url.getFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Map<?, ?> stfp = (Map<?, ?>) map.get("sftp");
			if(StringUtils.isNotBlank(ftpUser)) {
				stfp = (Map<?, ?>) stfp.get(ftpUser);
			}
			String host = (String) stfp.get("host");
			String username = (String) stfp.get("username");
			String password = (String) stfp.get("password");
			this.username = username;
	        this.host = host;
	        this.password = password;

		}

	}
    
    /**  
     * <p>Description: 对象构造方法，将连接信息通过类传递</p>  
     * @author zhouxy  
     * @date 2019年9月6日  下午4:42:42
     * @param sftpInfo
     */
    public SFTPUtils(SftpInfo sftpInfo) {
    	this.username = sftpInfo.getUsername();
        this.host = sftpInfo.getHost();
        this.password = sftpInfo.getPassword();
        if(sftpInfo.getPort()>0) {
        	this.port = sftpInfo.getPort();
        }
    }

	/**  
	 * <p>Description: 默认获取连接方法，获取default连接信息</p>  
	 * @author zhouxy  
	 * @date 2019年9月6日  下午4:43:01
	 * @return
	 */
	public static SFTPUtils getInstance() {
		return getInstance("");
	}
    /**  
     * <p>Description: 指定获取用户连接信息</p>  
     * @author zhouxy  
     * @date 2019年9月6日  下午4:43:21
     * @param ftpUser
     * @return
     */
    public static SFTPUtils getInstance(String ftpUser) {
    	if(StringUtils.isBlank(ftpUser)) {
    		ftpUser = "default";
    	}
    	if(null==mapinstance.get(ftpUser))
    	{	
    		SFTPUtils instance = new SFTPUtils(ftpUser);
    		mapinstance.put(ftpUser, instance);
    	}
    	return mapinstance.get(ftpUser);
    }
    
    /**  
     * <p>Description: 根据FTP信息对象获取连接</p>  
     * @author zhouxy  
     * @date 2019年6月20日  下午7:19:27
     * @param sftpInfo
     * @return
     */
    public static SFTPUtils getInstance(SftpInfo sftpInfo) {
    	SFTPUtils utils = new SFTPUtils(sftpInfo);
		return utils;
	}
  
    /**
     * <p>Description: 登录Sftp服务器，创建连接</p>  
     * @author zhouxy  
     * @date 2019年9月6日  下午4:43:39
     * @throws JSchException
     */
    public void login() throws JSchException{
        try {
        	Date startTime = new Date();
            
            JSch jsch = new JSch();
            if (privateKey != null) {
                jsch.addIdentity(privateKey);// 设置私钥
                log.info("sftp 连接开始，设置私钥：{}" , privateKey);
            }
            log.info("sftp 开始连接:{} 用户名:{}",host,username);
  
            session = jsch.getSession(username, host, port);
            if (password != null) {
                session.setPassword(password);  
            }
//            Properties config = new Properties();
//            config.put("StrictHostKeyChecking", "no");
//            config.put("PreferredAuthentications", "password");
//              
//            session.setConfig(config);
            session.setConfig("StrictHostKeyChecking","no");
            session.setConfig("PreferredAuthentications","password");
            session.connect();
            log.info("Session连接成功");
            
            Channel channel = session.openChannel("sftp");
            channel.connect();
//            log.info("channel通道 已连接");
  
            sftp = (ChannelSftp) channel;
            log.info("FTP服务器:{}，端口:{}连接成功", host, port);
            Date endTime = new Date();
            log.info("本次创建连接一共耗时{}秒", (endTime.getTime()-startTime.getTime())/1000);
        } catch (JSchException e) {
            log.error("FTP服务器连接失败 : {}:{} \n 异常原因: {}", host, port, e);
            throw e;
        }
    }  
  
    /**
     * <p>Description: 退出Sftp服务器</p>  
     * @author zhouxy  
     * @date 2019年9月6日  下午4:43:57
     */
    public void logout(){
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
                log.debug("SFTP连接已关闭");
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
                log.debug("SFTP session已关闭");
            }
        }
    }
  
    /** 
     * 将输入流的数据上传到sftp作为文件 
     *  
     * @param directory 
     *            上传到该目录 
     * @param sftpFileName 
     *            sftp端文件名 
     * @param in 
     *            输入流 
     * @throws SftpException  
     * @throws IOException 
     * @throws JSchException 
     * @throws Exception 
     */  
    public void upload(InputStream input,String sftpFileName, String directory) throws SftpException, IOException, JSchException{
    	Date startTime = new Date();
        try {
        	login();
            sftp.cd(directory);
        } catch (SftpException e) {
            log.warn("FTP目录:{}不存在，准备创建目录 ",directory);
            sftp.mkdir(directory);
            sftp.cd(directory);
        }
        sftp.put(input, sftpFileName);
        log.info("文件:{}上传成功" , sftpFileName);
        logout();
        if(input!=null) {
        	input.close();
        }
        Date endTime = new Date();
        log.info("本次创建连接一共耗时{}秒", (endTime.getTime()-startTime.getTime())/1000);
    }
  
    /** 
     * 上传单个文件
     *
     * @param directory 
     *            上传到sftp目录 
     * @param uploadFile
     *            要上传的文件,包括路径 
     * @throws SftpException
     * @throws IOException 
     * @throws JSchException 
     * @throws Exception
     */
    public void upload(String directory, String uploadFile) throws SftpException, IOException, JSchException{
        File file = new File(uploadFile);
        upload(new FileInputStream(file) , file.getName(), directory );
    }
    /**  
     * <p>Description: 上传文件到指定目录</p>  
     * @author zhouxy  
     * @date 2019年6月21日  下午4:09:04
     * @param directory
     * @param uploadFile
     * @param uploadFileName
     * @throws SftpException
     * @throws IOException
     * @throws JSchException 
     */
    public void upload(String directory, String uploadFile, String uploadFileName) throws SftpException, IOException, JSchException{
    	File file = new File(uploadFile);
    	upload(new FileInputStream(file) , uploadFileName, directory );
    }
  
    /**
     * 将byte[]上传到sftp，作为文件。注意:从String生成byte[]是，要指定字符集。
     * 
     * @param directory
     *            上传到sftp目录
     * @param sftpFileName
     *            文件在sftp端的命名
     * @param byteArr
     *            要上传的字节数组
     * @throws SftpException
     * @throws IOException 
     * @throws JSchException 
     * @throws Exception
     */
    public void upload(String directory, String sftpFileName, byte[] byteArr) throws SftpException, IOException, JSchException{
        upload(new ByteArrayInputStream(byteArr), sftpFileName,  directory);
    }
  
    /** 
     * 将字符串按照指定的字符编码上传到sftp
     *  
     * @param directory
     *            上传到sftp目录
     * @param sftpFileName
     *            文件在sftp端的命名
     * @param dataStr
     *            待上传的数据
     * @param charsetName
     *            sftp上的文件，按该字符编码保存
     * @throws SftpException
     * @throws IOException 
     * @throws JSchException 
     * @throws Exception
     */
    public void upload(String directory, String sftpFileName, String dataStr, String charsetName) throws SftpException, IOException, JSchException{  
        upload(new ByteArrayInputStream(dataStr.getBytes(charsetName)), sftpFileName, directory);  
    }
  
    /**
     * <p>Description: 下载文件至指定目录</p>  
     * @author zhouxy  
     * @date 2019年4月23日  下午6:07:54
     * @param downloadFile
     * @param saveFile
     * @throws SftpException
     * @throws IOException 
     * @throws JSchException 
     */
    public void download(String downloadFile, String saveFile) throws SftpException, IOException, JSchException{
    	Date startTime = new Date();
     	login();
        File file = new File(saveFile);
        log.info("开始下载文件:{}" , downloadFile);
        FileOutputStream fos = new FileOutputStream(file);
        sftp.get(downloadFile, fos);
        log.info("文件:{} 下载成功" , downloadFile);
        logout();
        if(fos !=null) {
        	fos.close();
        }
        Date endTime = new Date();
        log.info("本次下载一共耗时{}秒", (endTime.getTime()-startTime.getTime())/1000);
    }
    
    /**  
     * <p>Description: 下载文件返回文件流</p>  
     * @author zhouxy  
     * @date 2019年4月23日  下午6:07:37
     * @param downloadFile
     * @return
     * @throws SftpException
     * @throws IOException
     * @throws JSchException 
     */
    public InputStream download(String downloadFile) throws SftpException, IOException, JSchException{
        Date startTime = new Date();
    	login();
    	log.info("开始下载文件:{}" , downloadFile);
        InputStream is = sftp.get(downloadFile);
        
        log.info("文件:{} 下载成功" , downloadFile);
//        logout();
        Date endTime = new Date();
        log.info("本次下载一共耗时{}秒", (endTime.getTime()-startTime.getTime())/1000);
        return is;
    }
  
    /**
     * 删除文件
     *  
     * @param directory
     *            要删除文件所在目录
     * @param deleteFile
     *            要删除的文件
     * @throws SftpException
     * @throws Exception
     */
    public void delete(String directory, String deleteFile) throws SftpException{
        sftp.cd(directory);
        sftp.rm(deleteFile);
    }
  
    /**
     * 列出目录下的文件
     * 
     * @param directory
     *            要列出的目录
     * @param sftp
     * @return
     * @throws SftpException
     */
    public Vector<?> listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }
    
    public static void main(String[] args) throws SftpException, IOException, JSchException {
        SFTPUtils sftp = SFTPUtils.getInstance();
//        sftp.login();
        //byte[] buff = sftp.download("/opt", "start.sh");
        //System.out.println(Arrays.toString(buff));
//        File file = new File("D:\\upload\\index.html");
//        InputStream is = new FileInputStream(file);
        
//        sftp.upload("/data/work", "test_sftp_upload.csv", is);
        for (int i = 0; i < 1; i++) {
        	sftp.download("/upload/20190226/16cafa8a86c4300a9c60debd140fa318.xls");
			
		}
        SftpInfo sftpInfo = new SftpInfo();
        sftpInfo.setHost("192.168.96.50");
        sftpInfo.setUsername("mysftp");
        sftpInfo.setPassword("Wq1234!@#$");
        SFTPUtils.getInstance(sftpInfo).download("/upload/20190226/16cafa8a86c4300a9c60debd140fa318.xls");
//        sftp.logout();
    }
}
