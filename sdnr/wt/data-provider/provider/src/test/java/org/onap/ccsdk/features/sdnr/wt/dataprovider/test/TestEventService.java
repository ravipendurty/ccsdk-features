/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.HtDatabaseEventsService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SourceType;

/**
 * @author Michael Dürre
 *
 */
public class TestEventService {
    private static ElasticSearchDataProvider dbProvider;
    private static HtDatabaseClient dbRawProvider;
    private static HtDatabaseEventsService service = null;

    private static final String NODEID = "node1";
    private static final String NODEID2 = "node2";
    private static final String OBJECTREFID1 = "objid1";
    private static final String OBJECTREFID2 = "objid2";

    @BeforeClass
    public static void init() throws Exception {

        dbProvider = new ElasticSearchDataProvider(TestCRUDforDatabase.hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
        dbRawProvider = new HtDatabaseClient(TestCRUDforDatabase.hosts);

        try {
            service = new HtDatabaseEventsService(dbRawProvider, dbProvider);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testClearFaultsCurrent() {

        service.clearFaultsCurrentOfNode(NODEID);
        service.clearFaultsCurrentOfNode(NODEID2);

        List<String> nodeIds = service.getAllNodesWithCurrentAlarms();
        if (nodeIds.size() > 0) {
            for (String nodeId : nodeIds) {
                service.clearFaultsCurrentOfNode(nodeId);
            }
        }
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID1, "abc", SeverityType.Major));
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID1, "abcde", SeverityType.Major));
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID2, "abcde", SeverityType.Major));
        service.updateFaultCurrent(createFault(NODEID2, OBJECTREFID2, "abcde", SeverityType.Major));
        nodeIds = service.getAllNodesWithCurrentAlarms();
        assertTrue(nodeIds.size() == 2);
        service.clearFaultsCurrentOfNodeWithObjectId(NODEID, OBJECTREFID1);
        nodeIds = service.getAllNodesWithCurrentAlarms();
        assertTrue(nodeIds.size() == 2);
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID2, "abcde", SeverityType.NonAlarmed));
        nodeIds = service.getAllNodesWithCurrentAlarms();
        assertTrue(nodeIds.size() == 1);
    }

    private static FaultcurrentEntity createFault(String nodeId, String objectRefId, String problem,
            SeverityType severity) {
        return createFault(nodeId, objectRefId, problem, severity, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    /**
     * @param nODENAME
     * @param problem
     * @param ts
     * @param severity
     * @return
     */
    private static FaultcurrentEntity createFault(String nodeId, String objectRefId, String problem,
            SeverityType severity, DateAndTime ts) {
        return new FaultcurrentBuilder().setNodeId(nodeId).setObjectId(objectRefId).setTimestamp(ts)
                .setSeverity(severity).setProblem(problem).build();
    }

    @Test
    public void testIndexClean() {
        Date now = new Date();
        service.doIndexClean(now);
        clearDbEntity(Entity.Eventlog);
        clearDbEntity(Entity.Faultlog);
        TestCRUDforDatabase.trySleep(1000);
        service.writeEventLog(createEventLog(NODEID, OBJECTREFID1, "aaa", "abc", 1));
        service.writeEventLog(createEventLog(NODEID, OBJECTREFID1, "aaa", "avasvas", 2));

        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.Major, 1));
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.NonAlarmed, 2));
        service.writeFaultLog(createFaultLog(NODEID2, OBJECTREFID2, "problem", SeverityType.Major, 1));
        TestCRUDforDatabase.trySleep(100);
        now = new Date();
        int numOlds = service.getNumberOfOldObjects(now);
        assertEquals(5, numOlds);
        TestCRUDforDatabase.trySleep(100);
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.Major, 3));
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.NonAlarmed, 5));
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.Major, 6));
        numOlds = service.getNumberOfOldObjects(now);
        assertEquals(5, numOlds);
        now = new Date();
        numOlds = service.getNumberOfOldObjects(now);
        assertEquals(8, numOlds);
        service.doIndexClean(now);
        numOlds = service.getNumberOfOldObjects(now);
        assertEquals(0, numOlds);

    }

    @Test
    public void testPm() {
        final String IFNAME1 = "if1";
        final String SCNID1 = "scn1";
        List<PmdataEntity> list =
                Arrays.asList(createPmData(NODEID, IFNAME1, SCNID1), createPmData(NODEID, IFNAME1, SCNID1),
                        createPmData(NODEID, IFNAME1, SCNID1), createPmData(NODEID, IFNAME1, SCNID1)

                );
        service.doWritePerformanceData(list);
    }

    /**
     * @param ifname
     * @param ifUuid
     * @param scannerId
     * @param nodename3
     * @return
     */
    private static PmdataEntity createPmData(String nodeId, String ifUuid, String scannerId) {
        return new PmdataEntityBuilder().setNodeName(nodeId).setGranularityPeriod(GranularityPeriodType.Period15Min)
                .setUuidInterface(ifUuid).setScannerId(scannerId).setLayerProtocolName("NETCONF")
                .setPerformanceData(null).setSuspectIntervalFlag(true)
                .setTimeStamp(NetconfTimeStampImpl.getConverter().getTimeStamp()).build();
    }

    @Test
    public void testNeConnection() {
        service.removeNetworkConnection(NODEID);
        service.removeNetworkConnection(NODEID2);

        clearDbEntity(Entity.NetworkelementConnection);
        List<NetworkElementConnectionEntity> nes = service.getNetworkElementConnections();
        assertEquals(0, nes.size());
        service.updateNetworkConnection22(createNeConnection(NODEID, NetworkElementDeviceType.Unknown), NODEID);
        service.updateNetworkConnection22(createNeConnection(NODEID2, NetworkElementDeviceType.ORAN), NODEID2);
        nes = service.getNetworkElementConnections();
        assertEquals(2, nes.size());
        service.updateNetworkConnectionDeviceType(createNeConnection(NODEID, NetworkElementDeviceType.Wireless),
                NODEID);
        nes = service.getNetworkElementConnections();
        assertEquals(2, nes.size());
        boolean found = false;
        for (NetworkElementConnectionEntity ne : nes) {
            if (NODEID.equals(ne.getNodeId()) && ne.getDeviceType() == NetworkElementDeviceType.Wireless) {
                found = true;
            }
        }
        assertTrue(found);

    }

    @Test
    public void testConnectionLog() {
        clearDbEntity(Entity.Connectionlog);
        service.writeConnectionLog(createConnectionLog(NODEID, ConnectionLogStatus.Mounted));
        service.writeConnectionLog(createConnectionLog(NODEID, ConnectionLogStatus.Mounted));
        assertEquals(2, getDbEntityEntries(Entity.Connectionlog).getTotal());
    }

    /**
     * @param nodeId
     * @param status
     * @return
     */
    private static ConnectionlogEntity createConnectionLog(String nodeId, ConnectionLogStatus status) {
        return new ConnectionlogBuilder().setNodeId(nodeId)
                .setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp()).setStatus(status).build();
    }

    @Test
    public void testInventory() {
        clearDbEntity(Entity.Inventoryequipment);
        service.writeInventory(createEquipment(NODEID, "uuid1"));
        service.writeInventory(createEquipment(NODEID, "uuid2"));
        service.writeInventory(createEquipment(NODEID2, "uuid3"));
        service.writeInventory(createEquipment(NODEID2, "uuid4"));
        service.writeInventory(createEquipment(NODEID2, "uuid5"));
        assertEquals(5, getDbEntityEntries(Entity.Inventoryequipment).getTotal());
    }

    private static SearchResult<SearchHit> getDbEntityEntries(Entity entity) {
        return dbRawProvider.doReadAllJsonData(entity.getName());
    }

    private static void clearDbEntity(Entity entity) {
        DeleteByQueryRequest query = new DeleteByQueryRequest(entity.getName());
        query.setQuery(QueryBuilders.matchAllQuery().toJSON());
        try {
            dbRawProvider.deleteByQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TestCRUDforDatabase.trySleep(1000);
    }

    /**
     * @param nodeId
     * @param uuid
     * @return
     */
    private Inventory createEquipment(String nodeId, String uuid) {
        return new InventoryBuilder().setNodeId(nodeId).setParentUuid("").setDescription("desc")
                .setManufacturerName("manu").setUuid(uuid).build();
    }

    /**
     * @param devType
     * @param nodename3
     * @return
     */
    private static NetworkElementConnectionEntity createNeConnection(String nodeId, NetworkElementDeviceType devType) {
        return new NetworkElementConnectionBuilder().setNodeId(nodeId).setHost("host").setPort(1234L)
                .setCoreModelCapability("123").setStatus(ConnectionLogStatus.Connected).setDeviceType(devType)
                .setIsRequired(true).build();
    }

    /**
     * @param nodeId
     * @param objectId
     * @param problem
     * @param severity
     * @param counter
     * @return
     */
    private static FaultlogEntity createFaultLog(String nodeId, String objectId, String problem, SeverityType severity,
            int counter) {
        return new FaultlogBuilder().setNodeId(nodeId).setObjectId(objectId).setProblem(problem).setSeverity(severity)
                .setCounter(counter).setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp())
                .setSourceType(SourceType.Netconf).build();
    }

    /**
     * @param nodeId
     * @param objectId
     * @param attributeName
     * @param newValue
     * @param counter
     * @return
     */
    private static EventlogEntity createEventLog(String nodeId, String objectId, String attributeName, String newValue,
            int counter) {
        return new EventlogBuilder().setNodeId(nodeId).setObjectId(objectId).setAttributeName(attributeName)
                .setNewValue(newValue).setCounter(counter)
                .setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp()).setSourceType(SourceType.Netconf)
                .build();
    }
}
