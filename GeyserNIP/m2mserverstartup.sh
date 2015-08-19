#!/bin/bash

#------------------------------------------------------ SERVER SETUP ------------------------------------------------------

##SUN server IP
146.232.128.163

#### Install Java OpenJDK 7.
sudo yum install java-1.7.0-openjdk 		#For JRE
sudo yum install java-1.7.0-openjdk-devel	#For JDK

#### Upload Tomcat and OM2M
tar zxvf file.tar.gz #If needed to uncompress
#Set the CATALINA_HOME envorinment variable!! E.g in ~/.bashrc put
export CATALINA_HOME=/home/16441842/tomecat/apache-tomcat-7.0.61:$PATH
#Edit apache-tomcat-7/conf/server.xml to use port 80 instead of 8080.



# Email log files.
# In crontab every 10 minutes
tail -n 20 `find /home/16441842/serverlogs -type f -mmin -10` | mutt andrewhcloete@gmail.com

#View running Java processes
ps aux | grep java

#--------------------------------------------------------------------------------------------------------------------------

#------------------------------------------------------ SOFTWARE ----------------------------------------------------------
### REMEMBER SUDO SU ####

Delete old log files

# Navigate to NSCL and enter
nohup ./start.sh > /dev/null 2>&1 &
#nohup /home/16441842/om2m/OM2M-0.8.0/NSCL/start.sh > /dev/null 2>&1 &

# Start Tomcat server
nohup /home/16441842/tomcat/apache-tomcat-7.0.61/bin/startup.sh > /dev/null 2>&1 &

# Start Geyser NIP server <NSCL base URI> <Auth>  <UDPServer port> <aPoc URL>  <aPoc server port>  <registration timout>
nohup java -jar /home/16441842/geysernip/GeyserNIP.jar localhost:8080/om2m/nscl admin:admin 3535 localhost 9090 30 > /dev/null 2>&1 &

# Start Geyser Database Network Application <NSCL base URI> <aPoc URL> <aPoc PORT>  <database URL> <database USER>
nohup java -jar /home/16441842/geyserdatabase/GeyserDatabase.jar localhost:8080/om2m/nscl admin:admin localhost 9595 localhost m2mdatabasena > /dev/null 2>&1 &

#Start GeyserSetpointcontroller <NSCL IP address> <aPoc URL> <aPoc PORT> <Deadband>
nohup java -jar /home/16441842/geysersetpointcontroller/GeyserSetpointcontroller.jar > /dev/null 2>&1 &

# Install crontab <NSCL IP address>
0,15,30,45 * * * * java -jar /home/16441842/geyserscheduler/GeyserScheduler.jar

#--------------------------------------------------------------------------------------------------------------------------

## EC2 simulator
nohup java -jar /home/ubuntu/GeyserSimulator.jar 146.232.128.163 3535 1 > /dev/null 2>&1 &


#nohup java -jar GeyserDatabase.jar 52.10.236.177 aloeferox1.dyndns.org 9595 192.168.100.73 andrewhcloete > /dev/null 2>&1 &
#nohup python MODEMserial_geyserclient.py 1  > /dev/null 2>&1 &
#nohup python DSLudp_geyserclient.py 52.10.236.177 3535 2 > /dev/null 2>&1 &


#------------------------------------------------------ NOTES ----------------------------------------------------------
# Remeber to include jaxb.index to all JARs that use the OM2M libraries
# Confirm that JSON API is correct. (Casting from Double to Long not accepted)
