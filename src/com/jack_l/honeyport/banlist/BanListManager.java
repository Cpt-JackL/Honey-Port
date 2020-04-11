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

package com.jack_l.honeyport.banlist;

import com.jack_l.honeyport.configuration.CachedConfigurationValues;

import javax.security.auth.Destroyable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.jack_l.honeyport.console.ConsoleHandler.printMessage;

/**
 * Manages ban list, ban, unban operations should all go through here
 */
public class BanListManager implements Destroyable {
    private final CachedConfigurationValues configuration;
    private volatile Map<String, IPAddressData> bannedIps;
    private final Object dataSafetyLock = new Object();
    private final Thread unbanTimerThread;

    /**
     * Constructor
     */
    public BanListManager(final CachedConfigurationValues configuration) {
        this.configuration = configuration;
        final boolean isBanCmdAvailable = isBanCmdAvailable();
        final boolean isUnbanCmdAvailable = isUnbanCmdAvailable();

        if (isBanCmdAvailable && isUnbanCmdAvailable) {
            bannedIps = new LinkedHashMap<>();
            if (configuration.getBanLength() > 0) {
                unbanTimerThread = createAutoUnbanTimer();
                unbanTimerThread.start();
            } else {
                unbanTimerThread = null;
            }
        } else {
            bannedIps = null;
            unbanTimerThread = null;
        }

        if (!isBanCmdAvailable) {
            printMessage((byte) 0x01, "Ban list is not supported with current setting, Honey Port is running in detection mode only.");
        } else if (!isUnbanCmdAvailable) {
            printMessage((byte) 0x01, "Unban IP is not supported with current setting.");
        } else {
            printMessage((byte) 0x00, "Ban list successfully initialized.");
        }
    }

    private boolean isBanCmdAvailable() {
        return configuration.getBanCmd() != null && !configuration.getBanCmd().isEmpty() && !configuration.getBanCmd().equalsIgnoreCase("OFF");
    }

    private boolean isUnbanCmdAvailable() {
        return configuration.getUnbanCmd() != null && !configuration.getUnbanCmd().isEmpty() && !configuration.getUnbanCmd().equalsIgnoreCase("OFF");
    }

    private Thread createAutoUnbanTimer() {
        return new Thread(() -> {
            try {
                while (bannedIps != null) {
                    boolean sleep = true;
                    synchronized (dataSafetyLock) {
                        if (!bannedIps.isEmpty()) {
                            // Use peak to check if exp
                            final IPAddressData ipData = bannedIps.values().toArray(new IPAddressData[0])[0];
                            if (ipData.getExpireTime() <= new Date().getTime()) {
                                printMessage((byte) 0x00, "IP '" + ipData.getIpAddress().getHostAddress() + "' ban time has expired.");
                                removeBan(ipData, true);
                                // Do not delay here, multiple IPs could be added at the same time
                                sleep = false;
                            }
                        }
                    }
                    if (sleep) {
                        Thread.sleep(1000);
                    }
                }
            } catch (final InterruptedException e) {
                printMessage((byte) 0x10, "Auto unban timer is shutting down.");
            } catch (final Exception e) {
                printMessage((byte) 0x01, "Auto unban timer failed, auto unban will no longer work. (Exception: " + e + ")");
            }
        });
    }

    /**
     * Return an array of banned IPs
     */
    public String[] getIpList() {
        synchronized (dataSafetyLock) {
            if (bannedIps != null) {
                return bannedIps.keySet().toArray(new String[0]);
            }
        }
        return new String[0];
    }

