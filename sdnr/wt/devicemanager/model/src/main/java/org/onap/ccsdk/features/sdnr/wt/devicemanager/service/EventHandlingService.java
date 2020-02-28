/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.service;

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementDeviceType;

/**
 * Event Forwarding for central event management by devicemanager
 */
public interface EventHandlingService {

    /**
     * @param mountPointNodeName
     * @param deviceType
     */
    void connectIndication(String mountPointNodeName, NetworkElementDeviceType deviceType);

    /**
     * @param mountPointNodeName
     */
    void deRegistration(String mountPointNodeName);

    /**
     *
     * @param registrationName
     * @param attribute
     * @param attributeNewValue
     * @param nNode
     */
    void updateRegistration(String registrationName, String attribute, String attributeNewValue, NetconfNode nNode);

    /**
     * @param nodeIdString
     * @param nNode
     */
    void registration(String nodeIdString, NetconfNode nNode);

    /**
     * @param objectId
     * @param msg
     * @param value
     */
    void writeEventLog(String objectId, String msg, String value);

}
