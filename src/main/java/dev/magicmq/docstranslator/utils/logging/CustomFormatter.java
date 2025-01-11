/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.docstranslator.utils.logging;


import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        // Format timestamp
        String time = String.format(
                "[%1$tF %1$tT]",
                record.getMillis()
        );
        String level = "[" + record.getLevel().getName() + "]";
        String loggerName = "[" + (record.getLoggerName() != null ? record.getLoggerName() : "Unknown") + "]";
        String message = formatMessage(record);

        return String.format("%s %s %s: %s%n", time, loggerName, level, message);
    }
}
