#!/usr/bin/env python

"""
    SleekXMPP: The Sleek XMPP Library
    Implementation of xeps for Internet of Things
    http://wiki.xmpp.org/web/Tech_pages/IoT_systems
    Copyright (C) 2013 Sustainable Innovation, Joachim.lindborg@sust.se
    This file is part of SleekXMPP.

    See the file LICENSE for copying permission.
"""

import os
import sys
import socket

# This can be used when you are in a test environment and need to make paths right
sys.path=[os.path.join(os.path.dirname(__file__), '../..')]+sys.path

import logging
import unittest
import distutils.core
import datetime

from glob import glob
from os.path import splitext, basename, join as pjoin
from optparse import OptionParser
from urllib import urlopen

import sleekxmpp
# Python versions before 3.0 do not use UTF-8 encoding
# by default. To ensure that Unicode is handled properly
# throughout SleekXMPP, we will set the default encoding
# ourselves to UTF-8.
if sys.version_info < (3, 0):
    from sleekxmpp.util.misc_ops import setdefaultencoding
    setdefaultencoding('utf8')
else:
    raw_input = input
    
from sleekxmpp.plugins.xep_0323.device import Device as SensorDevice
from sleekxmpp.plugins.xep_0325.device import Device as ControlDevice

#from sleekxmpp.exceptions import IqError, IqTimeout


