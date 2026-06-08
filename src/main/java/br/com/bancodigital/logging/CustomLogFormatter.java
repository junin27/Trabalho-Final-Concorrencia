package br.com.bancodigital.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * CustomLogFormatter formats log records.
 * Intent: Display structured log lines containing timestamp, level, thread name and message.
 */
public class CustomLogFormatter extends Formatter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public synchronized String format(LogRecord record) {
        String threadName = Thread.currentThread().getName();
        String time = dateFormat.format(new Date(record.getMillis()));
        return String.format("[%s] [%s] [Thread: %s] %s%n",
                time,
                record.getLevel(),
                threadName,
                record.getMessage());
    }
}
