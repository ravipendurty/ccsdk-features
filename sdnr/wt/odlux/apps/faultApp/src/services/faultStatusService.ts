/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
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
import { requestRest } from "../../../../framework/src/services/restService";
import { Result, PostResponse } from "../../../../framework/src/models/elasticSearch";
import { FaultType, Faults, DeletedStuckAlarms } from "../models/fault";


export const getFaultStateFromDatabase = async (): Promise<FaultType | null> => {
  const path = 'restconf/operations/data-provider:read-status';
  const result = await requestRest<Result<Faults>>(path, { method: "POST" });

  let faultType: FaultType = {
    Critical: 0,
    Major: 0,
    Minor: 0,
    Warning: 0
  }
  let faults: Faults[] | null = null;

  if (result && result.output && result.output.data) {
    faults = result.output.data;
    faultType = {
      Critical: faults[0].faults.criticals,
      Major: faults[0].faults.majors,
      Minor: faults[0].faults.minors,
      Warning: faults[0].faults.warnings
    }
  }

  return faultType;
}

export const clearStuckAlarms = async (nodeNames: string[]) => {
  const path = 'restconf/operations/devicemanager:clear-current-fault-by-nodename'
  const result = await requestRest<DeletedStuckAlarms>(path, { method: 'Post', body: JSON.stringify({ input: { nodenames: nodeNames } }) })
  return result;

}