class IoT_TestDevice(sleekxmpp.ClientXMPP):

    """
    A simple IoT device that can act as server or client both on xep 323 and 325
    """
    
    def __init__(self, jid, password):
        sleekxmpp.ClientXMPP.__init__(self, jid, password)

        self.register_plugin('xep_0030')
        self.register_plugin('xep_0323')
        self.register_plugin('xep_0325')
        self.register_plugin('xep_0199') # XMPP ping

        self.add_event_handler("session_start", self.session_start)
        self.add_event_handler("message", self.message)
        self.add_event_handler("changed_status",self.manage_status)

        #Some local status variables to use
        self.device=None
        self.releaseMe=False
        self.beServer=True
        self.clientJID=None
        self.controlJID=None
        self.received=set()
        self.controlField=None
        self.controlValue=None
        self.controlType=None
        self.delayValue=None
        self.toggle=0

    def datacallback(self,from_jid,result,nodeId=None,timestamp=None,fields=None,error_msg=None):
        """
        This method will be called when you ask another IoT device for data with the xep_0323
        fields example
        [{'typename': 'numeric', 'unit': 'C', 'flags': {'momentary': 'true', 'automaticReadout': 'true'}, 'name': 'temperature', 'value': '13.5'},
        {'typename': 'numeric', 'unit': 'mb', 'flags': {'momentary': 'true', 'automaticReadout': 'true'}, 'name': 'barometer', 'value': '1015.0'},
        {'typename': 'numeric', 'unit': '%', 'flags': {'momentary': 'true', 'automaticReadout': 'true'}, 'name': 'humidity', 'value': '78.0'}]
        """
        
        if error_msg:
            logging.error('we got problem when recieving data %s', error_msg)
            return
        
        if result=='accepted':
            logging.debug("we got accepted from %s",from_jid)            
        elif result=='fields':
            logging.info("we got fields from %s on node %s",from_jid,nodeId)
            for field in fields:
                info="(%s %s %s) " % (nodeId,field['name'],field['value'])
                if field.has_key('unit'):
                    info+="%s " % field['unit']
                if field.has_key('flags'):
                    info+="["
                    for flag in field['flags'].keys():
                        info+=flag + ","
                    info+="]"
                logging.info(info)
        elif result=='done':
            logging.debug("we got  done from %s",from_jid)

    def controlcallback(self,from_jid,result,error_msg,nodeIds=None,fields=None):
        """
        Called as respons to a xep_0325 control message 
        """
        logging.info('Control callback from %s result %s error %s',from_jid,result,error_msg)

    def getformcallback(self,from_jid,result,error_msg):    
        """
        called as respons to a xep_0325 get Form iq message
        """
        logging.debug("IoT got a form "+str(result))
        
    def beClientOrServer(self,server=True,clientJID=None,controlJID=None,controlField=None,controlValue=None,controlType='boolean' ):
        if server:
            self.beServer=True
            self.clientJID=None
        elif clientJID:
            self.beServer=False
            self.clientJID=clientJID            
        else:
            self.beServer=False
            self.controlJID=controlJID
            self.controlField=controlField
            self.controlValue=controlValue
            self.controlType=controlType

    def testForRelease(self):
        # todo thread safe
        return self.releaseMe

    def doReleaseMe(self):
        # todo thread safe
        self.releaseMe=True
        
    def addDevice(self, device):
        self.device=device

    def printRoster(self):
        logging.debug('Roster for %s' % self.boundjid.bare)
        groups = self.client_roster.groups()
        for group in groups:
            logging.debug('\n%s' % group)
            logging.debug('-' * 72)
            for jid in groups[group]:
                sub = self.client_roster[jid]['subscription']
                name = self.client_roster[jid]['name']
                if self.client_roster[jid]['name']:
                    logging.debug(' %s (%s) [%s]' % (name, jid, sub))
                else:
                    logging.debug(' %s [%s]' % (jid, sub))
                    
                connections = self.client_roster.presence(jid)
                for res, pres in connections.items():
                    show = 'available'
                    if pres['show']:
                        show = pres['show']
                    logging.debug('   - %s (%s)' % (res, show))
                    if pres['status']:
                        logging.debug('       %s' % pres['status'])

    def manage_status(self, event):
        logging.debug("got a status update" + str(event.getFrom()))
        self.printRoster()
        
    def session_start(self, event):
        self.send_presence()
        self.get_roster()
        # tell your preffered friend that you are alive 
        self.send_message(mto='jocke@jabber.sust.se', mbody=self.boundjid.bare +' is now online use xep_323 stanza to talk to me')

        if not(self.beServer):
            if self.clientJID:
                logging.info('We are a client start asking %s for values' % self.clientJID)
                self.schedule('end', self.delayValue, self.askClientForValue, repeat=True, kwargs={})
            elif self.controlJID:
                logging.info('We are a control client set field %s to value %s on %s every %s',self.controlField,self.controlValue, self.controlJID, self.delayValue)
                self.schedule('end', self.delayValue, self.sendControlMessage, repeat=True, kwargs={})

    def message(self, msg):
        if msg['type'] in ('chat', 'normal'):
            if msg['body'].startswith('hi'):
                logging.info("got normal chat message" + str(msg))
                internetip=urlopen('http://icanhazip.com').read()
                localip=socket.gethostbyname(socket.gethostname())
                msg.reply("I am " + self.boundjid.full + " and I am on localIP " +localip +" and on internet " + internetip).send()
            elif msg['body'].startswith('?'):
                logging.debug('got a question ' + str(msg))
                self.device.refresh([])
                logging.debug('momentary values' + str(self.device.momentary_data))
                msg.reply(str(self.device.momentary_data)).send()
            elif msg['body'].startswith('T'):
                logging.debug('got a toggle ' + str(msg))
                if self.device.relay:
                    self.device.relay=False
                else:
                    self.device.srelay=True
            elif msg['body'].find('=')>0:
                logging.debug('got a control' + str(msg))
                (variable,value)=msg['body'].split('=')
                logging.debug('setting %s to %s' % (variable,value))
            else:
                logging.debug('message dropped ' +  msg['body'])
        else:
            logging.debug("got unknown message type %s", str(msg['type']))

    def askClientForValue(self):
        #need to find the full jid to call for data
        connections=self.client_roster.presence(self.clientJID)

        logging.debug('IoT will call for data to '+ str(connections))
        for res, pres in connections.items():
            # ask every session on the jid for data
            if self.controlField:
                session=self['xep_0323'].request_data(self.boundjid.full,self.clientJID+"/"+res,self.datacallback, fields=[self.controlField],flags={"momentary":"true"})
            else:
                session=self['xep_0323'].request_data(self.boundjid.full,self.clientJID+"/"+res,self.datacallback, flags={"momentary":"true"})

    def sendControlMessage(self):
        #need to find the full jid to call for data
        connections=self.client_roster.presence(self.controlJID)
        for res, pres in connections.items():
            # ask every session on the jid for data
            # session=self['xep_0325'].get_form(self.boundjid.full,self.controlJID+"/"+res,self.getformcallback)

            if not self.controlField:
                #no fields provided default to toggle a relay:
                if self.toggle:
                    self.toggle=0
                    logging.info('IoT will send relay true to '+ str(connections))
                    #session=self['xep_0325'].set_request(self.boundjid.full,self.controlJID+"/"+res,self.controlcallback,[("relay","boolean","1")])
                    session=self['xep_0325'].set_request(self.boundjid.full,self.controlJID+"/"+res,self.controlcallback,[("relay","boolean","true")])
                    #session=self['xep_0325'].set_command(self.boundjid.full,self.controlJID+"/"+res,[("relay","boolean","true")])
                else:
                    self.toggle=1
                    logging.info('IoT will send relay false to '+ str(connections))
                    #session=self['xep_0325'].set_request(self.boundjid.full,self.controlJID+"/"+res,self.controlcallback,[("relay","boolean","0")])
                    session=self['xep_0325'].set_request(self.boundjid.full,self.controlJID+"/"+res,self.controlcallback,[("relay","boolean","false")])
                    #session=self['xep_0325'].set_command(self.boundjid.full,self.controlJID+"/"+res,[("relay","boolean","false")])
            else:
                logging.info('IoT will set %s to %s on to %s'%(self.controlField,self.controlValue,str(connections)))
                session=self['xep_0325'].set_request(self.boundjid.full,self.controlJID+"/"+res,self.controlcallback,[(self.controlField,self.controlType,self.controlValue)])
            
