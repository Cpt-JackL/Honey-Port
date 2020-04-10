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
import lombok.Getter;

import javax.security.auth.Destroyable;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Socket manager - Manages socket initialization/shutdown(close)
 */
public class SocketManager implements Destroyable {
    private final CachedConfigurationValues configuration;
    private final BanListManager banList;
    private int totalPortsCountWithoutExcludedPorts = 0;
    private Map<Integer, ThreadAndHandler> sockets = new LinkedHashMap<>();

    @AllArgsConstructor
    @Getter
    private static class ThreadAndHandler {
        private final ListenerHandler runnable;
        private final Thread thread;
    }

    public SocketManager(final CachedConfigurationValues configuration, final BanListManager banList) {
        this.configuration = configuration;
        this.banList = banList;
    }

    /**
     * Must initialize ports before they are active
     */
    public void initializePorts() {
        printMessage((byte) 0x00, "Initializing listening ports...");

        // Check port range specification
        if (configuration.getPortRangeStart() == -1 || configuration.getPortRangeEnd() - configuration.getPortRangeStart() + 1 < 1) {
            printMessage((byte) 0x00, "Port range specification is disabled.");
        } else {
            totalPortsCountWithoutExcludedPorts += configuration.getPortRangeEnd() - configuration.getPortRangeStart() + 1;
        }

        // Check specified ports
        if (configuration.getSpecificPorts().length <= 0) {
            printMessage((byte) 0x00, "Specific ports are disabled.");
        } else {
            totalPortsCountWithoutExcludedPorts += configuration.getSpecificPorts().length;
        }

        // Check for excluded ports
        if (configuration.getExcludedPorts().size() <= 0) {
            printMessage((byte) 0x00, "Excluded ports are disabled.");
        }

        // Check if there is some task we need to do
        if (totalPortsCountWithoutExcludedPorts == 0) {
            printMessage((byte) 0x02, "All methods are disabled, nothing to do.");
            return;
        }

        printMessage((byte) 0x10, "Total ports calculated (excluding ignored ports): " + totalPortsCountWithoutExcludedPorts);

        // Create listening ports for range specification
        if (configuration.getPortRangeStart() != -1 && configuration.getPortRangeEnd() - configuration.getPortRangeStart() + 1 > 0) {
            for (int count = configuration.getPortRangeStart(); count <= configuration.getPortRangeEnd(); count++) {
                boolean excluded = false;
                if (configuration.getExcludedPorts().contains(count)) {
                    excluded = true;
                }

                // Added to thread if not excluded
                if (!excluded) {
                    final ListenerHandler handler = new ListenerHandler(configuration, this, banList, count);
                    final Thread thread = new Thread(handler);
                    final ThreadAndHandler threadAndHandler = new ThreadAndHandler(handler, thread);
                    sockets.put(count, threadAndHandler);
                    thread.start();
                }
            }
        }

        // Create listening ports for specific ports
        for (final int port : configuration.getSpecificPorts()) {
            boolean excluded = false;
            if (configuration.getExcludedPorts().contains(port)) {
                printMessage((byte) 0x10, "Excluding port: " + port);
                excluded = true;
            }
            // Added to thread if not excluded
            if (!excluded) {
                final ListenerHandler handler = new ListenerHandler(configuration, this, banList, port);
                final Thread thread = new Thread(handler);
                final ThreadAndHandler threadAndHandler = new ThreadAndHandler(handler, thread);
                sockets.put(port, threadAndHandler);
                thread.start();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            // Ignore
        }

        printMessage((byte) 0x00, "Listener ports successfully initialized. Total " + sockets.size() + " ports.");
    }

    public Integer[] getListOfListeningPorts() {
        return sockets.keySet().toArray(new Integer[0]);
    }

    @Override
    public void destroy() {
        closeAll();
    }

    private void closeAll() {
        for (final Map.Entry<Integer, ThreadAndHandler> entry : sockets.entrySet()) {
            entry.getValue().getRunnable().shutdownListener();
            entry.getValue().getThread().interrupt();
        }
        sockets.clear();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Does not care about exception
        }
        printMessage((byte) 0x00, "Shutdown signal sent to all threads and listener sockets.");
    }

    public void close(final int port) {
        final ThreadAndHandler threadAndHandler = sockets.get(port);
        if (threadAndHandler != null) {
            threadAndHandler.getRunnable().shutdownListener();
            threadAndHandler.getThread().interrupt();
            sockets.remove(port);
            printMessage((byte) 0x00, "Shutdown signal sent to listener socket on port: " + port);
        } else {
            printMessage((byte) 0x01, "Failed to shutdown port: " + port + ". (Listener does not exist)");
        }
    }

    /**
     * Unexpected shutdown of a port, force Exception as an argument to ensure this function being abused.
     */
    void removePortFromSocketList(final int port, final Exception e) {
        sockets.remove(port);
    }
}
