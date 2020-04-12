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

import java.text.SimpleDateFormat;

public class SharedRuntimeVariables {
    // Constants
    public final static String version = "1.0.3";
    public final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static CachedConfigurationValues currentCachedConfigurationData = null;

    private static int detectionCount = 0;

    public static void setCurrentCachedConfiguration(final CachedConfigurationValues settings) {
        currentCachedConfigurationData = settings;
    }

    public static CachedConfigurationValues getCurrentCachedConfiguration() {
        return currentCachedConfigurationData;
    }

    public static void addDetection() {
        detectionCount++;
    }

    public static int getDetectionCount() {
        return detectionCount;
    }
}
