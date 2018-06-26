package HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


public class responseHandler {
	public static byte[] getResponse(String[] parsedRequest) throws IOException
	{
		byte[] response;
		String responseStartLine = "HTTP/1.1 ";
		String urlHeader = "C:/Users/15761/Desktop/HTTP";
		String responseHeader = "Content-Type: ";
		String get = new String();
		String extension = new String();
		
		if(parsedRequest[1].indexOf('?')>-1)
		{
			get = parsedRequest[1].substring(parsedRequest[1].indexOf('?')+1);
			parsedRequest[1]=parsedRequest[1].substring(0,parsedRequest[1].indexOf('?'));
		}
		
		if(parsedRequest[1].endsWith("/")) parsedRequest[1]+="index.html";

		System.out.println("method: "+parsedRequest[0]+"\nurl: "+parsedRequest[1]);
		System.out.println("get: " + get);
		System.out.println("Accept: "+parsedRequest[2]+"\nbody: "+parsedRequest[3]);
		
		File requestedFile = new File( urlHeader + parsedRequest[1]);
		if(!requestedFile.exists()||requestedFile.isDirectory())
		{
			requestedFile = new File(urlHeader + "/404.html");
			responseStartLine+="404 NotFound\r\n";
		}
		else responseStartLine+="200 OK\r\n";
		extension = requestedFile.getPath().substring(requestedFile.getPath().lastIndexOf('.')+1);
		responseHeader+=getMIMEType(extension);
		response = getResponseByteArray(requestedFile.toPath(), responseStartLine, responseHeader);
		if(getMIMEType(extension).indexOf("text")>-1)System.out.println(new String(response, "UTF-8"));
		
		return response;
	}

	
	

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
	
	private static byte[] getResponseByteArray(Path requestedFilePath, String responseStartLine, String responseHeader)
	{
		byte[] response = null;
		System.out.println("opening file: "+requestedFilePath.toString());
		byte[] content = null;
		byte[] header = (responseStartLine + responseHeader +"\r\n\r\n").getBytes();
		try {
			content = Files.readAllBytes(requestedFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		response = new byte[content.length+ header.length];
		System.arraycopy(header, 0, response, 0, header.length);
		System.arraycopy(content, 0, response, header.length, content.length);
		return response;
	}

	private static byte[] getHandler(byte[] response, String get) {
		
		
		return null;
		
	}
}


