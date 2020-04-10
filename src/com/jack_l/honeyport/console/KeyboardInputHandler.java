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

import com.jack_l.honeyport.banlist.BanListManager;
import com.jack_l.honeyport.configuration.CachedConfigurationValues;
import com.jack_l.honeyport.configuration.ConfigurationFileReader;
import com.jack_l.honeyport.configuration.SharedRuntimeVariables;
import com.jack_l.honeyport.sockets.SocketManager;
import lombok.AllArgsConstructor;

import java.net.InetAddress;
import java.util.Scanner;

import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Handles input from console
 */
@AllArgsConstructor
public class KeyboardInputHandler {
    private final CachedConfigurationValues configuration;
    private final BanListManager banList;
    private final SocketManager socketManager;

    /**
     * Returns false for exit and true for reload, otherwise this thread will stay in this infinite loop
     */
    public boolean keyboardControl() {
        final Scanner keyboardScanner = new Scanner(System.in);
        String keyboardInputString;
        printMessage((byte) 0x00, "Enter '!h' to see a list of commands.");
        do {
            keyboardInputString = keyboardScanner.nextLine();
            printMessage((byte) 0x10, "Key '" + keyboardInputString + "' detected.");

            if (keyboardInputString.length() >= 2) {
                if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!h")) {
                    // !h command
                    printHelp();
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!c")) {
                    // !c command
                    printCurrentConfiguration();
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!p")) {
                    // !p command
                    listListeningPorts();
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!s")) {
                    // !s command
                    try {
                        shutdownSinglePort(Integer.parseInt(keyboardInputString.substring(3)));
                    } catch (Exception e) {
                        printMessage((byte) 0x01, "Incorrect command format. Example: !s 36478");
                    }
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!w")) {
                    // !w command
                    listWhitelistedIps();
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!b")) {
                    // !b command
                    listBannedIpAddresses();
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!u")) {
                    // !u command
                    try {
                        unbanIpAddress(keyboardInputString.substring(3));
                    } catch (Exception e) {
                        printMessage((byte) 0x01, "Incorrect command format. Example: !u 192.168.126.75");
                    }
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!r")) {
                    // !r command
                    return true;
                } else if (keyboardInputString.substring(0, 2).equalsIgnoreCase("!q")) {
                    // !q command
                    return false;
                } else {
                    printMessage((byte) 0x01, "Unknown command.");
                }
            } else {
                printMessage((byte) 0x01, "Unknown command.");
            }
        } while (true);
    }

    private void printHelp() {
        final StringBuilder helpMessage = new StringBuilder("List of commands: ");
        helpMessage.append("\r\n\t!h - Display this help.");
        helpMessage.append("\r\n\t!c - Display current loaded configuration.");
        helpMessage.append("\r\n\t!p - List of ports that are being listened.");
        helpMessage.append("\r\n\t!s - Shutdown an open port. For example:\r\n\t\t!s 36478");
        helpMessage.append("\r\n\t!w - List of whitelisted IP addresses.");
        helpMessage.append("\r\n\t!b - List of banned IP addresses.");
        helpMessage.append("\r\n\t!u - Unban a banned IP address. For example:\r\n\t\t!u 192.168.126.75");
        helpMessage.append("\r\n\t!r - Reload the application. This will reset everything.");
        helpMessage.append("\r\n\t!s - Shutdown the application.");
        printMessage((byte) 0x00, helpMessage.toString());
    }

    private void printCurrentConfiguration() {
        printMessage((byte) 0x00, "Current Configuration: \r\n\t" + SharedRuntimeVariables.getCurrentCachedConfiguration().toString());
    }

    private void listListeningPorts() {
        final StringBuilder listPortsMessage = new StringBuilder("Ports that are being listened: ");
        final Integer[] ports = socketManager.getListOfListeningPorts();
        int index = 0;
        for (final Integer port : ports) {
            if (index % 10 == 0) {
                listPortsMessage.append("\r\n\t");
            }
            listPortsMessage.append(port);
            if (index < ports.length - 1) {
                listPortsMessage.append(",");
            }
            index++;
        }
        listPortsMessage.append("\r\n\t").append("Total listening ports: " + ports.length);
        printMessage((byte) 0x00, listPortsMessage.toString());
    }

    private void shutdownSinglePort(final int portNum) {
        if (!ConfigurationFileReader.validatePortNum(portNum)) {
            printMessage((byte) 0x02, "Incorrect port number. Valid range is 1-65535.");
            return;
        }
        socketManager.close(portNum);
    }

    private void listWhitelistedIps() {
        final StringBuilder whitelistedIPsMessage = new StringBuilder("Currently whitelisted IPs: ");
        final InetAddress[] ips = SharedRuntimeVariables.getCurrentCachedConfiguration().getIpWhiteList().toArray(new InetAddress[0]);
        if (ips.length > 0) {
            int index = 1;
            for (final InetAddress ip : ips) {
                whitelistedIPsMessage.append("\r\n\t").append(index++).append(". ").append(ip.getHostAddress());
            }
        } else {
            whitelistedIPsMessage.append("None.");
        }
        whitelistedIPsMessage.append("\r\n\t").append("Total whitelisted IPs: " + ips.length);
        printMessage((byte) 0x00, whitelistedIPsMessage.toString());
    }

    private void listBannedIpAddresses() {
        final StringBuilder bannedIPsMessage = new StringBuilder("Currently banned IPs: ");
        final String[] listOfBannedIps = banList.getIpList();
        if (listOfBannedIps.length > 0) {
            int index = 1;
            for (final String ip : listOfBannedIps) {
                bannedIPsMessage.append("\r\n\t").append(index++).append(". ").append(ip);
            }
        } else {
            bannedIPsMessage.append("None.");
        }
        bannedIPsMessage.append("\r\n\t").append("Total currently banned IPs: " + listOfBannedIps.length);
        bannedIPsMessage.append("\r\n\t").append("Total connections made to honey port: " + SharedRuntimeVariables.getDetectionCount());
        printMessage((byte) 0x00, bannedIPsMessage.toString());
    }

    private void unbanIpAddress(final String ipAddress) {
        banList.removeBan(ipAddress);
    }
}
