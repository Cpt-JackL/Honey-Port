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

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Handles port listening
 */
public class ListenerHandler implements Runnable {
    private final CachedConfigurationValues configuration;
    private final SocketManager socketManager;
    private final BanListManager banList;
    private int port;
    private int delayDisconnectTime = -1;
    private int rndWelcomeMsgID = -1;
    private ServerSocket socket;
    private boolean shutdown = false;

    public ListenerHandler(final CachedConfigurationValues configuration, final SocketManager socketManager, final BanListManager banList, final int port) {
        this.configuration = configuration;
        this.socketManager = socketManager;
        this.banList = banList;
        printMessage((byte) 0x20, "Initializing port " + port + "...");
        this.port = port;

        //Random Number Generator
        final Random randomNumberGenerator = new Random();

        // Generate random delay time
        if (configuration.getFakeServerRandomDelayDisconnectingTime() > 0) {
            delayDisconnectTime = randomNumberGenerator.nextInt(configuration.getFakeServerRandomDelayDisconnectingTime() + 1);
        }

        //Generate random message for this port
        if (configuration.getRandomWelcomeMessageCount() > 0) {
            rndWelcomeMsgID = randomNumberGenerator.nextInt(configuration.getRandomWelcomeMessageCount() + 1);
        }
    }

    //Create port listener
    @Override
    public void run() {
        // Start listening
        try (final ServerSocket listenerSocket = new ServerSocket(port)) {
            socket = listenerSocket;
            printMessage((byte) 0x20, "Listening on port " + port + ". Param: WelcomeMsgCount=" + configuration.getRandomWelcomeMessageCount() + ", WelcomeMsgID=" + rndWelcomeMsgID + ", DelayDisconnectTimer=" + delayDisconnectTime + ".");
            while (!shutdown) {
                final Socket acceptedConnection = listenerSocket.accept();
                new Thread(new ConnectionHandler(configuration, banList, acceptedConnection, delayDisconnectTime, rndWelcomeMsgID)).start();
            }
        } catch (final BindException e) {
            printMessage((byte) 0x01, "Failed to bind on port " + port + ". Thread shutting down.");
            socketManager.removePortFromSocketList(port, e);
        } catch (final Exception e) {
            if (!shutdown) {
                printMessage((byte) 0x01, "Port:" + port + " is shutting down due to error. (Exception:" + e + ")");
                socketManager.removePortFromSocketList(port, e);
            } else {
                printMessage((byte) 0x20, "Shutdown command detected. Closing port: " + port);
            }
        }
    }

    public void shutdownListener() {
        if (socket != null) {
            try {
                shutdown = true;
                socket.close();
                socket = null;
            } catch (IOException e) {
                printMessage((byte) 0x01, "Failed to shutdown port: " + port + ". (Exception:" + e + ")");
            }
        } else {
            printMessage((byte) 0x01, "Failed to shutdown port: " + port + ". (Socket is not initialized)");
        }
    }
}