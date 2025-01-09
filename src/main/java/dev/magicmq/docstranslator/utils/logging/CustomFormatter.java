package dev.magicmq.docstranslator.utils.logging;


import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        // Format timestamp
        String time = String.format(
                "[%1$tH:%1$tM:%1$tS]", // Format: HH:mm:ss
                record.getMillis()
        );

        // Format log level
        String level = "[" + record.getLevel().getName() + "]";

        // Format logger name
        String loggerName = "[" + (record.getLoggerName() != null ? record.getLoggerName() : "Unknown") + "]";

        // Format message
        String message = formatMessage(record);

        // Combine into the desired log format
        return String.format("%s %s %s: %s%n", time, loggerName, level, message);
    }
}
