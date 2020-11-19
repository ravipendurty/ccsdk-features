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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data;

public enum ComponentName {

    CONNECTIONLOG("connectionlog"), EVENTLOG("eventlog"), FAULTLOG("faultlog"), FAULTCURRENT(
            "faultcurrent"), HISTORICAL_PERFORMANCE_15M("historicalperformance15m"), HISTORICAL_PERFORMANCE_24H(
                    "historicalperformance24h"), INVENTORY(
                            "inventory"), INVENTORYTOPLEVEL("inventorytoplevel"), MAINTENANCE(
                                    "maintenance"), MEDIATOR_SERVER("mediator-server"), REQUIRED_NETWORKELEMENT(
                                            "required-networkelement"), GUICUTTHROUGH("guicutthrough");

    private final String value;

    private ComponentName(String s) {
        this.value = s;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return value;
    }

    public static ComponentName getValueOf(String s) throws Exception {
        s = s.toLowerCase();
        for (ComponentName p : ComponentName.values()) {
            if (p.value.equals(s)) {
                return p;
            }
        }
        throw new Exception("value not found for " + s);
    }
}
