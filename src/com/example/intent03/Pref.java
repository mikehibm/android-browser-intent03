package com.example.intent03;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Pref  extends PreferenceActivity{
	
	private static final String KEY_TO_ADDR1 = "to_addr1";
	private static final String KEY_PREFIX = "prefix";
	private static final String KEY_FOOTER = "footer";
	private static final String KEY_OPEN_BROWSER = "open_browser";
	
	private SharedPreferences mSharedPreferences = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.pref);
        mSharedPreferences =  getPreferenceScreen().getSharedPreferences();

        dispSummary();
        setResult(RESULT_OK, null);        
    }
    
	public static String getToAddr1(Context con){  
    	String value = PreferenceManager.getDefaultSharedPreferences(con).getString(KEY_TO_ADDR1, con.getString(R.string.pref_to_addr1_default));
		if (value == null) value = "";
    	return value;
    }  
    
    public static String getPrefix(Context con){  
		String value = PreferenceManager.getDefaultSharedPreferences(con).getString(KEY_PREFIX, con.getString(R.string.pref_prefix_default));
		if (value == null) value = "";
		if (!value.endsWith(" ")) value += " ";
		return value;
    }  

    public static String getFooter(Context con){  
    	String value = PreferenceManager.getDefaultSharedPreferences(con).getString(KEY_FOOTER, con.getString(R.string.pref_footer_default));
    	if (value == null) value = "";
    	return value;
    }
    
    public static boolean getOpenBrowser(Context con){  
    	boolean value = PreferenceManager.getDefaultSharedPreferences(con).getBoolean(KEY_OPEN_BROWSER, true);
    	return value;
    }
    
	private void dispSummary(){
		String s;
		s = mSharedPreferences.getString(KEY_TO_ADDR1, getString(R.string.pref_to_addr1_default)) 
								+ "\n" + getString(R.string.pref_to_addr1_summary);
		findPreference(KEY_TO_ADDR1).setSummary(s);
		
		s = mSharedPreferences.getString(KEY_PREFIX, getString(R.string.pref_prefix_default));
		findPreference(KEY_PREFIX).setSummary(s);
		
		s = mSharedPreferences.getString(KEY_FOOTER, getString(R.string.pref_footer_default));
		findPreference(KEY_FOOTER).setSummary(s);
		
    }
    
    @Override  
    protected void onResume() {  
        super.onResume();  
        mSharedPreferences =  getPreferenceScreen().getSharedPreferences();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);  
    }  
       
    @Override  
    protected void onPause() {  
        super.onPause();  
        mSharedPreferences =  getPreferenceScreen().getSharedPreferences();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);  
    }  
    
    private SharedPreferences.OnSharedPreferenceChangeListener listener =   
        new SharedPreferences.OnSharedPreferenceChangeListener() {  
           
		public void onSharedPreferenceChanged(SharedPreferences sp, String key) {  
			dispSummary();
		}  
    };
   
}
