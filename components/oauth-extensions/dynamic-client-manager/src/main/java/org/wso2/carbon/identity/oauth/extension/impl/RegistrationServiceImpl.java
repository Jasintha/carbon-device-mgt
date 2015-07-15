/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.identity.oauth.extension.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth.extension.*;
import org.wso2.carbon.identity.oauth.extension.profile.RegistrationProfile;
import org.wso2.carbon.identity.oauth.extension.profile.UnregistrationProfile;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistrationServiceImpl implements RegistrationService {

    private static final Log log = LogFactory.getLog(RegistrationServiceImpl.class);

    @POST
    @Override
    public Response register(RegistrationProfile profile) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            OAuthApplicationInfo info = DynamicClientRegistrationUtil.registerApplication(profile);
            return Response.status(Response.Status.ACCEPTED).entity(info.toString()).build();
        } catch (APIManagementException e) {
            String msg = "Error occurred while registering client '" + profile.getClientName() + "'";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new FaultResponse(ErrorCode.INVALID_CLIENT_METADATA, msg)).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @DELETE
    @Override
    public Response unregister(UnregistrationProfile profile) {
        String applicationName = profile.getApplicationName();
        String consumerKey = profile.getConsumerKey();
        String userId = profile.getUserId();
        try {
            DynamicClientRegistrationUtil.unregisterApplication(userId, applicationName, consumerKey);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (APIManagementException e) {
            String msg = "Error occurred while un-registering client '" + applicationName + "'";
            log.error(msg, e);
            return Response.serverError().entity(new FaultResponse(ErrorCode.INVALID_CLIENT_METADATA, msg)).build();
        }
    }

}