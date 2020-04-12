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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class CachedConfigurationValues {

    @Builder
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class ConsoleConfigurationValues {
        /**
         * UseColorCode - Display message with color or not
         */
        @Builder.Default
        private boolean useColorCode = false;

        /**
         * DebugLevel - Controls how much debug message is being output and log 0000
         * (0x0) - OFF 0001 (0x1) - General 0010 (0x2) - Detailed
         */
        @Builder.Default
        private byte debugLevel = 0x1;

        /**
         * Variable: LogToFile - Log detections to file
         */
        @Builder.Default
        private Byte logLevel = 0x0; //Default must be off
    }

    private ConsoleConfigurationValues consoleConfigurations;

    /**
     * Variable: BanCmd - Command to execute during a detection Use OFF to
     * disable ban feature
     */
    @Builder.Default
    private String banCmd = "OFF";

    /**
     * Variable: UnbanCmd - Command to execute during a detection Use OFF to
     * disable unban feature
     */
    @Builder.Default
    private String unbanCmd = "OFF";

    /**
     * Variable: BanLength - Time in seconds to keep the IP banned Use 0 to ban
     * permanently
     */
    @Builder.Default
    private long banLength = 0;

    /**
     * Fake Server welcome message
     */
    @Builder.Default
    private int fakeServerRandomDelayDisconnectingTime = 0;
    @Builder.Default
    private int randomWelcomeMessageCount = 0;
    private String[] randomWelcomeMessageType;
    private String[] randomWelcomeMessage;

    /**
     * Port Range settings, see read me file for detail
     */
    @Builder.Default
    private int portRangeStart = -1;
    @Builder.Default
    private int portRangeEnd = 0;

    @Builder.Default
    private Integer[] specificPorts = new Integer[0];

    @Builder.Default
    private Set<Integer> excludedPorts = new HashSet<>();

    @Builder.Default
    private Set<InetAddress> ipWhiteList = new HashSet<>();

    public boolean isThisCurrentlyActiveConfiguration() {
        return SharedRuntimeVariables.getCurrentCachedConfiguration().equals(this);
    }
}
