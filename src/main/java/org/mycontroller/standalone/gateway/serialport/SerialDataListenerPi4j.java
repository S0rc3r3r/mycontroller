/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.gateway.serialport;

import java.io.IOException;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.GatewayInfo;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.mycontroller.standalone.mysensors.RawMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialDataListenerPi4j implements SerialDataEventListener {
    private static Logger _logger = LoggerFactory.getLogger(SerialDataListenerPi4j.class.getName());
    StringBuilder message = new StringBuilder();
    private GatewayInfo gatewayInfo;

    public SerialDataListenerPi4j(GatewayInfo gatewayInfo) {
        this.gatewayInfo = gatewayInfo;

    }

    public void dataReceived(SerialDataEvent event) {
        try {
            byte[] buffer = event.getBytes();
            for (byte b : buffer) {
                if ((b == SerialPortCommon.MESSAGE_SPLITTER) && message.length() > 0) {
                    String toProcess = message.toString();
                    _logger.debug("Received a message:[{}]", toProcess);
                    //Send Message to message factory
                    ObjectFactory.getRawMessageQueue().putMessage(new RawMessage(toProcess));
                    message.setLength(0);
                } else if (b != SerialPortCommon.MESSAGE_SPLITTER) {
                    _logger.trace("Received a char:[{}]", ((char) b));
                    message.append((char) b);
                } else {
                    _logger.debug("Received MESSAGE_SPLITTER and current message length is ZERO! Nothing to do");
                }
            }
        } catch (IOException ex) {
            _logger.error("exception on pi4j data event, ", ex);
            gatewayInfo.getData().put(SerialPortCommon.IS_CONNECTED, false);
            gatewayInfo.getData().put(SerialPortCommon.CONNECTION_STATUS, ex.getMessage());
            message.setLength(0);
        } catch (RawMessageException rEx) {
            _logger.warn("RawMessage Exception,", rEx.getMessage());
            message.setLength(0);
        } catch (Exception ex) {
            _logger.error("Exception,", ex.getMessage());
            message.setLength(0);
        }
    }
}