class TheDevice(SensorDevice,ControlDevice):
    """
    Xep 323 SensorDevice
    This is the actual device object that you will use to get information from your real hardware
    You will be called in the refresh method when someone is requesting information from you

    xep 325 ControlDevice
    This 
    """
    def __init__(self,nodeId):
        SensorDevice.__init__(self,nodeId)
        ControlDevice.__init__(self,nodeId)
        self.counter=0
        self.relay=0


    def refresh(self,fields):
        """
        the implementation of the refresh method
        """
        self._set_momentary_timestamp(self._get_timestamp())
        self.counter=self.counter+1
        self._add_field_momentary_data("Counter", self.counter)
        self._add_field_momentary_data("Relay", self.relay)

    
    def _set_field_value(self, name,value):
        """ overrides the set field value from device to act on my local values                                            
        """
        if name=="Toggle":
            if self.relay:
                self.relay=False
            else:
                self.relay=True
                self._set_momentary_timestamp(self._get_timestamp())
            self._add_field_momentary_data("Relay", self.relay)
        elif name=="Relay":
            self.relay=int(value)
            self._set_momentary_timestamp(self._get_timestamp())
            self._add_field_momentary_data("Relay", self.relay)
        elif name=="Counter":
            self.counter=int(value)
            self._set_momentary_timestamp(self._get_timestamp())
            self._add_field_momentary_data("Counter", self.counter)
        
