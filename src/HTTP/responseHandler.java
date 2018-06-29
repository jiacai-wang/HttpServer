/*
 * 0，判断url是否包含?，若有则分离为纯url和get
 * 1，目录请求补全为index.html请求，判断请求资源是否存在，补全http响应头状态码
 * 2，通过请求后缀名查找MIMEType，补全Content-Type字段
 * 3，至此响应头已完整(能用)
 * 4，将请求的文件连接到响应头
 * 5，若get请求非空，调用getHandler(response, get)进行处理
 * 6，若body非空，调用postHandler(response, body)进行处理
 * 7，返回处理后的response
 * 
 */


package HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;



public class responseHandler {
	public static byte[] getResponse(String[] parsedRequest) throws IOException
	{
		byte[] response;
		String responseStartLine = "HTTP/1.1 ";				//固定响应头
		String urlHeader = "C:/Users/15761/Desktop/HTTP";			//服务器根目录
		String responseHeader = "Content-Type: ";			//响应类型
		String get = new String();
		String body = new String();
		String extension = new String();
		
		if(parsedRequest[1].indexOf('?')>-1)		//从url分离出get请求
		{
			get = parsedRequest[1].substring(parsedRequest[1].indexOf('?')+1);
			parsedRequest[1]=parsedRequest[1].substring(0,parsedRequest[1].indexOf('?'));
		}
		
		if(parsedRequest[1].endsWith("/")) parsedRequest[1]+="index.html";			//自动补全默认页index.html
		body = parsedRequest[3];
		
		//控制台调试信息
		System.out.println("method: "+parsedRequest[0]+"\nurl: "+parsedRequest[1]);
		System.out.println("get: " + get);
		System.out.println("Accept: "+parsedRequest[2]+"\nbody: "+parsedRequest[3]);
		
		
		//判断请求文件是否存在并补全响应头的状态码，ie. 200 OK, 404 NotFound ...
		File requestedFile = new File( urlHeader + parsedRequest[1]);
		if(!requestedFile.exists()||requestedFile.isDirectory())
		{
			requestedFile = new File(urlHeader + "/404.html");
			responseStartLine+="404 NotFound\r\n";
		}
		else responseStartLine+="200 OK\r\n";
		
		//通过请求的url后缀名判断MIMEType，补全Content-Type
		extension = requestedFile.getPath().substring(requestedFile.getPath().lastIndexOf('.')+1);
		responseHeader+=getMIMEType(extension);
		
		response = getResponseByteArray(requestedFile.toPath(), responseStartLine, responseHeader);
		
		//若响应为html页面，打印到控制台
		//if(getMIMEType(extension).indexOf("text")>-1)System.out.println(new String(response, "UTF-8"));
		
		//处理get和post请求改变页面内容 ，并实现数据库查询功能
		if(!get.isEmpty()) response = getHandler(response, get);
//		response = getHandler(response, get);
		if(!body.isEmpty()) response = postHandler(response, body);
//		response = postHandler(response, body);
		
		return response;
	}