    /**
     * Adds a new ban, only when ban command is applied
     */
    public void addBan(final InetAddress inetRemoteAddress) {
        final String remoteIp = inetRemoteAddress.getHostAddress();
        if (!isBanCmdAvailable()) {
            return;
        }

        // Check whitelist
        if (configuration.getIpWhiteList().contains(inetRemoteAddress)) {
            printMessage((byte) 0x04, "IP: '" + remoteIp + "' is in the whitelist, ignored.");
            return; // No need this thread anymore. Exit immediately
        }

        // Replacing %ip with actual detected IP address
        final String exeCmd = configuration.getBanCmd().replaceAll("%ip", remoteIp);

        // Prepare to execute command, only one execution at a time
        try {
            // Check banlist, make sure no duplicate bans
            synchronized (dataSafetyLock) {
                if (bannedIps != null && bannedIps.get(remoteIp) != null) {
                    printMessage((byte) 0x20, "IP '" + remoteIp + "' is already in the banned list.");
                    return;
                }

                // Executing cmd here.
                printMessage((byte) 0x10, "Executing cmd: " + exeCmd);
                Runtime.getRuntime().exec(exeCmd);
                printMessage((byte) 0x05, "Banned IP: " + remoteIp);

                if (bannedIps != null) {
                    final IPAddressData newBannedIp = new IPAddressData(inetRemoteAddress, new Date().getTime() + configuration.getBanLength() * 1000);
                    bannedIps.put(remoteIp, newBannedIp);
                }
            }
        } catch (Exception e) {
            printMessage((byte) 0x01, "Failed to execute command: " + exeCmd + ". (Exception: " + e + ")");
        }
    }

    @Override
    public void destroy() {
        if (unbanTimerThread != null) {
            unbanTimerThread.interrupt();
        }

        if (bannedIps != null) {
            removeAllBans();
            bannedIps.clear();
            bannedIps = null;
        }
    }

    private void removeAllBans() {
        synchronized (dataSafetyLock) {
            if (bannedIps != null) {
                for (IPAddressData ipData : bannedIps.values()) {
                    removeBan(ipData, false);
                }
                if (bannedIps.size() <= 0) {
                    printMessage((byte) 0x05, "All bans are removed.");
                } else {
                    printMessage((byte) 0x01, "Failed to remove " + bannedIps.size() + " IPs from ban list.");
                }
            }
        }
    }

    /**
     * Removes a ban with IP address, only when ban and unband commands are applied
     */
    public void removeBan(final String ipAddress) {
        synchronized (dataSafetyLock) {
            try {
                if (bannedIps != null) {
                    final InetAddress inetIpAddress = InetAddress.getByName(ipAddress);
                    final IPAddressData ipData = bannedIps.get(inetIpAddress.getHostAddress());
                    if (ipData != null) {
                        removeBan(ipData, false);
                    } else {
                        printMessage((byte) 0x01, "Failed to unban IP: " + ipAddress + ". (No such IP in ban list)");
                    }
                } else {
                    printMessage((byte) 0x01, "Failed to unban IP. (Ban list did not initialized)");
                }
            } catch (UnknownHostException e) {
                printMessage((byte) 0x01, "Failed to unban IP: " + ipAddress + ". (Invalid IP address)");
            }
        }
    }

    private void removeBan(final IPAddressData ipData, final boolean isRequestedFromAutoBan) {
        synchronized (dataSafetyLock) {
            if (bannedIps != null) {
                String exeCmd = configuration.getUnbanCmd().replaceAll("%ip", ipData.getInetAddress().getHostAddress());
                try {
                    printMessage((byte) 0x10, "Executing cmd: " + exeCmd);
                    Runtime.getRuntime().exec(exeCmd);
                    bannedIps.remove(ipData.getIpAddress().getHostAddress());
                    printMessage((byte) 0x05, "Unbanned IP: " + ipData.getIpAddress().getHostAddress());
                } catch (Exception e) {
                    printMessage((byte) 0x01, "Failed to execute command: " + exeCmd + ". (Exception: " + e + ")");
                } finally {
                    // If the request is from auto ban, remove the IP from the list regardless, otherwise this will end up in an infinite loop
                    if (isRequestedFromAutoBan && bannedIps.containsKey(ipData.getIpAddress().getHostAddress())) {
                        bannedIps.remove(ipData.getIpAddress().getHostAddress());
                    }
                }
            } else {
                printMessage((byte) 0x01, "Failed to unban IP. (Ban list did not initialized)");
            }
        }
    }
}
