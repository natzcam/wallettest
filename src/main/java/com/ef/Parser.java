package com.ef;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import picocli.CommandLine;

/**
 *
 * @author natc <nathaniel.camomot@legalmatch.com>
 */
public class Parser {

	public static void main(String[] args) {
		CommandLine cmd = new CommandLine(new IPChecker());
		cmd.setCaseInsensitiveEnumValuesAllowed(true);
		cmd.registerConverter(LocalDateTime.class,
				(String value) -> LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss")));
		cmd.execute(args);
		String result = cmd.getExecutionResult();
		System.out.println(result);
	}
}
