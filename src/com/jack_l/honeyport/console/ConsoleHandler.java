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
import com.jack_l.honeyport.configuration.SharedRuntimeVariables;

import java.util.Date;

import static com.jack_l.honeyport.console.LoggingHandler.writeLog;

/**
 * Handles output to console
 */
public class ConsoleHandler {
    private static final String C_RESET = "\u001B[0m";
    private static final String C_BLACK = "\u001B[30m";
    private static final String C_RED = "\u001B[31m";
    private static final String C_GREEN = "\u001B[32m";
    private static final String C_YELLOW = "\u001B[33m";
    private static final String C_BLUE = "\u001B[34m";
    private static final String C_PURPLE = "\u001B[35m";
    private static final String C_CYAN = "\u001B[36m";
    private static final String C_WHITE = "\u001B[37m";

    /**
     * Print message by using system current setting
     */
    public static void printMessage(final byte messageType, final String message) {
        final CachedConfigurationValues.ConsoleConfigurationValues configurations = SharedRuntimeVariables.getCurrentCachedConfiguration().getConsoleConfigurations();
        printMessage(messageType, message, configurations);
    }

    /**
     * Function: printMessage - Display message to user console
     *
     * 0000_0000 (0x0) - Normal Message
     * 0000_0001 (0x1) - Warning Message
     * 0000_0010 (0x2) - Error Message
     * 0000_0100 (0x4) - Detection Message
     * 0001_0000 (0x10) - 0011_0000 (0x30) - Debug Message, corresponding to level
     */
    public static void printMessage(final byte messageType, final String message, final CachedConfigurationValues.ConsoleConfigurationValues configurations) {
        // Get and format current date & time
        String formattedMessage;
        final byte debugLevel = configurations.getDebugLevel();
        final byte logLevel = configurations.getLogLevel();
        final boolean useColorCode = configurations.isUseColorCode();

        if ((messageType >> 4) <= debugLevel && (messageType >> 4) != 0x0) {
            // Debug Message
            formattedMessage = "[" + SharedRuntimeVariables.dateTimeFormat.format(new Date()) + " DEBUG" + (messageType >> 4) + "    ]: " + message;

            // Check if logging is required
            if ((logLevel & 0x8) == 0x8) {
                writeLog(formattedMessage, configurations);
            }

            // Add color
            if (useColorCode) {
                formattedMessage = C_PURPLE + formattedMessage + C_RESET;
            }

            // Print
            System.out.println(formattedMessage);
        } else if (messageType == 0x0) {
            // Normal Message
            formattedMessage = "[" + SharedRuntimeVariables.dateTimeFormat.format(new Date()) + " INFO      ]: " + message;

            // Check if logging is required
            if ((logLevel & 0x1) == 0x1) {
                writeLog(formattedMessage, configurations);
            }

            // Print
            System.out.println(formattedMessage);
        } else if (messageType == 0x1) {
            // Warning Message
            formattedMessage = "[" + SharedRuntimeVariables.dateTimeFormat.format(new Date()) + " WARNING   ]: " + message;

            // Check if logging is required
            if ((logLevel & 0x2) == 0x2) {
                writeLog(formattedMessage, configurations);
            }

            // Add color
            if (useColorCode) {
                formattedMessage = C_YELLOW + formattedMessage + C_RESET;
            }

            // Print
            System.out.println(formattedMessage);
        } else if (messageType == 0x2) {
            // Error Message
            formattedMessage = "[" + SharedRuntimeVariables.dateTimeFormat.format(new Date()) + " ERROR     ]: " + message;

            // Check if logging is required
            if ((logLevel & 0x4) == 0x4) {
                writeLog(formattedMessage, configurations);
            }

            // Add color
            if (useColorCode) {
                formattedMessage = C_RED + formattedMessage + C_RESET;
            }

            // Print
            System.out.println(formattedMessage);
        } else if (messageType == 0x4) {
            // Detection Message
            formattedMessage = "[" + SharedRuntimeVariables.dateTimeFormat.format(new Date()) + " DETECTION ]: " + message;

            // Check if logging is required
            if ((logLevel & 0x10) == 0x10) {
                writeLog(formattedMessage, configurations);
            }

            // Add color
            if (useColorCode) {
                formattedMessage = C_CYAN + formattedMessage + C_RESET;
            }

            // Print
            System.out.println(formattedMessage);
        } else if (messageType == 0x5) {
            // Banned message
            formattedMessage = "[" + SharedRuntimeVariables.dateTimeFormat.format(new Date()) + " BAN/UNBAN ]: " + message;

            // Check if logging is required
            if ((logLevel & 0x20) == 0x20) {
                writeLog(formattedMessage, configurations);
            }

            // Add color
            if (useColorCode) {
                formattedMessage = C_PURPLE + formattedMessage + C_RESET;
            }

            // Print
            System.out.println(formattedMessage);
        }
    }
}
