/*
 * Copyright (C) 2014-2020 Jack L (http://jack-l.com)
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

import com.jack_l.honeyport.banlist.BanListManager;
import com.jack_l.honeyport.configuration.CachedConfigurationValues;
import com.jack_l.honeyport.configuration.ConfigurationFileReader;
import com.jack_l.honeyport.configuration.ConfigurationExceptions;
import com.jack_l.honeyport.configuration.SharedRuntimeVariables;
import com.jack_l.honeyport.console.KeyboardInputHandler;
import com.jack_l.honeyport.sockets.SocketManager;

import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Main class of Honey Port project. Handles program startup/shutdown/reload
 */
public class HoneyPort {
    private static BanListManager banList;
    private static SocketManager socketManager;
    private static boolean shutdownHookInitialized = false;

    /**
     * Main entry point to the application
     */
    public static void main(String[] args) {
        boolean reload;
        do {
            // Load, validate and apply configuration
            boolean configurationApplied;
            try {
                configurationApplied = ConfigurationFileReader.validSettingsAndSetActive(ConfigurationFileReader.loadConfigurationsToCache());
            } catch (final ConfigurationExceptions e) {
                configurationApplied = false;
            }

            if (!configurationApplied) {
                if (SharedRuntimeVariables.getCurrentCachedConfiguration() != null) {
                    printMessage((byte) 0x01, "Failed to reload configuration. Using previous configuration to run the application.");
                } else {
                    printMessage((byte) 0x02, "Failed to load configuration. Application will now exit.", CachedConfigurationValues.ConsoleConfigurationValues.builder().build());
                    System.exit(-1);
                }
            }
            final CachedConfigurationValues configuration = SharedRuntimeVariables.getCurrentCachedConfiguration();

            // Welcome message
            printMessage((byte) 0x00, "-----------------------------------------");
            printMessage((byte) 0x00, "Welcome use Honey Port.");
            printMessage((byte) 0x00, "Original Author: Jack L (http://jack-l.com)");
            printMessage((byte) 0x00, "Version: " + SharedRuntimeVariables.version);
            printMessage((byte) 0x01, "This program is released under GNU General Public License v3");
            printMessage((byte) 0x01, "This program is still under Beta testing stage.");
            printMessage((byte) 0x00, "-----------------------------------------");

            // Initialize objects
            banList = new BanListManager(configuration);
            socketManager = new SocketManager(configuration, banList);
            socketManager.initializePorts();
            if (!shutdownHookInitialized) {
                shutdownHookInitialized = true;
                initializeShutdownHook();
            }

            // Transfer control to keyboard
            final KeyboardInputHandler keyboard = new KeyboardInputHandler(configuration, banList, socketManager);
            reload = keyboard.keyboardControl();

            // Transfer out from keyboard
            if (reload) {
                printMessage((byte) 0x00, "Reload signal received, unload current settings...");
                socketManager.destroy();
                socketManager = null;
                banList.destroy();
                banList = null;
                printMessage((byte) 0x00, "All current setting unloaded, restarting...");
                if (configuration.getConsoleConfigurations().isUseColorCode()) {
                    System.out.print("\033[H\033[2J"); // Clear console
                }
            }
        } while (reload);
        System.exit(0);
    }

    private static void initializeShutdownHook() {
        printMessage((byte) 0x10, "Initializing shutdown hook...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            printMessage((byte) 0x00, "Shutdown signal detected. Destroying all resources...");
            if (socketManager != null) {
                socketManager.destroy();
                socketManager = null;
            }
            if (banList != null) {
                banList.destroy();
                banList = null;
            }
            printMessage((byte) 0x00, "Total of " + SharedRuntimeVariables.getDetectionCount() + " connections established to Honey Port during runtime.");
        }));
        printMessage((byte) 0x10, "Shutdown hook initialized.");
    }
}
