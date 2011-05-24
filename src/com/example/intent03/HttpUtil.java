package com.example.intent03;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtil {
	
	
	public static String getHtml(String url){
		String result = null;
		String encoding = "UTF-8";
		
		HttpGet httpGet = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		try {
			// レスポンスを取得
			httpResponse = client.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null){
				byte[] arr = EntityUtils.toByteArray(entity);
				encoding = findEncoding(entity, arr);
				result = new String(arr, encoding);
				entity.consumeContent();			// entityのリソースを解放
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// クライアントを終了させる
		client.getConnectionManager().shutdown();
		
		return result;
	}
	
	private static String findEncoding(HttpEntity entity, byte[] arr){
		String encoding = "UTF-8";
		
		Header enc = entity.getContentEncoding();
		if (enc != null) encoding = enc.getName();
		
		try {
			String result = new String(arr, encoding);

			String regexp1 = "<meta (.*)";
			Pattern pattern = Pattern.compile(regexp1);
			Matcher matcher = pattern.matcher(result);
			while (matcher.find()) {
				String metaline = matcher.group(1);

				//<meta http-equiv="Content-Type" content="text/html; charset=Shift_JIS" />		
				if (metaline.toLowerCase().contains("charset=shift_jis")){
					encoding = "Shift_JIS";
					break;
				}

				//<meta http-equiv="Content-Type" content="text/html; charset=euc-jp" />
				if (metaline.toLowerCase().contains("charset=euc-jp")){
					encoding = "euc-jp";
					break;
				}
			}

			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return encoding;
	} 
	
	public static String getTitle(String url) {
		String html = getHtml(url);
		if (html == null) return null;
		
		String html_lower = html.toLowerCase();
		int p = html_lower.indexOf("</head>");
		html_lower = html_lower.substring(0, p).replace("\n", "").replace("\r", "");
		String title = null;

		String regexp1 = "<title>(.*)</title>";
		Pattern pattern = Pattern.compile(regexp1);
		Matcher matcher = pattern.matcher(html_lower);
		while (matcher.find()) {
			title = matcher.group(1);
		}
		title =  NCR2String(title);
		
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
