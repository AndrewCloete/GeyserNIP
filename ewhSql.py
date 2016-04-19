import datetime

def formatDataInsert(s):
    if s["Rstate"] == "ON":
        rstate = True
    else:
        rstate = False

    if s["Vstate"] == "OPEN":
        vstate = True
    else:
        vstate = False

    if s["Gstate"] == "OK":
        gstate = True
    else:
        gstate = False

    server_ts = datetime.datetime.today().strftime('%Y-%m-%d %H:%M:%S')
    client_ts = datetime.datetime.fromtimestamp(s["Tstamp"]).strftime('%Y-%m-%d %H:%M:%S')

    ts_insert_1 = "INSERT INTO timestamps(geyser_id, version, server_stamp, client_stamp, relay_state, valve_state, drip_detect,t1, t2, t3, t4, watt_avgpmin, kwatt_tot, hot_flow_ratepmin, hot_litres_tot, cold_flow_ratepmin, cold_litres_tot)"
    ts_insert_2 = "VALUES (%d,%d,'%s','%s',%r,%r,%r,%d,%d,%d,%d,%f,%f,%f,%f,%f,%f)" % (s["ID"], s["Ver"], server_ts, client_ts, rstate, vstate, gstate, s["T1"], s["T2"], s["T3"], s["T4"], s["KW"], s["KWH"], s["HLmin"], s["HLtotal"], s["CLmin"], s["CLtotal"])

    return ts_insert_1 + ts_insert_2

def formatControlInsert(s):
    server_ts = datetime.datetime.today().strftime('%Y-%m-%d %H:%M:%S')

    ts_insert_1 = "INSERT INTO tempcontrol(geyser_id, server_stamp, relay_state, setpoint)"
    ts_insert_2 = "VALUES (%d,'%s',%r,%f)" % (s["ID"], server_ts, s["Rstate"], s["setpoint"])

    return ts_insert_1+ts_insert_2

