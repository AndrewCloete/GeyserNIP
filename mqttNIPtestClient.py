import socket
import json

UDP_IP = "127.0.0.1"
UDP_PORT = 6565
MESSAGE = {"Ver":20,"ID":302,"Vstate":"OPEN","Rstate":"ON","Gstate":"OK","T1":54,"T2":49,"T3":39,"T4":38,"KW":0.005585,"KWH":2.155529,"HLmin":0.000000,"CLmin":0.000000,"HLtotal":23.740005,"CLtotal":49.060001,"Tstamp":1454759322}

print "UDP target IP:", UDP_IP
print "UDP target port:", UDP_PORT
print "message:", MESSAGE

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
sock.sendto(json.dumps(MESSAGE), (UDP_IP, UDP_PORT))
