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

package com.jack_l.honeyport.configuration;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Handles reading configuration file and validation
 */
public class ConfigurationFileReader {

    /**
     * Reads the file and cache into configuration object
     */
    public static CachedConfigurationValues loadConfigurationsToCache() throws ConfigurationExceptions {
        final String readFileErrorMessage = "Failed to read configuration file. (%s)";
        final CachedConfigurationValues.CachedConfigurationValuesBuilder configurationCacheBuilder = CachedConfigurationValues.builder();
        final CachedConfigurationValues.ConsoleConfigurationValues.ConsoleConfigurationValuesBuilder consoleConfigurationCacheBuilder = CachedConfigurationValues.ConsoleConfigurationValues.builder();
        final String configurationFileName = "Settings.conf"; // Setting file location and name
        printMessage((byte) 0x00, "Reading configuration from " + configurationFileName + "...", consoleConfigurationCacheBuilder.build());
        final Properties configurationFilePropertyReader = new Properties();

        // Choose console configuration data, if an old one exist, use the old one. Otherwise use default
        final CachedConfigurationValues.ConsoleConfigurationValues defaultConsoleConfiguration = getConsoleConfigurationToUseWhileLoadingConfigurationFile(null);

        // Start reading file
        try (final InputStream fileReader = new FileInputStream(configurationFileName)) {
            configurationFilePropertyReader.load(fileReader);
            // Start reading data from file
            // Log level (must be validate and create file here)
            final byte tempLogLevel = Byte.parseByte(configurationFilePropertyReader.getProperty("Program.LogLevel"), 16);
            // Validate
            if (tempLogLevel < 0x0 || tempLogLevel >= 0x40) {
                final String errorMessage = String.format(readFileErrorMessage, "Invalid 'logLevel' input. Valid range is 0x0 - 0x3F.");
                printMessage((byte) 0x02, errorMessage, defaultConsoleConfiguration);
                throw new ConfigurationExceptions(errorMessage);
            }
            consoleConfigurationCacheBuilder.logLevel(tempLogLevel);

            // Color code
            consoleConfigurationCacheBuilder.useColorCode(Boolean.parseBoolean(configurationFilePropertyReader.getProperty("Program.UseColorCode")));

            // Debug Level
            final byte debugLevel = (Byte.parseByte(configurationFilePropertyReader.getProperty("Program.Debug"), 16));
            consoleConfigurationCacheBuilder.debugLevel(debugLevel);

            // Apply console setting to configuration object
            final CachedConfigurationValues.ConsoleConfigurationValues consoleConfigurationCache = consoleConfigurationCacheBuilder.build();
            configurationCacheBuilder.consoleConfigurations(consoleConfigurationCache);
            final CachedConfigurationValues.ConsoleConfigurationValues consoleConfigurationToUseWhileLoading = getConsoleConfigurationToUseWhileLoadingConfigurationFile(configurationCacheBuilder.build());
            printMessage((byte) 0x10, "Log Level is set to: " + consoleConfigurationToUseWhileLoading.getLogLevel(), consoleConfigurationToUseWhileLoading);
            printMessage((byte) 0x10, "Use Color Code is set to: " + consoleConfigurationToUseWhileLoading.isUseColorCode(), consoleConfigurationToUseWhileLoading);
            printMessage((byte) 0x10, "Debug Level is set to: " + consoleConfigurationToUseWhileLoading.getDebugLevel(), consoleConfigurationToUseWhileLoading);
            if (SharedRuntimeVariables.getCurrentCachedConfiguration() == null) {
                printMessage((byte) 0x10, "Console settings successfully applied.", consoleConfigurationToUseWhileLoading);
            }

            // Other settings
            final String banCmd = configurationFilePropertyReader.getProperty("General.BanCommand");
            configurationCacheBuilder.banCmd(banCmd);
            final String unbanCmd = configurationFilePropertyReader.getProperty("General.UnbanCommand");
            configurationCacheBuilder.unbanCmd(unbanCmd);
            configurationCacheBuilder.banLength(Long.parseLong(configurationFilePropertyReader.getProperty("General.BanLength")));
            configurationCacheBuilder.portRangeStart(Integer.parseInt(configurationFilePropertyReader.getProperty("PortRange.Start")));
            configurationCacheBuilder.portRangeEnd(Integer.parseInt(configurationFilePropertyReader.getProperty("PortRange.End")));

            // Random fake srv welcome message settings
            if (configurationFilePropertyReader.getProperty("FakeServer.Enabled").equalsIgnoreCase("on")) {
                configurationCacheBuilder.fakeServerRandomDelayDisconnectingTime(Integer.parseInt(configurationFilePropertyReader.getProperty("FakeServer.RandomDelayDisconnectingTime")));
                final int randomWelcomeMessageCount = Integer.parseInt(configurationFilePropertyReader.getProperty("FakeServer.RandomWelcomeMessageCount"));
                configurationCacheBuilder.randomWelcomeMessageCount(randomWelcomeMessageCount);

                // Read random welcome messages
                if (randomWelcomeMessageCount > 0) {
                    final String[] randomWelcomeMessageType = new String[randomWelcomeMessageCount];
                    final String[] randomWelcomeMessage = new String[randomWelcomeMessageCount];
                    //Reading contents
                    for (int count = 0; count < randomWelcomeMessageCount; count++) {
                        randomWelcomeMessageType[count] = configurationFilePropertyReader.getProperty("FakeServer.RandomWelcomeMessage." + (count + 1) + ".Type");
                        randomWelcomeMessage[count] = configurationFilePropertyReader.getProperty("FakeServer.RandomWelcomeMessage." + (count + 1) + ".Content");
                    }
                    configurationCacheBuilder.randomWelcomeMessageType(randomWelcomeMessageType);
                    configurationCacheBuilder.randomWelcomeMessage(randomWelcomeMessage);
                }
            }

            // Specific ports
            final String specificPortsString = configurationFilePropertyReader.getProperty("SpecificPorts");
            if (specificPortsString != null && !specificPortsString.isEmpty()) {
                final String[] specificPortsArray = specificPortsString.replaceAll(" ", "").split(",");
                final Integer[] specificPorts = new Integer[specificPortsArray.length];
                int arrayIndex = 0;
                try {
                    for (final String port : specificPortsArray) {
                        specificPorts[arrayIndex++] = Integer.parseInt(port);
                    }
                } catch (final NumberFormatException e) {
                    final String errorMessage = String.format(readFileErrorMessage, "'SpecificPorts' contains non integer value on index " + arrayIndex + ".");
                    printMessage((byte) 0x02, errorMessage, defaultConsoleConfiguration);
                    throw new ConfigurationExceptions(errorMessage, e);
                }
                configurationCacheBuilder.specificPorts(specificPorts);
            }

            // Excluded ports
            final String excludedPortsString = configurationFilePropertyReader.getProperty("ExcludePorts");
            if (excludedPortsString != null && !excludedPortsString.isEmpty()) {
                final String[] excludedPortsArray = excludedPortsString.replaceAll(" ", "").split(",");
                final Set<Integer> excludedPorts = new HashSet<>();
                try {
                    for (final String port : excludedPortsArray) {
                        excludedPorts.add(Integer.parseInt(port));
                    }
                } catch (final NumberFormatException e) {
                    final String errorMessage = String.format(readFileErrorMessage, "'ExcludedPorts' contains non integer value(s).");
                    printMessage((byte) 0x02, errorMessage, defaultConsoleConfiguration);
                    throw new ConfigurationExceptions(errorMessage, e);
                }
                configurationCacheBuilder.excludedPorts(excludedPorts);
            }

            // Whitelisted IPs
            final String whiteListedIPsString = configurationFilePropertyReader.getProperty("WhiteListedIPs");
            if (whiteListedIPsString != null && !whiteListedIPsString.isEmpty()) {
                final String[] whiteListedIPsArray = whiteListedIPsString.replaceAll(" ", "").split(",");
                final Set<InetAddress> whiteListedIps = new HashSet<>();
                try {
                    for (final String ip : whiteListedIPsArray) {
                        whiteListedIps.add(InetAddress.getByName(ip));
                    }
                } catch (final UnknownHostException e) {
                    final String errorMessage = String.format(readFileErrorMessage, "'WhiteListedIPs' contains invalid IP address(es).");
                    printMessage((byte) 0x02, errorMessage, defaultConsoleConfiguration);
                    throw new ConfigurationExceptions(errorMessage, e);
                }
                configurationCacheBuilder.ipWhiteList(whiteListedIps);
            }

            printMessage((byte) 0x00, "Configuration file loaded to memory.", consoleConfigurationToUseWhileLoading);
            return configurationCacheBuilder.build();
        } catch (final FileNotFoundException e) {
            printMessage((byte) 0x02, String.format(readFileErrorMessage, "Configuration file '" + configurationFileName + "' not found!"), defaultConsoleConfiguration);
            throw new ConfigurationExceptions(e);
        } catch (final NullPointerException | IllegalArgumentException | IOException e) {
            printMessage((byte) 0x02, String.format(readFileErrorMessage, "Error while reading configuration file: " + configurationFileName + ". (Exception: " + e + ")"), defaultConsoleConfiguration);
            throw new ConfigurationExceptions(e);
        }
    }

