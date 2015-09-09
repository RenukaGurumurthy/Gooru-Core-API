/////////////////////////////////////////////////////////////
//AccountRestV2Controller.java
//rest-v2-app
// Created by Gooru on 2014
// Copyright (c) 2014 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person      obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so,  subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY  KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE    WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR  PURPOSE     AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR  COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
package org.ednovo.gooru.controllers.v2.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.Identity;
import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserAccountType;
import org.ednovo.gooru.core.api.model.UserRole;
import org.ednovo.gooru.core.api.model.UserToken;
import org.ednovo.gooru.core.application.util.RequestUtil;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.authentication.AccountService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.UserRepository;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = { "/v2/account" })
public class AccountRestV2Controller extends BaseController implements ConstantProperties, ParameterProperties {

	@Autowired
	private AccountService accountService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	@Resource(name = "serverConstants")
	private Properties serverConstants;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_USER_SIGNIN })
	@RequestMapping(method = { RequestMethod.POST }, value = "/login")
	public ModelAndView login(@RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final JSONObject json = requestData(data);
		ActionResponseDTO<UserToken> responseDTO = null;
		responseDTO = this.getAccountService().logIn(getValue(USER_NAME, json), getValue(PASSWORD, json), false, request);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			SessionContextSupport.putLogParameter(EVENT_NAME, USER_LOGIN);
		}
		String[] includes = (String[]) ArrayUtils.addAll(USER_INCLUDES, ERROR_INCLUDE);

		if (getValue(RETURN_URL, json) != null) {
			response.sendRedirect(getValue(RETURN_URL, json));
			return null;
		} else {
			return toModelAndView(serialize(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, includes));
		}

	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_USER_SIGNIN })
	@RequestMapping(method = { RequestMethod.PUT }, value = "/switch-session")
	public ModelAndView swithSession(@RequestParam(value = SESSIONTOKEN, required = true) final String sessionToken, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		ActionResponseDTO<UserToken> responseDTO = null;
		responseDTO = this.getAccountService().switchSession(sessionToken);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			SessionContextSupport.putLogParameter(EVENT_NAME, USER_SIGN_IN);
			SessionContextSupport.putLogParameter(CURRENT_SESSION_TOKEN, responseDTO.getModel().getToken());
			SessionContextSupport.putLogParameter(GOORU_UID, responseDTO.getModel().getUser().getPartyUid());
		}
		String[] includes = (String[]) ArrayUtils.addAll(USER_INCLUDES, ERROR_INCLUDE);
		return toModelAndView(serialize(responseDTO.getModel(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, includes));
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_USER_SIGNOUT })
	@RequestMapping(method = RequestMethod.POST, value = "/logout")
	public void logout(final HttpServletRequest request, final HttpServletResponse response, @RequestParam(value = SESSIONTOKEN, required = false) final String sessionToken) throws Exception {
		getAccountService().logOut(sessionToken);
		request.getSession().invalidate();
		RequestUtil.deleteCookie(request, response, GOORU_SESSION_TOKEN);
		RequestUtil.deleteCookie(request, response, COOKIE_KEY_SERVICE_VERSION);

	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_USER_SIGNIN })
	@RequestMapping(method = { RequestMethod.POST }, value = "/loginas/{id}")
	public ModelAndView loginAs(@PathVariable(value = ID) final String gooruUid, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final UserToken userToken = this.getAccountService().loginAs(gooruUid, request);
		if (userToken == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
		}
		String[] includes = (String[]) ArrayUtils.addAll(USER_INCLUDES, ERROR_INCLUDE);
		return toModelAndView(serialize(userToken, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, includes));

	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_USER_SIGNIN })
	@RequestMapping(method = RequestMethod.POST, value = "/authenticate")
	public ModelAndView authenticateUser(@RequestBody final String data, @RequestParam(value = API_KEY, required = false) final String apiKey, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final JSONObject json = requestData(data);
		SessionContextSupport.putLogParameter(EVENT_NAME, USER_AUTHENTICATE);
		final User user = this.getAccountService().userAuthentication(buildUserFromInputParameters(data), getValue(SECERT_KEY, json), getValue(API_KEY, json) == null ? apiKey : getValue(API_KEY, json),
				getValue(SOURCE, json) != null ? getValue(SOURCE, json) : UserAccountType.accountCreatedType.GOOGLE_APP.getType(),
						getValue(USER_PROFILE_CATEGORY, json) != null ? getValue(USER_PROFILE_CATEGORY, json) : UserRole.UserRoleType.OTHER.getType(),
						request);
		if (user.getIdentities() != null) {
			final Identity identity = user.getIdentities().iterator().next();
			if (identity.getActive() == 0) {
				final Map<String, Object> redirectObj = new HashMap<String, Object>();
				redirectObj.put(ACTIVE, 0);
				return toModelAndView(serialize(redirectObj, JSON));
			}
		}
		return toModelAndViewWithIoFilter(user, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, USER_INCLUDES);
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public AccountService getAccountService() {
		return accountService;
	}

	private User buildUserFromInputParameters(final String data) {

		return JsonDeserializer.deserialize(data, User.class);
	}

	public Properties getServerConstants() {
		return serverConstants;
	}

}
