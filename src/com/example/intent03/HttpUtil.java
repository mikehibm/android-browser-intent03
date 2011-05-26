package com.example.intent03;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.mozilla.universalchardet.UniversalDetector;

import android.util.Log;

public class HttpUtil {
	
	public static String getHtml(String url){
		String result = null;
		String encoding = "UTF-8";
		
		HttpGet httpGet = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		
	    HttpConnectionParams.setConnectionTimeout(httpParams, 1000 * 10);		//接続のタイムアウト（単位：ms）
	    HttpConnectionParams.setSoTimeout(httpParams, 1000 * 30);				//データ取得のタイムアウト（単位：ms）   		
		client.setParams(httpParams);
	    
		try {
			// レスポンスを取得
			HttpResponse httpResponse = client.execute(httpGet);
			int status = httpResponse.getStatusLine().getStatusCode();

			if (HttpStatus.SC_OK == status){
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null){
					byte[] arr = EntityUtils.toByteArray(entity);
					encoding = detectEncoding(arr);
					if (encoding == null) encoding = findEncoding(entity, arr);
					result = new String(arr, encoding);
					entity.consumeContent();			// entityのリソースを解放
				}
			}
		
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// クライアントを終了させる
			client.getConnectionManager().shutdown();
		}
		
		return result;
	}
	
	private static String findEncoding(HttpEntity entity, byte[] arr){
		String encoding = "UTF-8";
		
		Header enc = entity.getContentEncoding();
		if (enc != null){ 
			encoding = enc.getName();
		} else {
			try {
				String result = new String(arr, encoding);

				String regexp1 = "<meta (.*)charset(.*)";
				Pattern pattern = Pattern.compile(regexp1);
				Matcher matcher = pattern.matcher(result);
				while (matcher.find()) {
					String metaline = matcher.group(2);

					//<meta http-equiv="Content-Type" content="text/html; charset=Shift_JIS" />		
					if (metaline.toLowerCase().contains("shift_jis")){
						encoding = "Shift_JIS";
						break;
					}

					//<meta http-equiv="Content-Type" content="text/html; charset=euc-jp" />
					if (metaline.toLowerCase().contains("euc-jp")){
						encoding = "euc-jp";
						break;
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return encoding;
	} 
	
	private static String detectEncoding(byte[] arr){
       byte[] buf = new byte[4096];
       ByteArrayInputStream stream = new ByteArrayInputStream(arr);
		UniversalDetector detector = new UniversalDetector(null);

		try {
			int nread;
			while ((nread = stream.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
       } catch (IOException e) {
    	   e.printStackTrace();
        }
       detector.dataEnd();
		return detector.getDetectedCharset();
	}
	
	public static String getTitle(String url, String msg_no_title) {
		String title = msg_no_title;
		String html = getHtml(url);
		if (html == null) return title;
		
		String html_lower = html.toLowerCase();

		int startIndex = -1, p = 0;
		int endIndex = html_lower.indexOf("</title>");
		if (endIndex >= 0){ 
			html_lower = html_lower.substring(0, endIndex);

			p = html_lower.indexOf("<title>");
			if (p >= 0){
				startIndex = p+7;
			} else {
				p = html_lower.indexOf("<title ");
				if (p > 0) {
					p = html_lower.indexOf(">", p+7);
					if (p > 0) startIndex = p+1;
				}
			}
			if (startIndex >=0 && endIndex >= 0){
				title = html.substring(startIndex, endIndex)
							.replace("\n", "").replace("\r", "")
							.trim();
				title =  NCR2String(title);
			}
		} 
		return title;
	}
	

	//タイトルに含まれる数値文字参照を変換する。
	// 例：　「&#65374;」→ 「〜」
	static String NCR2String(String str) { 
		if (str == null) return str;
		
		String ostr = new String(); 
		int i1 = 0; 
		int i2 = 0; 

		while (i2 < str.length()) { 
			i1 = str.indexOf("&#", i2); 
			if (i1 == -1) { 
				ostr += str.substring(i2, str.length()); 
				break; 
			} 
			ostr += str.substring(i2, i1); 
			i2 = str.indexOf(";", i1); 
			if (i2 == -1) { 
				ostr += str.substring(i1, str.length()); 
				break; 
			} 

			String tok = str.substring(i1 + 2, i2); 
			try { 
				int radix = 10; 
				if (tok.trim().charAt(0) == 'x') { 
					radix = 16; 
					tok = tok.substring(1, tok.length()); 
				} 
				ostr += (char) Integer.parseInt(tok, radix); 
			} 
			catch (NumberFormatException exp) { 
				ostr += '?'; 
			} 
			i2++; 
		} 
		return ostr; 
	}
	
}
