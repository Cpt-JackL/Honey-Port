Honey Port
Author: Jack L
Website: http://jack-l.com

--------------------------------
License
--------------------------------
Copyright (C) 2014-2020 Jack L (http://jack-l.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
 *
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Library License:
Project Lombok (https://projectlombok.org/)
 - MIT: https://opensource.org/licenses/mit-license.php

--------------------------------
Introduction
--------------------------------
The main purpose of this application is to prevent TCP port scanning by listening on multiple ports. Once the client established a full TCP connection to one of the ports, this application will detects it, logs the connection and adds the IP address to the firewall to block. It is useful to hide actual application ports inside ports monitored by Honey Port.

Example:
A FTP port is on 44149 and a SSH port is on 44891. This application listen from 44000 to 44999 and excluding port 44149 and 44891. A half-open TCP scanner will report every port from 44000 to 44999 are open and a full open TCP connection will result an immediate ban from the server. This reduces the chance for an attacker to find the actual port for your FTP and SSH services. An example of Nmap scan output with this application running can be found at the end of this file.

--------------------------------
System Requirements
--------------------------------
 - Java Runtime Environment 8 or above is recommended.
 - [Optional] A firewall for banning detected IPs.

* Tested on:
- Linux with iptables and JRE 1.8.
- Windows Server 2012 R2 x64 with Windows Firewall and JRE 1.8
- Windows 10 Technical Preview Build 9879 with Windows Firewall and JRE 1.8

--------------------------------
Negative impacts
--------------------------------
Each listened port requires a thread. Listening ports from 44000 to 44999 is 1000 threads. Each new inbound connection requires a thread as well, but this thread should be ended immediately once the connection is closed or the IP is banned. Therefore listening on a wide range of ports might have negative impacts to the system, however we have not noticed any during our testing of the application.

We tested with 10000 threads on Windows Server 2012 with debug mode, CPU usage was mostly 0% on a 3.70Ghz 8 cores CPU. RAM usage was about 15MB.
We also tested 10000 threads on Linux, this reached number of threads allowance for each application. Will have to change this limitation for this many ports to run on Linux.

--------------------------------
Steps to run
--------------------------------
 1. Edit "Setting.conf" file accordingly
 2. Run the JAR file in a console with command "java -jar Honey_Port.jar"

--------------------------------
Run time commands
--------------------------------
Use '!h' to see a list of possible commands.

--------------------------------
NMAP Scanning Output Example:
NMAP targeting a Windows 10 Technical Preview machine with Honey Port running.
----------------------------------------
17818/tcp open  unknown
17819/tcp open  http       Apache httpd
17820/tcp open  tcpwrapped
17821/tcp open  unknown
17822/tcp open  mysql      MySQL 5.6.19-0ubuntu0.14.04.1
17823/tcp open  unknown
17824/tcp open  ssh        OpenSSH 5.3 (protocol 2.0)
17825/tcp open  ssh        OpenSSH 5.3 (protocol 2.0)
17826/tcp open  unknown

Warning: OSScan results may be unreliable because we could not find at least 1 open and 1 closed port
Device type: specialized|WAP|phone
Running: iPXE 1.X, Linksys Linux 2.4.X, Linux 2.6.X, Sony Ericsson embedded
OS CPE: cpe:/o:ipxe:ipxe:1.0.0%2b cpe:/o:linksys:linux_kernel:2.4 cpe:/o:linux:linux_kernel:2.6 cpe:/h:sonyericsson:u8i_vivaz
OS details: iPXE 1.0.0+, Tomato 1.28 (Linux 2.4.20), Tomato firmware (Linux 2.6.22), Sony Ericsson U8i Vivaz mobile phone
