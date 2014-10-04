Honey Port
Author: Jack L
Website: http://jack-l.com
Program written in Java

This program is still under Beta testing stage.

--------------------------------
License
--------------------------------
Copyright (C) 2014 Jack L (http://jack-l.com)

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

--------------------------------
Introduction
--------------------------------
This application is designed to prevent attacker/hacker to find out valid open ports on a server.
This application ONLY detects upon a full-open TCP connection (We took deeply consideration for this, be decided to use full-open instead of half-open mainly to reduce the chance of TCP IP spoofing on the inbound side).

It is useful to hide your valid ports inside a port range that is being listened by this application.

Example Scenario:
A valid SSH port is 22953, you can set this application to listen from 22900 to 23000 and excluding port 22953.

We call ports from 22900 to 23000 excluding 22953 the honey ports. This is why the application is called honey port.

A half-open TCP port scanner will respond that all ports from 22900 to 23000 are opened, but without knowing what service you are running. Once the attacker try to connect to one of the honey ports, they will be ban immediately from the server. It is unlikely they will find your SSH port that is listening on 22953.

A full-open TCP port scanner is likely detect first few open ports and then completely banned from the server.

However, you must set up this application correctly, especially BanCmd and UnbanCmd. Wrong configuration may result serious problem on your server.

Main Achievement: Reduce SSH/RDP attack on a server by creating honey ports to deflect hacking attempt.

--------------------------------
System Requirement
--------------------------------
Any system that runs Java.
You will need a firewall the accepts bash/cmd/powershell command to ban an IP automatically.
Currently only support on IPv4 and TCP only.
Tested on: Linux with iptables and Java 1.7.

--------------------------------
Negative impact
--------------------------------
Each listened port required a new thread running. If you are using this program from port 50000 - 59999, then 10000 threads will be created to listen on those ports.
Also, each new inbound connection (detection) will required a new thread as well, but this thread should be close immediately once the connection is closed or the IP is banned.
Listening on a big range of ports might have some negative impact to your system.

I tested with 10000 threads on Windows Server 2012 with debug mode, CPU usage was 0% on a 3.70Ghz 8 cores CPU. RAM usage was about 15MB.
I also tested 10000 threads with Linux, the maximum allowance threads for each program denied this application to create more threads at the end. (I was too lazy to change the limit)

Before you begin, you should check the maximum allowance threads for each process in your OS settings. Reaching maximum threads during runtime will make this program useless.

--------------------------------
Program Settings
--------------------------------
There is no argument required for this application.
All settings are located inside "Settings.conf" file.

Program.Debug - Controls printout debug messages
Valid range: (Integer) 0-2
0 - No debug message will be displayed
1 - Some debug message will be displayed
3 - All debug message will be displayed (This might cause some issue during shutdown process)

Program.UseColorCode - Use colour when printing out console messages
Valid range: (boolean) True or False
True - Enable this feature
False - Disable this feature (You should use "False" on Windows OS

Program.LogToFile - Log detection information to file
Log file name: Log_yyyy_MM_dd.log, yyyy_MM_dd is the year, month and date when this application starts.
Valid range: (boolean) True or False
True - Enable this feature
False - Disable this feature

General.BanCmd - Command to execute during a detection
Valid Range: (String) Any
Use OFF or leave it empty to disable this feature
WARNING:
* Incorrect setting of this variable might result serious problem on your server
* User that running this application must have privilege to execute the command, otherwise it will not work.
Example:
For Linux with iptables, you should use:
General.BanCmd=iptables -A INPUT -s %ip -j DROP
%ip will be replaced with detected IP on a detection

General.UnbanCmd - Command to execute when a ban time is expired
Valid Range: (String) Any
Use OFF or leave it empty to disable this feature
WARNING:
* Incorrect setting of this variable might result serious problem on your server
* User that running this application must have privilege to execute the command, otherwise it will not work.
Example:
For Linux with iptables, you should use:
General.UnbanCmd=iptables -D INPUT -s %ip -j DROP
%ip will be replaced with detected IP on a detection

---Port range listening specifications---
PortRange.Start - Range of ports to be listened
Valid range: (Integer) 1- 65535, should also be less than or equal to PortRange.End
Use -1 to turn this feature off

PortRange.Start - Range of ports to be listened
Valid range: (Integer) 1- 65535, should also be greater than or equal to PortRange.End
Use -1 in PortRange.Start to turn this feature off

---Other ports that are not included in port range specifications---
SpecPort.Count - How many additional port listeners do you need?
Valid range: (Integer) 0 - 65535
Use 0 to disable this feature

SpecPort.# - Additional port number to listen
Valid range: (Integer) 1-65535
Replace # with 1, 2, 3, 4...
To disable this feature, set SpecPort.Count to 0
For example:
SpecPort.1=1111
SpecPort.2=2222
You can add more by using "SpecPort.3", "SpecPort.4"... You can add as many up to the number you specified in "SpecPort.Count". The number in between must be continuous.

---Exclude specified port---
ExclPort.Count - How many ports you do not want this application to touch that are included in the range specification?
Valid range: (Integer) 0-65535
Use 0 to disable this feature

ExclPort.# - Port number to exclude
Valid range: (Integer) 1-65535
Replace # with 1, 2, 3, 4...
To disable this feature, set ExclPort.Count to 0
For example:
ExclPort.1=22
ExclPort.2=80
You can add more by using "ExclPort.3", "ExclPort.4"... You can add as many up to the number you specified in "ExclPort.Count". The number in between must be continuous.

Note: Port that are in use will be automatically skipped as well.

---IP White listing---
IPWhiteList.Count - How many IPs would you like to keep them in white list? Ban and unban cmd will be exclude for those IP address
Valid range: (Integer) 0 to inf
Use 0 to disable this feature

IPWhiteList.# - Specified IP address
Valid range: IPv4 address
To disable this feature, set IPWhiteList.Count to 0
For example:
IPWhiteList.1=127.0.0.1
IPWhiteList.2=192.168.0.1
You can add more by using "IPWhiteList.3", "IPWhiteList.4"... You can add as many up to the number you specified in "IPWhiteList.Count". The number in between must be continuous.
It is strongly suggested to keep 127.0.0.1 and the external IP address for the current machine inside this list.

--------------------------------
Run time commands
--------------------------------
Once all ports are created, you will be able to enter the following commands:
!q - Exit the application

!s ##### - Shutdown listener on a specified port
For example:
!s 8080