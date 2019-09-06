package com.ctbcbank.detaillog.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.ctbcbank.detaillog.encrypt.DES;
import com.ctbcbank.detaillog.properties.DetailLogProperties;
import com.ctbcbank.detaillog.tools.ConsoleProgressBar;

@Component
public class DetailLog {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private DetailLogProperties detailLogProperties;
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public Boolean execute(String logDate) {
		String line;
		String sql = StringUtils.EMPTY;
		//執行成功的sql行數
		int successInsertSqlCount=0;
		//每一份文件的sql執行行數
		int perLogSqlCount = 0;
		//讀取的sql總行數
		int readSqlCount = 0;
		Boolean status;
		String hostAddress = StringUtils.EMPTY;
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			hostAddress = iAddress.getHostAddress();
			params.put("Date", logDate);
			params.put("HostAddress", hostAddress);
			List<?> list = namedParameterJdbcTemplate.queryForList(detailLogProperties.getSelectStatusSql(), params);
			if(list.isEmpty())
				namedParameterJdbcTemplate.update(detailLogProperties.getInsertStatusSql(), params);
			logger.info(logDate.replace("-", "") + "-detailLog");
			int NumFile = checkFile(logDate.replace("-", ""), detailLogProperties.getLogPath());
			//每個資料的處理
			List<String> sqlArray = new ArrayList<String>();
			long time = System.currentTimeMillis();
			for(int i = 0;i <= (NumFile-1); i++) {
				perLogSqlCount = 0;
				String readFileName = logDate.replace("-", "") + "." + i + ".txt";
//				取得檔案的行數
				File file = new File(detailLogProperties.getLogPath() + "detailLog." + readFileName);
				long fileLength = file.length();
				LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
				lineNumberReader.skip(fileLength);
				ConsoleProgressBar.init(lineNumberReader.getLineNumber());
				lineNumberReader.close();
//				逐行取得內容
				FileReader fileReader = new FileReader(detailLogProperties.getLogPath() + "detailLog." + readFileName);
				BufferedReader reader = new BufferedReader(fileReader);
				//讀取檔案內容資料和資料庫處理
				while ((line = reader.readLine()) != null) {
					readSqlCount++;
					try {
						sql = DES._DecryptByDES(line.substring(line.indexOf("ivr_detail_log - ")+17,line.indexOf("#")),detailLogProperties.getKey());
						sql = sql.replace("[ProcessDate]", "[ProcessDate],[HostAddress]")
								 .replace("getdate()", "getdate(),'" + hostAddress + "'");
					}
					catch(StringIndexOutOfBoundsException e) {
						sql = line;
						logger.info(e.toString());
						logger.info("Error sql : " + sql);
					}
					sqlArray.add(sql);
					if(sqlArray.size()>=2000) {
						jdbcTemplate.batchUpdate(sqlArray.toArray(new String[0]));
						successInsertSqlCount += sqlArray.size();
						perLogSqlCount += sqlArray.size();
						ConsoleProgressBar.printConsoleProgressBar(perLogSqlCount);
						sqlArray.clear();
					}
				}
				reader.close();
				if(!sqlArray.isEmpty()) {
					jdbcTemplate.batchUpdate(sqlArray.toArray(new String[0]));
					successInsertSqlCount += sqlArray.size();
					perLogSqlCount += sqlArray.size();
					ConsoleProgressBar.printConsoleProgressBar(perLogSqlCount);
					sqlArray.clear();
				}
				ConsoleProgressBar.ok();
			}
//			if(!sqlArray.isEmpty()) {
//				jdbcTemplate.batchUpdate(sqlArray.toArray(new String[0]));
//				successInsertSqlCount += sqlArray.size();
//				sqlArray.clear();
//			}
			logger.info("DetailLog insert time : " + (System.currentTimeMillis()-time));
			params.clear();
			params.put("Status", "completed");
			params.put("LineNumber", readSqlCount);
			params.put("SuccessCount", successInsertSqlCount);
			params.put("Date", logDate);
			params.put("HostAddress", hostAddress);
			namedParameterJdbcTemplate.update(detailLogProperties.getUpdateStatusSql(), params);
			logger.info("Read " + NumFile + " Folder");
			logger.info("Read " + readSqlCount + " sql columns");
			logger.info(successInsertSqlCount + " sql columns was success to be insert");
			status = true;
		} 
		catch (Exception e) {
			status = false;
			logger.error("---ERROR--- : ",e);
			params.clear();
			params.put("Status", "api error");
			params.put("LineNumber", readSqlCount);
			params.put("SuccessCount", successInsertSqlCount);
			params.put("Date", logDate);
			params.put("HostAddress", hostAddress);
			namedParameterJdbcTemplate.update(detailLogProperties.getUpdateStatusSql(), params);
		}
		return status;
	}
	
	public int checkFile(String data,String folderPath) throws Exception{
		 int Numdata = 0;
	        File folder = new File(folderPath);
	        String[] list = folder.list();           
	          for(int i = 0; i < list.length; i++){
	              if(data.equals(list[i].toString().substring(10,18)))
	            	  Numdata++;
	        }
	        return Numdata;
	}
	
	public Boolean delete(String date) {
		int count = 0;
		Boolean status;
		String hostAddress = StringUtils.EMPTY;
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			hostAddress = iAddress.getHostAddress();
			long time = System.currentTimeMillis();
			Map<String, Object> params = new HashMap<String, Object>();
			params.clear();
			params.put("StartTime", date + "%");
			params.put("HostAddress", hostAddress);
			Map<String, Object> map = namedParameterJdbcTemplate.queryForMap(detailLogProperties.getSelectCountSql(), params);
			ConsoleProgressBar.init((int)map.get("count"));
//			刪除舊的DetailLog
			int i = 0;
			do {
				i = namedParameterJdbcTemplate.update(detailLogProperties.getDeleteDetailLog(), params);
				count += i;
				ConsoleProgressBar.printConsoleProgressBar(count);
			}
			while(i!=0);
			ConsoleProgressBar.ok();
			logger.info("DetailLog delete count : " + count);
			logger.info("DetailLog delete time : " + (System.currentTimeMillis()-time));
			status = true;
		}
		catch(Exception e) {
			logger.error("---ERROR--- : ", e);
			status = false;
		}
		return status;
	}
	
	public Boolean insert() {
		//抓前一天(一天有86400000毫秒)
		long time = System.currentTimeMillis()-86400000;
		Date now = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return execute(sdf.format(now));
	}
	
	public Boolean insert(String date) {
		return execute(date);
	}
}
