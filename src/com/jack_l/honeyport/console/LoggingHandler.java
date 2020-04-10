/*
 * Copyright (C) 2020 Jack L (http://jack-l.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jack_l.honeyport.console;

import com.jack_l.honeyport.configuration.CachedConfigurationValues;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Log file handler
 */
public class LoggingHandler {

    /**
     * Writes log to file, does not check log setting
     */
    public static void writeLog(String formattedMessage, final CachedConfigurationValues.ConsoleConfigurationValues consoleConfiguration) {
        final SimpleDateFormat formatCurrentDate = new SimpleDateFormat("yyyy_MM_dd");
        try (final PrintWriter logFileWriter = new PrintWriter(new FileOutputStream(new File("./Logs/Log_" + formatCurrentDate.format(new Date()) + ".log"), true), true)) {
            logFileWriter.println(formattedMessage);
        } catch (final FileNotFoundException e) {
            final CachedConfigurationValues.ConsoleConfigurationValues printErrorMessageConfiguration = CachedConfigurationValues.ConsoleConfigurationValues.builder().debugLevel(consoleConfiguration.getDebugLevel()).logLevel((byte) 0x0).useColorCode(consoleConfiguration.isUseColorCode()).build();
            printMessage((byte) 0x01, "Failed to write log message. File does not exist.", printErrorMessageConfiguration);
        }
    }
}
