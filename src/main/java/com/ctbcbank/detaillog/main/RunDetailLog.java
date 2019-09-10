package com.ctbcbank.detaillog.main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RunDetailLog implements CommandLineRunner{
	@Autowired
	private DetailLog detailLog;
    
	public void run(String... args) throws ParseException {
		String dateFromat = "((^((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/\\._])(10|12|0?[13578])([-\\/\\._])"
				+ "(3[01]|[12][0-9]|0?[1-9])$)|(^((1[8-9]\\d{2})|([2-9]\\d{3}))([-\\/\\._])"
				+ "(11|0?[469])([-\\/\\._])(30|[12][0-9]|0?[1-9])$)|(^((1[8-9]\\d{2})|([2-9]\\d{3}))"
				+ "([-\\/\\._])(0?2)([-\\/\\._])(2[0-8]|1[0-9]|0?[1-9])$)|(^([2468][048]00)([-\\/\\._])"
				+ "(0?2)([-\\/\\._])(29)$)|(^([3579][26]00)([-\\/\\._])(0?2)([-\\/\\._])(29)$)|(^([1][89][0][48])"
				+ "([-\\/\\._])(0?2)([-\\/\\._])(29)$)|(^([2-9][0-9][0][48])([-\\/\\._])(0?2)([-\\/\\._])(29)$)"
				+ "|(^([1][89][2468][048])([-\\/\\._])(0?2)([-\\/\\._])(29)$)|(^([2-9][0-9][2468][048])([-\\/\\._])"
				+ "(0?2)([-\\/\\._])(29)$)|(^([1][89][13579][26])([-\\/\\._])(0?2)([-\\/\\._])(29)$)|"
				+ "(^([2-9][0-9][13579][26])([-\\/\\._])(0?2)([-\\/\\._])(29)$))";
		String date = StringUtils.EMPTY;
		
		Options options = new Options( );
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("E", "execute", false, "Using the assignation date execute the detailLog" );
        options.addOption("D", "delete", false, "Using the assignation date delete the detailLog");
        options.addOption("d", "date", true, "detailLog start time");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse( options, args );
        
		if(args.length!=0) {
			if(commandLine.hasOption("h")) {
				System.out.println("說明:" + options.getOption("h").getDescription());
				System.out.println("選項:");
				System.out.println("  -d date 指定detailLog的日期(須符合YYYY/MM/DD的格式)");
				System.out.println("  -D 從DataBase刪除detailLog");
				System.out.println("  -E 新增detailLog資料進DataBase");
				System.out.println("範例:");
				System.out.println("  e.g. xxx.jar 無參數，依照預設日期(執行當日的前一天)執行");
				System.out.println("  e.g. xxx.jar -E -d date 依照指定日期(date)執行");
				System.out.println("  e.g. xxx.jar -D -d date 依照指定日期(date)刪除");
				System.out.println("  e.g. xxx.jar -D -E -d date 依照指定日期(date)刪除且執行");
			}
			if(commandLine.hasOption("d")) {
				date = commandLine.getOptionValue("d");
				if(date.matches(dateFromat)) {
					date = date.replaceAll("[-\\/\\._]", "-");
				}
				else {
					System.out.println("Date format is invaild!");
					System.out.println("Input -h to get help");
					System.exit(0);
				}
			}
			if(commandLine.hasOption("D")) {
				if(!date.equals(StringUtils.EMPTY)) {
					detailLog.delete(date);
				}
				else {
					System.out.println("Date is empty!");
					System.out.println("Input -h to get help");
					System.exit(0);
				}
			}
			if(commandLine.hasOption("E")) {
				if(!date.equals(StringUtils.EMPTY)) {
					detailLog.insert(date);
				}
				else {
					System.out.println("Date is empty!");
					System.out.println("Input -h to get help");
					System.exit(0);
				}
			}
		}
		else {
			detailLog.insert();
		}
	}
}
