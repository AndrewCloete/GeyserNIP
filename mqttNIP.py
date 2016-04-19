import SocketServer
import paho.mqtt.client as mqtt
import json
import logging
import threading
import sys
import time

import MySQLdb
from ewhSql import formatDataInsert

LOG_FILENAME = 'mqttNIP.log'
FORMAT = '%(levelname)s %(asctime)-15s %(message)s'
logging.basicConfig(filename=LOG_FILENAME, level=logging.DEBUG, format=FORMAT)
log = logging.getLogger()
log.info("---------------------------------------------------------------")
log.info("MQTT NIP started")



#Connect to MQTT broker
mqttc=mqtt.Client()
mqttc.connect("52.31.251.102",1883,60)
log.info("Connected to MQTT broker")

# Connect to database
db = MySQLdb.connect(host="146.232.128.163", user="intelligeyser", passwd="ewhM2Mnscl", db="GeyserM2M")
cur = db.cursor()
log.info(db)



#Class handler for inbound UDP msgs
# ---------------------------------------------------------------------------------------------------------------------

class ThreadedUDPRequestHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        data = self.request[0].strip()
        socket = self.request[1]
        log.info("New UDP tstamp from %s, %d"%(self.client_address[0], self.client_address[1]))
        try:
            ts = {}
            tstamp = json.loads(data)

            #Map to new names
            ts["ewhId"] = str(tstamp["ID"])
            ts["ver"] = str(tstamp["Ver"])
            ts["time"] = int(time.time())
            ts["V"] = tstamp["Vstate"]
            ts["R"] = tstamp["Rstate"]
            ts["G"] = tstamp["Gstate"]
            ts["T1"] = tstamp["T1"]
            ts["T2"] = tstamp["T2"]
            ts["T3"] = tstamp["T3"]
            ts["T4"] = tstamp["T4"]
            ts["W"] = int(tstamp["KW"])*1000
            ts["Wh"] = int(tstamp["KWH"])*1000
            ts["Hd"] = int(tstamp["HLtotal"])
            ts["Hm"] = int(tstamp["HLmin"])
            ts["Cd"] = int(tstamp["CLtotal"])
            ts["Cm"] = int(tstamp["CLmin"])
            
            #Debug
            print(json.dumps(ts))

            #Publish the new formatted datapoint to broker
            mqttc.publish("ewh/ts/%d"%tstamp["ID"],json.dumps(ts),2)

            #Save the stamp to the old DB as backup
            cur.execute(formatDataInsert(tstamp))
            db.commit()

        except ValueError:
            log.error("Invalid JSON: Value error")
        except KeyError:
            log.error("Invalid JSON: Key error")
        except MySQLdb.Error, e:
            log.error("MySQL Error [%d]: %s" % (e.args[0], e.args[1]))

#    def finish():
#       self.shutdown()

class ThreadedUDPServer(SocketServer.ThreadingMixIn, SocketServer.UDPServer):
    pass
# ----------------------------------------------------------------------------------------------------------------------





# Start UDP listening
HOST, PORT = "localhost", 6565 
udpserver = ThreadedUDPServer((HOST, PORT), ThreadedUDPRequestHandler) 
udp_thread = threading.Thread(target=udpserver.serve_forever) 
log.info("UDP serving at port %s %d" % (HOST, PORT)) 
udp_thread.start() 


# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
try:
    mqttc.loop_forever()
except KeyboardInterrupt:
    udpserver.shutdown()
    udpserver.server_close()
    sys.exit()