if __name__ == '__main__':

    # Setup the command line arguments.
    #
    # This script can for xep 323 act both as
    #   "server" an IoT device that can provide sensorinformation
    #   python IoT_TestDevice.py -j "poviderOfDataDevicedJID@yourdomain.com" -p "password" -n "TestIoT" --debug
    #
    #   "client" an IoT device or other party that would like to get data from another device every minute
    #   python IoT_TestDevice.py -j "loginJID@yourdomain.com" -p "password" -g "clienttocallfordata@yourdomain.com" --delay 60 --debug
    #
    # This script can for xep 325 act both as
    #   "server" an IoT device that can recieve a control command and act upon it
    #   python IoT_TestDevice.py -j "poviderOfControlDevicedJID@yourdomain.com" -p "password" -n "TestIoT" --debug
    #
    #   "client" an IoT device or other party that would like to send a control message to a field in another device every 60 second
    #   python IoT_TestDevice.py -j "loginJID@yourdomain.com" -p "password" -c "clienttocallfordata@yourdomain.com" --field "relay" --value "1" --delay 60 --debug
    #
    #   "client" an IoT device, special case that will toggle the relay every 6 second
    #   python IoT_TestDevice.py -j "loginJID@yourdomain.com" -p "password" -c "clienttocallfordata@yourdomain.com" --delay 6 --debug
    
    optp = OptionParser()

    # Output verbosity options.
    optp.add_option('-q', '--quiet', help='set logging to ERROR',
                    action='store_const', dest='loglevel',
                    const=logging.ERROR, default=logging.INFO)
    optp.add_option('-d', '--debug', help='set logging to DEBUG',
                    action='store_const', dest='loglevel',
                    const=logging.DEBUG, default=logging.INFO)
    optp.add_option('-v', '--verbose', help='set logging to COMM',
                    action='store_const', dest='loglevel',
                    const=5, default=logging.INFO)


    # JID and password options.
    optp.add_option("-j", "--jid", dest="jid",
                    help="JID to use")
    optp.add_option("-p", "--password", dest="password",
                    help="password to use")
    optp.add_option("--phost", dest="proxy_host",
                    help="Proxy hostname", default = None)
    optp.add_option("--pport", dest="proxy_port",
                    help="Proxy port", default = None)
    optp.add_option("--puser", dest="proxy_user",
                    help="Proxy username", default = None)
    optp.add_option("--ppass", dest="proxy_pass",
                    help="Proxy password", default = None)

    # Useful to test
    optp.add_option('-t', '--pingto', help='set jid to ping',
                    action='store', type='string', dest='pingjid',
                    default=None)
    
    # IoT test
    optp.add_option("-n", "--nodeid", dest="nodeid",
                    help="Server (Provider) I am a device that can be called for data or send control to", default=None)
    optp.add_option("-g", "--getsensorjid", dest="getsensorjid",
                    help="Device to call for data on", default=None)
    optp.add_option("-c", "--controljid", dest="controljid",
                    help="Device to call for data on", default=None)
    optp.add_option("--field", dest="controlfield",
                    help="Field to act upon", default=None)
    optp.add_option("--value", dest="controlvalue",
                    help="control value", default=None)
    optp.add_option("--type", dest="controltype",
                    help="type of value", default="boolean")
    optp.add_option("--delay", dest="delayvalue",
                    help="secondsdelay between reads or controls", default=30)
    
    opts, args = optp.parse_args()

     # Setup logging.
    logging.basicConfig(level=opts.loglevel,
                        format='%(levelname)-8s %(message)s')

    if opts.jid is None:
        opts.jid = raw_input("Username: ")
    if opts.password is None:
        opts.password = raw_input("Password: ")
        
    xmpp = IoT_TestDevice(opts.jid,opts.password)
    xmpp.delayValue=int(opts.delayvalue)
    logging.debug("DELAY " + str(int(opts.delayvalue)) + "  " + str(xmpp.delayValue))
    
    if opts.proxy_host:
        xmpp.use_proxy = True
        xmpp.proxy_config = {
            'host' : opts.proxy_host,
            'port' : int(opts.proxy_port),
            'username' : opts.proxy_user,
            'password' : opts.proxy_pass}
    
    if opts.nodeid:
        # prepare the IoT_TestDevice to be a provider of data and be able to recieve control commands
        # xmpp['xep_0030'].add_feature(feature='urn:xmpp:sn',
        # node=opts.nodeid,
        # jid=xmpp.boundjid.full)

        # Instansiate the device object
        myDevice = TheDevice(opts.nodeid);
        myDevice._add_field(name="Relay", typename="numeric", unit="Bool");
        myDevice._add_control_field(name="Relay", typename="numeric", value=1);
        myDevice._add_field(name="Counter", typename="numeric", unit="Count");
        myDevice._set_momentary_timestamp(myDevice._get_timestamp())
        myDevice._add_field_momentary_data("Counter", "0", flags={"automaticReadout": "true","momentary":"true"});
        myDevice._add_field_momentary_data("Relay", "0", flags={"automaticReadout": "true","momentary":"true","writable":"true"});
        xmpp.device=myDevice

        xmpp['xep_0323'].register_node(nodeId=opts.nodeid, device=myDevice, commTimeout=10);
        xmpp['xep_0325'].register_node(nodeId=opts.nodeid, device=myDevice, commTimeout=10);
        xmpp.beClientOrServer(server=True)
        # while not(xmpp.testForRelease()):
        xmpp.connect()
        xmpp.process(block=True)    
        logging.debug("lost connection")
            
    elif opts.getsensorjid:
        logging.debug("will try to call another device for data")
        xmpp.beClientOrServer(server=False,clientJID=opts.getsensorjid)
        xmpp.connect()
        xmpp.process(block=True)
        logging.debug("ready ending")

    elif opts.controljid:
        logging.debug("will try to send control message to another device")
        xmpp.beClientOrServer(server=False,controlJID=opts.controljid,controlField=opts.controlfield,controlValue=opts.controlvalue,controlType=opts.controltype)
        xmpp.connect()
        xmpp.process(block=True)
        logging.debug("ready ending")
    else:
       print "noopp didn't happen"
