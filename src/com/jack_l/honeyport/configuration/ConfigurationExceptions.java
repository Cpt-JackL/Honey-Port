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

/**
 * Exception for configuration file or validation error
 */
public class ConfigurationExceptions extends Exception {
    public ConfigurationExceptions(final Throwable e) {
        super(e);
    }

    public ConfigurationExceptions(final String s, final Throwable e) {
        super(s, e);
    }

    public ConfigurationExceptions(final String s) {
        super(s);
    }
}
