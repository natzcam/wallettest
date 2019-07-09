package com.ef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static picocli.CommandLine.*;
import picocli.CommandLine.Command;

/**
 *
 * @author natc <nathaniel.camomot@legalmatch.com>
 */
@Command(mixinStandardHelpOptions = true,
		description = "Checks if an IP made requests over the threshold!",
		version = "1.0-SNAPSHOT"
)
public class IPChecker implements Callable<String> {

	@Option(names = {"--delimiter"}, description = "the delimiter", defaultValue = "\\|",
			showDefaultValue = Help.Visibility.ALWAYS)
	private String delimiter;

	@Option(names = {"--accesslog"}, description = "the access log", defaultValue = "access.log",
			showDefaultValue = Help.Visibility.ALWAYS)
	private File accessLog;

	@Option(names = {"--startDate"}, description = "the start date-time", required = true)
	private LocalDateTime startDate;

	@Option(names = {"--duration"}, description = "hourly, daily", required = true)
	private Durations duration;

	@Option(names = {"--threshold"}, description = "the threshold", required = true)
	private Long threshold;

	@Override
	public String call() throws Exception {
		if (!accessLog.exists()) {
			throw new FileNotFoundException("access log not found");
		}

		processLog((String[] test) -> {
			System.out.println(Arrays.toString(test));
		});

		return accessLog + " " + startDate + " " + duration + " " + threshold;
	}

	private void processLog(Consumer<String[]> lineHandler) {

		if (lineHandler == null) {
			throw new IllegalArgumentException("Handler must not be null");
		}

		String line;
		try (BufferedReader br = new BufferedReader(new FileReader(accessLog))) {
			while ((line = br.readLine()) != null) {
				String[] country = line.split(delimiter);

				lineHandler.accept(country);

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
