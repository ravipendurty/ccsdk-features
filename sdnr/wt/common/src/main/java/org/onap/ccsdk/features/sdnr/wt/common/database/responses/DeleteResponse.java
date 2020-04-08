/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.database.responses;

import org.elasticsearch.client.Response;
import org.json.JSONObject;

public class DeleteResponse extends BaseResponse {

	private boolean isDeleted;

	public DeleteResponse(Response response) {
		super(response);
		if (this.isResponseSucceeded()) {

			JSONObject o = this.getJson(response);
			if (o != null) {
				this.isDeleted = "deleted".equals(o.getString("result"));
			}
			else {
				this.isDeleted=false;
			}
		}
		else {
			this.isDeleted=false;
		}

	}

	public boolean isDeleted() {
		return this.isDeleted;
	}

}