    /**
     * Validates configuration object and set active if passed
     */
    public static boolean validSettingsAndSetActive(final CachedConfigurationValues settings) {
        final CachedConfigurationValues.ConsoleConfigurationValues consoleConfiguration = getConsoleConfigurationToUseWhileLoadingConfigurationFile(settings);
        final boolean validationResult = validateSettings(settings);
        if (validationResult) {
            SharedRuntimeVariables.setCurrentCachedConfiguration(settings);
            printMessage((byte) 0x00, "New configuration applied.", settings.getConsoleConfigurations());
        } else {
            printMessage((byte) 0x02, "Failed to apply new settings.", consoleConfiguration);
        }
        return validationResult;
    }

    /**
     * Validates if everything inside a configuration object is valid
     */
    private static boolean validateSettings(final CachedConfigurationValues configuration) {
        final CachedConfigurationValues.ConsoleConfigurationValues consoleConfiguration = getConsoleConfigurationToUseWhileLoadingConfigurationFile(configuration);
        printMessage((byte) 0x00, "Validating configurations...", consoleConfiguration);
        if (configuration.getConsoleConfigurations().getDebugLevel() >= 0x3 || configuration.getConsoleConfigurations().getDebugLevel() < 0x0) {
            printMessage((byte) 0x02, "Invalid 'debugLevel' input. Valid range is 0x0 - 0x2.", consoleConfiguration);
            return false;
        } else if (configuration.getFakeServerRandomDelayDisconnectingTime() < 0 || configuration.getFakeServerRandomDelayDisconnectingTime() > 60) {
            printMessage((byte) 0x02, "Invalid 'RandomDelayDisconnectingTimer' input. Valid range is 0-60 seconds.", consoleConfiguration);
            return false;
        } else if (configuration.getBanLength() < 0) {
            printMessage((byte) 0x02, "Invalid 'BanLength'. Valid range is 0-" + Long.MAX_VALUE + "seconds. Use 0 to disable unban feature.", consoleConfiguration);
        } else if (configuration.getPortRangeStart() != -1 && (!validatePortNum(configuration.getPortRangeStart()) || !validatePortNum(configuration.getPortRangeEnd()))) {
            printMessage((byte) 0x02, "Invalid 'PortRangeStart' or 'PortRangeEnd' value(s). Valid range is 1-65535. Set 'PortRangeStart' to -1 to disable this feature.", consoleConfiguration);
            return false;
        }

        // Verify each input for specific ports
        for (final int port : configuration.getSpecificPorts()) {
            if (!validatePortNum(port)) {
                printMessage((byte) 0x02, "Invalid 'SpecificPorts' value: " + port + ". Valid range is 1-65535. Set 'SpecificPortsCount' to -1 to disable this feature.", consoleConfiguration);
                return false;
            } else if (configuration.getPortRangeStart() != -1 && configuration.getPortRangeStart() != -1 && (port - configuration.getPortRangeStart() >= 0 && port - configuration.getPortRangeEnd() <= 0)) {
                // Make sure specific ports not included in the range specification
                printMessage((byte) 0x02, "'SpecificPorts' value: " + port + " is already included in the port range configuration.", consoleConfiguration);
                return false;
            }
        }

        // Verify each input for excluded ports
        for (final Integer port : configuration.getExcludedPorts()) {
            if (!validatePortNum(port)) {
                printMessage((byte) 0x02, "Invalid 'ExcludedPorts' value: " + port + ". Valid range is 1-65535. Set 'ExcludedPortsCount' to -1 to disable this feature.", consoleConfiguration);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks valid port range
     */
    public static boolean validatePortNum(int PortNum) {
        return PortNum <= 65535 && PortNum >= 1;
    }

    /**
     * Determines what console configuration should use while reading configuration
     */
    private static CachedConfigurationValues.ConsoleConfigurationValues getConsoleConfigurationToUseWhileLoadingConfigurationFile(final CachedConfigurationValues configurationsBeingRead) {
        final CachedConfigurationValues currentInUseSettings = SharedRuntimeVariables.getCurrentCachedConfiguration();
        return currentInUseSettings != null ? currentInUseSettings.getConsoleConfigurations() : configurationsBeingRead != null ? configurationsBeingRead.getConsoleConfigurations() : CachedConfigurationValues.ConsoleConfigurationValues.builder().build();
    }
}
