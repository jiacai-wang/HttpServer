/*
 * 0���ж�url�Ƿ����?�����������Ϊ��url��get
 * 1��Ŀ¼����ȫΪindex.html�����ж�������Դ�Ƿ���ڣ���ȫhttp��Ӧͷ״̬��
 * 2��ͨ�������׺������MIMEType����ȫContent-Type�ֶ�
 * 3��������Ӧͷ������(����)
 * 4����������ļ����ӵ���Ӧͷ
 * 5����get����ǿգ�����getHandler(response, get)���д���
 * 6����body�ǿգ�����postHandler(response, body)���д���
 * 7�����ش�����response
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
		String responseStartLine = "HTTP/1.1 ";				//�̶���Ӧͷ
		String urlHeader = "C:/Users/15761/Desktop/HTTP";			//��������Ŀ¼
		String responseHeader = "Content-Type: ";			//��Ӧ����
		String get = new String();
		String body = new String();
		String extension = new String();
		
		if(parsedRequest[1].indexOf('?')>-1)		//��url�����get����
		{
			get = parsedRequest[1].substring(parsedRequest[1].indexOf('?')+1);
			parsedRequest[1]=parsedRequest[1].substring(0,parsedRequest[1].indexOf('?'));
		}
		
		if(parsedRequest[1].endsWith("/")) parsedRequest[1]+="index.html";			//�Զ���ȫĬ��ҳindex.html
		body = parsedRequest[3];
		
		//����̨������Ϣ
		System.out.println("method: "+parsedRequest[0]+"\nurl: "+parsedRequest[1]);
		System.out.println("get: " + get);
		System.out.println("Accept: "+parsedRequest[2]+"\nbody: "+parsedRequest[3]);
		
		
		//�ж������ļ��Ƿ���ڲ���ȫ��Ӧͷ��״̬�룬ie. 200 OK, 404 NotFound ...
		File requestedFile = new File( urlHeader + parsedRequest[1]);
		if(!requestedFile.exists()||requestedFile.isDirectory())
		{
			requestedFile = new File(urlHeader + "/404.html");
			responseStartLine+="404 NotFound\r\n";
		}
		else responseStartLine+="200 OK\r\n";
		
		//ͨ�������url��׺���ж�MIMEType����ȫContent-Type
		extension = requestedFile.getPath().substring(requestedFile.getPath().lastIndexOf('.')+1);
		responseHeader+=getMIMEType(extension);
		
		response = getResponseByteArray(requestedFile.toPath(), responseStartLine, responseHeader);
		
		//����ӦΪhtmlҳ�棬��ӡ������̨
		//if(getMIMEType(extension).indexOf("text")>-1)System.out.println(new String(response, "UTF-8"));
		
		//����get��post����ı�ҳ������ ����ʵ�����ݿ��ѯ����
		if(!get.isEmpty()) response = getHandler(response, get);
//		response = getHandler(response, get);
		if(!body.isEmpty()) response = postHandler(response, body);
//		response = postHandler(response, body);
		
		return response;
	}




	//����mime�ļ��к�׺����Ӧ��MIMEType
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
	
	//���������С�����ͷ�������ļ���ϳ�������Ӧ
	private static byte[] getResponseByteArray(Path requestedFilePath, String responseStartLine, String responseHeader)
	{
		byte[] response = null;
		System.out.println("opening file: "+requestedFilePath.toString());
		byte[] content = null;
		byte[] header = (responseStartLine + responseHeader +"\r\n\r\n").getBytes();		//��Ӧͷ����Ӧ����֮�����ӿ���
		try {
			content = Files.readAllBytes(requestedFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//��ʼ��response�Ĵ�С������Ӧhead��body���
		response = new byte[content.length+ header.length];
		System.arraycopy(header, 0, response, 0, header.length);
		System.arraycopy(content, 0, response, header.length, content.length);
		return response;
	}

	//����get���󣬵���SQLHandler�滻��̬ҳ���ڵ�ռλ�ַ���
	private static byte[] getHandler(byte[] response, String get) throws UnsupportedEncodingException {
		String temp = new String(response, "UTF-8");
		String name = new String("null");
		String id = new String("null");
		String SQLResult = new String();
		
		//��ȡget�����ڵ�name��/��id�ֶ�ֵ
		for(int i=0;i<get.split("&").length;i++)
		{
			if(get.split("&")[i].trim().startsWith("name")&&!get.split("&")[i].trim().endsWith("=")) name = get.split("&")[i].split("=")[1];
			if(get.split("&")[i].trim().startsWith("id")&&!get.split("&")[i].trim().endsWith("=")) id = get.split("&")[i].split("=")[1];
		}

		SQLResult = SQLHandler(name, id, "get");
		//System.out.println("name: "+name+"\nid: "+id);
		
		//�滻��̬ҳ���ڵ�"__name__"�ַ���Ϊnameֵ��"__id__"�ַ���Ϊidֵ
		temp = temp.replaceAll("__name__", name);
		temp = temp.replaceAll("__id__", id);
		temp = temp.replaceAll("__SQL__", SQLResult);
		//�ַ���תΪ�����͵��ֽ�����
		response = temp.getBytes("UTF-8");
		
		return response;
	}
	
	//����post���󣬵���SQLHandler�滻��̬ҳ���ڵ�ռλ�ַ���
	private static byte[] postHandler(byte[] response, String body) throws UnsupportedEncodingException {
		//����post����
		String temp = new String(response, "UTF-8");
		String name = new String("null");
		String id = new String("null");
		String SQLResult = new String();

		//��ȡpost�����ڵ�name��/��id�ֶ�ֵ
		for(int i=0;i<body.split("&").length;i++)
		{
			if(body.split("&")[i].trim().startsWith("name")&&!body.split("&")[i].trim().endsWith("=")) name = body.split("&")[i].split("=")[1];
			if(body.split("&")[i].trim().startsWith("id")&&!body.split("&")[i].trim().endsWith("=")) id = body.split("&")[i].split("=")[1];
		}
		

		SQLResult = SQLHandler(name, id, "post");

		//System.out.println("name: "+name+"\nid: "+id);
		
		//�滻��̬ҳ���ڵ�"__name__"�ַ���Ϊnameֵ��"__id__"�ַ���Ϊidֵ
		temp = temp.replaceAll("__name__", name);
		temp = temp.replaceAll("__id__", id);
		temp = temp.replaceAll("__SQL__", SQLResult);
		//�ַ���תΪ�����͵��ֽ�����
		response = temp.getBytes("UTF-8");
		
		return response;
	}
	
	//������ݿ����
	//�ⲿ�ִ�������
	private static String SQLHandler(String name, String id, String method)
	{
		String result = new String();
		Connection c = null;
	    Statement stmt = null;
	    
	    //����name��Ϣ����ѯname��Ӧѧ��
	    if(!name.contains("null")&&id.contains("null"))
	    {
	    	if(method=="get")		//get����Ϸ�
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
			    if(result.isEmpty()) result = "���޴���";
	    	}
	    	else if(method=="post")		//post��������id��nameΪ��
	    	{
	    		result = "name��idΪ�գ�";
	    	}
	    	else result = "��ϲ�㴥����һ��bug";
	    }
	    
	    //����id��Ϣ����ѯ����id�����м�¼
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
	    	else if(method=="post")		//post��������id��nameΪ��
	    	{
	    		result = "name��idΪ�գ�";
	    	}
	    	else result = "��ϲ�㴥����һ��bug";
	    }
	    
	    //ͬʱ��name��id��Ϣ���ж��Ƿ��Ѵ��ڣ���ִ��insert���
	    if(!name.contains("null")&&!id.contains("null"))
	    {
	    	if(method=="post")		//post�Ϸ�
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
				        	result = "���ݿ�д��ʧ�ܣ��Ѵ���������Ϣ��<br>" + rs.getString("name") + ": " + rs.getString("id");
				        }
				        else
				        {
				        	System.out.println("now inserting");
				        	sql = "insert into student(id,name) values(\"" + id + "\",\"" + name + "\");";
				        	stmt.executeUpdate(sql);
				        	result = "������Ϣ�ѱ���¼";
				        	c.commit();
				        }
				        stmt.close();
				        c.close();
				      } catch ( Exception e ) {
				        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
				        System.exit(0);
				      }
	    	}
	    	else if(method=="get")		//get��������name��idͬʱ����
	    	{
	    		result = "��ϲ�㴥����һ��bug";
	    	}
	    	else result = "��ϲ�㴥����һ��bug";
	    }
	    
	    //name��idͬΪ�գ�������ʾ
	    if(name.contains("null")&&id.contains("null")) result = "��δ������Ϣ";
	    
	    
		return result;
	}

	
}


