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
import android.text.TextUtils;
import android.util.Log;


public class HttpUtil {
	
	private static final String TAG = "intent04.HttpUtil";
	
	
	/***
	 * 指定されたURLにHTTP GETリクエストを行ない、結果のHTMLからタイトルを抽出する。
	 * タイトルに含まれる数値文字参照、実体参照は変換してから返す。
	 * 
	 * @param url
	 * @param msg_no_title タイトルが見つからない(もしくはエラーになった)場合に返す文字列
	 * @return ページタイトル
	 */
	public static String getTitle(String url, String msg_no_title) {
		String title = msg_no_title;
		String html = getHtml(url);
		if (html == null) return title;
		
		String html_lower = html.toLowerCase();

		int startIndex = -1, p = 0;
		int endIndex = html_lower.indexOf("</title>");				//まず終了タグを探す。
		if (endIndex >= 0){ 
			html_lower = html_lower.substring(0, endIndex);

			p = html_lower.indexOf("<title>");						//開始タグを探す。
			if (p >= 0){
				startIndex = p+7;
			} else {
				p = html_lower.indexOf("<title ");					//開始タグにidなどの属性が付いている場合があるのでその対応。
				if (p > 0) {
					p = html_lower.indexOf(">", p+7);
					if (p > 0) startIndex = p+1;
				}
			}
			if (startIndex >=0 && endIndex >= 0){
				//見つかった始点から終点までを抜き出す。同時に改行と空白を除去する。
				title = html.substring(startIndex, endIndex)
							.replace("\n", "")
							.replace("\r", "")
							.trim();
				
				title = NCR2String(title);				//数値文字参照を変換
				title = convertRefString(title);		//実体参照を変換
			}
		} 
		return title;
	}
	
	/***
	 * 指定されたURLにHTTP GETリクエストを行ない結果を取得する。
	 * 
	 * @param url
	 * @return HTML文字列
	 */
	public static String getHtml(String url){
		String result = null;
		
		HttpGet httpGet = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		
	    HttpConnectionParams.setConnectionTimeout(httpParams, 1000 * 10);		//接続のタイムアウト（単位：ms）
	    HttpConnectionParams.setSoTimeout(httpParams, 1000 * 60);				//データ取得のタイムアウト（単位：ms）   		
		client.setParams(httpParams);
	    
		try {
			// レスポンスを取得
			HttpResponse httpResponse = client.execute(httpGet);
			int status = httpResponse.getStatusLine().getStatusCode();

			if (HttpStatus.SC_OK == status){
				//Content-Typeを取得
				Header[] headers = httpResponse.getHeaders("Content-Type");
				if (headers.length > 0) { 
					String contentType = "";
					contentType = headers[0].getValue();
					if (contentType.contains("text/html")){

						//HTMLを取得
						HttpEntity entity = httpResponse.getEntity();
						if (entity != null){
							byte[] arr = EntityUtils.toByteArray(entity);
							
							//文字エンコーディングを判定
							String encoding = detectEncoding(arr);
							if (encoding == null) encoding = findEncoding(entity, arr);
							
							//判定されたエンコーディングでバイト配列から文字列に変換
							result = new String(arr, encoding);
							
							//entityのリソースを解放
							entity.consumeContent();			
						}
					}
				}
			}
		
		} catch (ClientProtocolException e) {
			Log.d(TAG, e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
		} finally {
			//HTTPクライアントを終了させる
			client.getConnectionManager().shutdown();
		}
		
		return result;
	}
	
	//UniversalDetectorクラスを使ってエンコーディングを判別する。
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
			Log.d(TAG, e.getMessage());
		}
		detector.dataEnd();
		return detector.getDetectedCharset();
	}
		
	private static String findEncoding(HttpEntity entity, byte[] arr){

		//HTTP Responseヘッダでエンコーディングが示されている場合はそれを返す。
		Header enc = entity.getContentEncoding();
		if (enc != null){
			String header_encoding = enc.getName();
			if (header_encoding != null){
				return header_encoding;				
			}
		} 

		//charset属性を含むmetaタグを探してその値を返す。
		String encoding = "UTF-8";
		try {
			String result = new String(arr, encoding);		//一旦UTF-8で文字列に変換。

			String regexp1 = "<meta (.*)charset(.*)";		
			Pattern pattern = Pattern.compile(regexp1);
			Matcher matcher = pattern.matcher(result);
			while (matcher.find()) {
				String metaline = matcher.group(2);

				//例：　<meta http-equiv="Content-Type" content="text/html; charset=Shift_JIS" />		
				if (metaline.toLowerCase().contains("shift_jis")){
					encoding = "Shift_JIS";
					break;
				}

				//例：　<meta http-equiv="Content-Type" content="text/html; charset=euc-jp" />
				if (metaline.toLowerCase().contains("euc-jp")){
					encoding = "euc-jp";
					break;
				}
			}
		} catch (UnsupportedEncodingException e) {
			Log.d(TAG, e.getMessage());
		}
		return encoding;
	} 
	

	/***
	 * 文字列に含まれる数値文字参照を変換する。
	 * 例：　「&#65374;」→ 「〜」
	 * 
	 * 参照元：	http://www.atmarkit.co.jp/bbs/phpBB/viewtopic.php?topic=12493&forum=12&start=8
	 * 			http://www.free-drive.net/web/?p=325
	 */
	private static String NCR2String(String str) { 
		if (TextUtils.isEmpty(str)) return str;
		
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
	
	/***
	 * 文字列に含まれる実体参照を変換する。(一部のみ対応)
	 * 
	 * 実体参照の一覧はこちら：  http://code.cside.com/3rdpage/jp/entity/converter.html
	 */
	private static String convertRefString(String s){
		if (TextUtils.isEmpty(s)) return s;
		
		s = s.replace("&quot;", "\"")
			.replace("&amp;", "&")
			.replace("&lt;", "<")
			.replace("&gt;", ">")
			.replace("&nbsp;", " ")
			.replace("&yen;", "\\")
			.replace("&brvbar;", "¦")
			.replace("&copy;", "©")
			.replace("&hellip;", "…")
			.replace("&reg;", "®")
			.replace("&ndash;", "–")
			.replace("&mdash;", "—")
			.replace("&lsquo;", "‘")
			.replace("&rsquo;", "’")
			.replace("&ldquo;", "“")
			.replace("&rdquo;", "”")
			.replace("&bull;", "•");
		return s;
	}
	
}
