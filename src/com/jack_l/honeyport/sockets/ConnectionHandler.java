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

package com.jack_l.honeyport.sockets;

import com.jack_l.honeyport.banlist.BanListManager;
import com.jack_l.honeyport.configuration.CachedConfigurationValues;
import lombok.AllArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;

import static com.jack_l.honeyport.configuration.SharedRuntimeVariables.addDetection;
import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Handles a connection established from a client
 */
@AllArgsConstructor
public class ConnectionHandler implements Runnable {

    private final CachedConfigurationValues configuration;
    private final BanListManager banList;
    private final Socket acceptedConnection;
    private final int delayDisconnectTime;
    private final int welcomeMessageId;

    @Override
    public void run() {
        // Record remote IP
        final InetAddress inetRemoteAddress = ((InetSocketAddress) acceptedConnection.getRemoteSocketAddress()).getAddress();
        final String remoteIp = inetRemoteAddress.getHostAddress();
        int RemotePort = ((InetSocketAddress) acceptedConnection.getRemoteSocketAddress()).getPort();
        final String localIp = acceptedConnection.getInetAddress().getHostAddress();
        int LocalPort = acceptedConnection.getLocalPort();

        try {
            // Add to counter and display message to log
            addDetection();
            printMessage((byte) 0x04, "Connection detected from '" + remoteIp + ":" + RemotePort + "' to '" + localIp + ":" + LocalPort + "'");

            // Respond welcome message if enabled
            if (welcomeMessageId != -1 && welcomeMessageId != configuration.getRandomWelcomeMessageCount()) {
                try {
                    if (configuration.getRandomWelcomeMessageType()[welcomeMessageId].equalsIgnoreCase("Base64")) { // Check message is base64 format
                        printMessage((byte) 0x10, "Sending decoded base64 welcome message to IP: " + remoteIp + "...");
                        acceptedConnection.getOutputStream().write(Base64.getDecoder().decode(configuration.getRandomWelcomeMessage()[welcomeMessageId]));
                    } else {
                        // Using non-ascii encoding might cause some problem here...
                        // Depends on file encoding, Java charset etc. etc. Use base64 encoding if needed
                        printMessage((byte) 0x10, "Sending " + configuration.getRandomWelcomeMessageType()[welcomeMessageId] + " welcome message to IP: " + remoteIp + "...");
                        acceptedConnection.getOutputStream().write(configuration.getRandomWelcomeMessage()[welcomeMessageId].getBytes(configuration.getRandomWelcomeMessageType()[welcomeMessageId]));
                    }
                } catch (UnsupportedEncodingException e) {
                    printMessage((byte) 0x01, "Failed to send welcome message to client. Unknown encoding name: " + configuration.getRandomWelcomeMessageType()[welcomeMessageId] + ".");
                } catch (Exception e) {
                    printMessage((byte) 0x01, "Failed to send welcome message to IP: " + remoteIp + ". (Exception: " + e + ")");
                }
            }

            // Add IP to firewall (execute cmd)
            banList.addBan(inetRemoteAddress);

            // Disconnecting the client
            if (delayDisconnectTime > 1) {
                try {
                    printMessage((byte) 0x20, "Wait for " + delayDisconnectTime + " seconds before closing connection " + remoteIp + " (Firewall might still cuts off the connection)...");
                    Thread.sleep(delayDisconnectTime * 1000);
                } catch (Exception e) {
                    printMessage((byte) 0x01, "Delay timer to close connection failed on remote IP: " + remoteIp + ". (Exception " + e + ")");
                }
            }
        } finally {
            try {
                acceptedConnection.close();
            } catch (Exception e) {
                printMessage((byte) 0x01, "Failed to close remote connection for IP: " + remoteIp + ". (Exception: " + e + ")");
            }
        }
    }
}