	//查找mime文件中后缀名对应的MIMEType
	private static String getMIMEType(String extension) {
		String MIMEType = new String();
		try (InputStream in = Files.newInputStream((new File("C:/Users/15761/Desktop/HTTP/mime.txt")).toPath());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8")))
		{
		    String line = null;
		    reader.read();
		    while ((line = reader.readLine()) != null)
		    {
		    	if(line.startsWith(extension)) MIMEType = line.split("\t")[1];
		    }
		} catch (IOException x) {
		    System.err.println(x);
		}
		if(MIMEType.isEmpty()) MIMEType = "none";
		return MIMEType;
	}
	
	//将请求首行、请求头、请求文件组合成完整响应
	private static byte[] getResponseByteArray(Path requestedFilePath, String responseStartLine, String responseHeader)
	{
		byte[] response = null;
		System.out.println("opening file: "+requestedFilePath.toString());
		byte[] content = null;
		byte[] header = (responseStartLine + responseHeader +"\r\n\r\n").getBytes();		//响应头和响应内容之间增加空行
		try {
			content = Files.readAllBytes(requestedFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//初始化response的大小并将响应head和body组合
		response = new byte[content.length+ header.length];
		System.arraycopy(header, 0, response, 0, header.length);
		System.arraycopy(content, 0, response, header.length, content.length);
		return response;
	}

	//处理get请求，调用SQLHandler替换静态页面内的占位字符串
	private static byte[] getHandler(byte[] response, String get) throws UnsupportedEncodingException {
		String temp = new String(response, "UTF-8");
		String name = new String("null");
		String id = new String("null");
		String SQLResult = new String();
		
		//获取get请求内的name和/或id字段值
		for(int i=0;i<get.split("&").length;i++)
		{
			if(get.split("&")[i].trim().startsWith("name")&&!get.split("&")[i].trim().endsWith("=")) name = get.split("&")[i].split("=")[1];
			if(get.split("&")[i].trim().startsWith("id")&&!get.split("&")[i].trim().endsWith("=")) id = get.split("&")[i].split("=")[1];
		}

		SQLResult = SQLHandler(name, id, "get");
		//System.out.println("name: "+name+"\nid: "+id);
		
		//替换静态页面内的"__name__"字符串为name值，"__id__"字符串为id值
		temp = temp.replaceAll("__name__", name);
		temp = temp.replaceAll("__id__", id);
		temp = temp.replaceAll("__SQL__", SQLResult);
		//字符串转为待发送的字节数组
		response = temp.getBytes("UTF-8");
		
		return response;
	}
	
	//处理post请求，调用SQLHandler替换静态页面内的占位字符串
	private static byte[] postHandler(byte[] response, String body) throws UnsupportedEncodingException {
		//处理post请求
		String temp = new String(response, "UTF-8");
		String name = new String("null");
		String id = new String("null");
		String SQLResult = new String();

		//获取post请求内的name和/或id字段值
		for(int i=0;i<body.split("&").length;i++)
		{
			if(body.split("&")[i].trim().startsWith("name")&&!body.split("&")[i].trim().endsWith("=")) name = body.split("&")[i].split("=")[1];
			if(body.split("&")[i].trim().startsWith("id")&&!body.split("&")[i].trim().endsWith("=")) id = body.split("&")[i].split("=")[1];
		}
		

		SQLResult = SQLHandler(name, id, "post");

		//System.out.println("name: "+name+"\nid: "+id);
		
		//替换静态页面内的"__name__"字符串为name值，"__id__"字符串为id值
		temp = temp.replaceAll("__name__", name);
		temp = temp.replaceAll("__id__", id);
		temp = temp.replaceAll("__SQL__", SQLResult);
		//字符串转为待发送的字节数组
		response = temp.getBytes("UTF-8");
		
		return response;
	}
	
	//完成数据库操作
	//这部分代码贼烂
	private static String SQLHandler(String name, String id, String method)
	{
		String result = new String();
		Connection c = null;
	    Statement stmt = null;
	    
	    //仅有name信息，查询name对应学号
	    if(!name.contains("null")&&id.contains("null"))
	    {
	    	if(method=="get")		//get请求合法
	    	{
			    try {
			        Class.forName("org.sqlite.JDBC");
			        c = DriverManager.getConnection("jdbc:sqlite:C:/Users/15761/Desktop/HTTP/student.db");
			        c.setAutoCommit(false);
			        System.out.println("Opened database successfully");
		
			        stmt = c.createStatement();
			        String sql = new String();
			        sql = "select * from student where name =\""+name+"\";";
			        ResultSet rs = stmt.executeQuery(sql);
			        while(rs.next())
			        {
			        	result = result + rs.getString("id") + "<br>";
			        }
			        stmt.close();
			        c.close();
			      } catch ( Exception e ) {
			        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			        System.exit(0);
			      }
			    if(result.isEmpty()) result = "查无此人";
	    	}
	    	else if(method=="post")		//post请求不允许id或name为空
	    	{
	    		result = "name或id为空！";
	    	}
	    	else result = "恭喜你触发了一个bug";
	    }
	    
	    //仅有id信息，查询包含id的所有记录
	    if(name.contains("null")&&!id.contains("null"))
	    {
	    	if(method=="get")
	    	{
			    try {
			        Class.forName("org.sqlite.JDBC");
			        c = DriverManager.getConnection("jdbc:sqlite:C:/Users/15761/Desktop/HTTP/student.db");
			        c.setAutoCommit(false);
			        System.out.println("Opened database successfully");
		
			        stmt = c.createStatement();
			        String sql = new String();
			        sql = "select * from student where id like\"%" + id + "%\";";
			        ResultSet rs = stmt.executeQuery(sql);
			        while(rs.next())
			        {
			        	result = result + "<br>" + rs.getString("name") + ": " + rs.getString("id");
			        }
			        stmt.close();
			        c.close();
			      } catch ( Exception e ) {
			        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			        System.exit(0);
			      }
	    	}
	    	else if(method=="post")		//post请求不允许id或name为空
	    	{
	    		result = "name或id为空！";
	    	}
	    	else result = "恭喜你触发了一个bug";
	    }
	    
	    //同时有name和id信息，判断是否已存在，并执行insert语句
	    if(!name.contains("null")&&!id.contains("null"))
	    {
	    	if(method=="post")		//post合法
	    	{
		    	System.out.println("name: "+name+" id: "+id);
		    	 try {
				        Class.forName("org.sqlite.JDBC");
				        c = DriverManager.getConnection("jdbc:sqlite:C:/Users/15761/Desktop/HTTP/student.db");
				        c.setAutoCommit(false);
				        System.out.println("Opened database successfully");
			
				        stmt = c.createStatement();
				        String sql = new String();
				        sql = "select * from student where id =\"" + id + "\";";
				        ResultSet rs = stmt.executeQuery(sql);
				        if(rs.next())
				        {
				        	result = "数据库写入失败，已存在如下信息：<br>" + rs.getString("name") + ": " + rs.getString("id");
				        }
				        else
				        {
				        	System.out.println("now inserting");
				        	sql = "insert into student(id,name) values(\"" + id + "\",\"" + name + "\");";
				        	stmt.executeUpdate(sql);
				        	result = "您的信息已被记录";
				        	c.commit();
				        }
				        stmt.close();
				        c.close();
				      } catch ( Exception e ) {
				        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
				        System.exit(0);
				      }
	    	}
	    	else if(method=="get")		//get请求不允许name和id同时存在
	    	{
	    		result = "恭喜你触发了一个bug";
	    	}
	    	else result = "恭喜你触发了一个bug";
	    }
	    
	    //name和id同为空，给出提示
	    if(name.contains("null")&&id.contains("null")) result = "您未输入信息";
	    
	    
		return result;
	}

	
}


