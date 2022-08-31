package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.InternalDataModelSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14DomWireInterfaceNotificationListener implements DOMNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(Onf14DomWireInterfaceNotificationListener.class);

    private final NetconfDomAccessor netconfDomAccessor;
    private final DeviceManagerServiceProvider serviceProvider;

    public Onf14DomWireInterfaceNotificationListener(NetconfDomAccessor netconfDomAccessor,
            DeviceManagerServiceProvider serviceProvider) {
        this.netconfDomAccessor = netconfDomAccessor;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void onNotification(@NonNull DOMNotification domNotification) {
        log.debug("Got event of type :: {}", domNotification.getType());
        if (domNotification.getType()
                .equals(Absolute.of(Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION))) {
            onObjectCreationNotification(domNotification);
        } else if (domNotification.getType()
                .equals(Absolute.of(Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION))) {
            onAttributeValueChangedNotification(domNotification);
        } else if (domNotification.getType()
                .equals(Absolute.of(Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION))) {
            onProblemNotification(domNotification);
        } else if (domNotification.getType()
                .equals(Absolute.of(Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION))) {
            onObjectDeletionNotification(domNotification);
        }
    }

    public void onObjectDeletionNotification(DOMNotification notification) {

        ContainerNode cn = notification.getBody();
        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setNodeId(netconfDomAccessor.getNodeId().getValue()).setAttributeName("")
                .setCounter(Integer.parseInt(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION_COUNTER)))
                .setNewValue("deleted")
                .setObjectId(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION_OBJECT_ID_REF))
                .setSourceType(SourceType.Netconf).setTimestamp(new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION_TIMESTAMP)));
        serviceProvider.getDataProvider().writeEventLog(eventlogBuilder.build());
        serviceProvider.getWebsocketService().sendNotification(notification, netconfDomAccessor.getNodeId(),
                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION,
                new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION_TIMESTAMP)));

        log.debug("onObjectDeletionNotification log entry written");
    }

    public void onProblemNotification(DOMNotification notification) {

        ContainerNode cn = notification.getBody();
        FaultlogEntity faultAlarm =
                new FaultlogBuilder()
                        .setObjectId(Onf14DMDOMUtility.getLeafValue(cn,
                                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_OBJECT_ID_REF))
                        .setProblem(Onf14DMDOMUtility.getLeafValue(cn,
                                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_PROBLEM))
                        .setTimestamp(new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP)))
                        .setNodeId(this.netconfDomAccessor.getNodeId().getValue()).setSourceType(SourceType.Netconf)
                        .setSeverity(InternalDataModelSeverity.mapSeverity(Onf14DMDOMUtility.getLeafValue(cn,
                                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_SEVERITY)))
                        .setCounter(Integer.parseInt(Onf14DMDOMUtility.getLeafValue(cn,
                                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_COUNTER)))
                        .build();
        serviceProvider.getFaultService().faultNotification(faultAlarm);
        serviceProvider.getWebsocketService().sendNotification(notification, netconfDomAccessor.getNodeId(),
                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION,
                new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP)));
        log.debug("onProblemNotification log entry written");
    }

    public void onAttributeValueChangedNotification(DOMNotification notification) {

        ContainerNode cn = notification.getBody();
        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setNodeId(netconfDomAccessor.getNodeId().getValue())
                .setAttributeName(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION_ATTRIBUTE_NAME))
                .setCounter(Integer.parseInt(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_COUNTER)))
                .setNewValue(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION_NEW_VALUE))
                .setObjectId(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_OBJECT_ID_REF))
                .setSourceType(SourceType.Netconf).setTimestamp(new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP)));
        serviceProvider.getDataProvider().writeEventLog(eventlogBuilder.build());
        serviceProvider.getWebsocketService().sendNotification(notification, netconfDomAccessor.getNodeId(),
                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION,
                new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP)));

        log.debug("onAttributeValueChangedNotification log entry written");
    }

    public void onObjectCreationNotification(DOMNotification notification) {

        ContainerNode cn = notification.getBody();
        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setNodeId(netconfDomAccessor.getNodeId().getValue())
                .setAttributeName(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION_OBJECT_TYPE))
                .setCounter(Integer.parseInt(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_COUNTER)))
                .setNewValue("created")
                .setObjectId(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_OBJECT_ID_REF))

                .setSourceType(SourceType.Netconf).setTimestamp(new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP)));
        serviceProvider.getDataProvider().writeEventLog(eventlogBuilder.build());
        serviceProvider.getWebsocketService().sendNotification(notification, netconfDomAccessor.getNodeId(),
                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION,
                new DateAndTime(Onf14DMDOMUtility.getLeafValue(cn,
                        Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP)));

        log.debug("onObjectCreationNotification log entry written");
    }
}
