/////////////////////////////////////////////////////////////
// CollectionUtil.java
// gooru-api
// Created by Gooru on 2014
// Copyright (c) 2014 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
package org.ednovo.gooru.application.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.Content;
import org.ednovo.gooru.core.api.model.ContentPermission;
import org.ednovo.gooru.core.api.model.Rating;
import org.ednovo.gooru.core.api.model.StandardFo;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserGroupSupport;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.domain.service.CollectionService;
import org.ednovo.gooru.domain.service.rating.RatingService;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.domain.service.taxonomy.TaxonomyService;
import org.ednovo.gooru.infrastructure.mail.MailHandler;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.UserRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.assessment.AssessmentRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.content.ContentRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyRespository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyStoredProcedure;
import org.ednovo.gooru.security.OperationAuthorizer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CollectionUtil implements ParameterProperties {

	@Autowired
	private MailHandler mailHandler;

	@Autowired
	private RedisService redisService;

	@Autowired
	private ContentRepository contentRepository;

	@Autowired
	private TaxonomyRespository taxonomyRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BaseRepository baseRepository;

	@Autowired
	private AssessmentRepository assessmentRepository;

	@Autowired
	private RatingService ratingService;

	@Autowired
	private TaxonomyStoredProcedure procedureExecutor;

	@Autowired
	private TaxonomyService taxonomyService;

	@Autowired
	private OperationAuthorizer operationAuthorizer;
	
	@Autowired
	private CollectionService collectionService;


	private final static Logger LOGGER = LoggerFactory.getLogger(CollectionUtil.class);

	public List<User> updateNewCollaborator(Content content, List<String> collaboratorsList, User apiCaller, String predicate, String collaboratorOperation) {
		List<User> userList = null;
		if (collaboratorsList != null && collaboratorsList.size() > 0 && !collaboratorsList.isEmpty()) {
			userList = this.getUserRepository().findByIdentities(collaboratorsList);
			if (collaboratorOperation.equals(DELETE)) {
				deleteCollaborators(content, userList, apiCaller, predicate);
			} else {
				addNewCollaborators(content, userList, apiCaller, predicate, false);
			}
		}
		if (userList != null && userList.size() > 0) {
			for (User user : userList) {
				user.setEmailId(user.getIdentities().iterator().next().getExternalId());
			}
			return userList;
		}
		return null;
	}

	public User updateNewCollaborators(Collection collection, List<String> collaboratorsList, User apiCaller, String predicate, String collaboratorOperation) {
		List<User> userList = null;
		if (collaboratorsList != null && collaboratorsList.size() > 0 && !collaboratorsList.isEmpty()) {
			userList = this.getUserRepository().findByIdentities(collaboratorsList);
			if (collaboratorOperation.equals(DELETE)) {
				deleteCollaborators(collection, userList, apiCaller, predicate);
			} else {
				addNewCollaborators(collection, userList, apiCaller, predicate, false);
			}
		}
		if (userList != null && userList.size() > 0) {
			User user = userList.get(0);
			user.setEmailId(user.getIdentities().iterator().next().getExternalId());
			return user;
		}
		return null;
	}

	public void deleteCollaborators(Content content, List<User> userList, User apiCaller, String predicate) {

		Set<ContentPermission> contentPermissions = content.getContentPermissions();
		Set<ContentPermission> removePermissions = new HashSet<ContentPermission>();

		for (ContentPermission contentPermission : contentPermissions) {
			for (User user : userList) {
				if (user.getPartyUid().equalsIgnoreCase(contentPermission.getParty().getPartyUid())) {
					removePermissions.add(contentPermission);
					break;
				}
			}

		}
		if (removePermissions.size() > 0) {
			contentPermissions.removeAll(removePermissions);
			this.getBaseRepository().removeAll(removePermissions);
		}

		this.getBaseRepository().saveAll(contentPermissions);

	}

	public void addNewCollaborators(Content content, List<User> userList, User apiCaller, String predicate, boolean addToShelf) {

		Set<ContentPermission> contentPermissions = content.getContentPermissions();

		if (contentPermissions == null) {
			contentPermissions = new HashSet<ContentPermission>();
		}

		if (userList != null && userList.size() > 0) {
			Date date = new Date();
			for (User user : userList) {
				if (!user.getGooruUId().equals(content.getUser().getGooruUId())) {
					boolean newFlag = true;
					for (ContentPermission contentPermission : contentPermissions) {
						if (contentPermission.getParty().getPartyUid().equals(user.getPartyUid())) {
							newFlag = false;
							break;
						}
					}
					if (newFlag) {
						ContentPermission contentPerm = new ContentPermission();
						contentPerm.setParty(user);
						contentPerm.setContent(content);
						contentPerm.setPermission(EDIT);
						contentPerm.setValidFrom(date);
						contentPermissions.add(contentPerm);
					}
				}
			}
		}
		if (contentPermissions != null && contentPermissions.size() > 0) {
			content.setContentPermissions(contentPermissions);
		}
	}

	public boolean hasSubOrgPermission(String contentOrganizationId) {
		String[] subOrgUids = UserGroupSupport.getUserOrganizationUids();
		if (subOrgUids != null && subOrgUids.length > 0) {
			for (String userSuborganizationId : subOrgUids) {
				if (contentOrganizationId.equals(userSuborganizationId)) {
					return true;
				}
			}
		}
		return false;
	}

	public ContentRepository getContentRepository() {
		return contentRepository;
	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public TaxonomyRespository getTaxonomyRepository() {
		return taxonomyRepository;
	}

	public void setTaxonomyRepository(TaxonomyRespository taxonomyRepository) {
		this.taxonomyRepository = taxonomyRepository;
	}

	public JSONObject getContentSocialData(User user, String contentGooruOid) throws JSONException {
		JSONObject socialDataJSON = new JSONObject();
		Integer contentUserRating = ratingService.getContentRatingForUser(user.getPartyUid(), contentGooruOid);
		Rating rating = ratingService.findByContent(contentGooruOid);
		socialDataJSON.put(CONTENT_USER_RATING, contentUserRating);
		socialDataJSON.put(CONTENT_RATING, new JSONObject(rating).put(VOTE_UP, rating.getVotesUp()));
		return socialDataJSON;
	}

	public JSONObject getContentTaxonomyData(Set<Code> taxonomySet, String contentGooruOid)  {
		JSONObject collectionTaxonomy = new JSONObject();
		try {
		Iterator<Code> iter = taxonomySet.iterator();
		Set<String> subject = new HashSet<String>();
		Set<String> course = new HashSet<String>();
		Set<String> unit = new HashSet<String>();
		Set<String> topic = new HashSet<String>();
		Set<String> lesson = new HashSet<String>();
		List<String> curriculumCode = new ArrayList<String>();
		List<String> curriculumDesc = new ArrayList<String>();
		List<String> curriculumName = new ArrayList<String>();
		while (iter.hasNext()) {
			Code code = iter.next();
			try {
				this.getProcedureExecutor().setCode(code);
				Map codeMap = this.getProcedureExecutor().execute();

				String codeLabel = (String) codeMap.get(CODE_LABEL);
				String[] taxonomy = codeLabel.split("\\$\\@");

				int length = taxonomy.length;
				if (length > 1) {
					subject.add(taxonomy[length - 2]);
				}
				if (length > 2 && code.getRootNodeId() != null && code.getRootNodeId().toString().equalsIgnoreCase(Code.GOORU_TAXONOMY_CODE_ID)) {
					course.add(taxonomy[length - 3]);
				}
				if (length > 3) {
					unit.add(taxonomy[length - 4]);
				}
				if (length > 4) {
					topic.add(taxonomy[length - 5]);
				}
				if (length > 5) {
					lesson.add(taxonomy[length - 6]);
				}
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
		}

		if (taxonomySet != null) {
			for (Code code : taxonomySet) {
				if (code.getRootNodeId() != null && UserGroupSupport.getTaxonomyPreference() != null && UserGroupSupport.getTaxonomyPreference().contains(code.getRootNodeId().toString())) {
					String codeOrDisplayCode = "";
					if (code.getCommonCoreDotNotation() != null && !code.getCommonCoreDotNotation().equals("")) {
						codeOrDisplayCode = code.getCommonCoreDotNotation().replace(".--", " ");
					} else if (code.getdisplayCode() != null && !code.getdisplayCode().equals("")) {
						codeOrDisplayCode = code.getdisplayCode().replace(".--", " ");
					}
					if (!curriculumCode.contains(codeOrDisplayCode)) {
						// string replace has been added to fix the ".--" issue
						// code
						// in USCCM (US Common Core Math - Curriculum)
						curriculumCode.add(codeOrDisplayCode);
						if (code.getLabel() != null && !code.getLabel().equals("")) {
							curriculumDesc.add(code.getLabel());
						} else {
							curriculumDesc.add(BLANK + codeOrDisplayCode);
						}
						Code rootNode = this.getTaxonomyRepository().findCodeByCodeId(code.getRootNodeId());
						if (rootNode == null) {
							LOGGER.error("FIXME: Taxonomy root was found null for code id" + code.getRootNodeId());
							continue;
						}
						String curriculumLabel = this.getTaxonomyRepository().findRootLevelTaxonomy(rootNode);
						curriculumName.add(curriculumLabel);
					}
				}
			}
		}
		JSONObject curriculumTaxonomy = new JSONObject();
		curriculumTaxonomy.put(CURRICULUM_CODE, curriculumCode).put(CURRICULUM_DESC, curriculumDesc).put(CURRICULUM_NAME, curriculumName);
		collectionTaxonomy.put(SUBJECT, subject);
		collectionTaxonomy.put(COURSE, course);
		collectionTaxonomy.put(TOPIC, topic);
		collectionTaxonomy.put(UNIT, unit);
		collectionTaxonomy.put(LESSON, lesson);
		collectionTaxonomy.put(CURRICULUM, curriculumTaxonomy);
		} catch (Exception e) { 
			LOGGER.error("failed to fetch ");
		}
		return collectionTaxonomy;
	}

	// Form the custom fieldname and value map
	public Map<String, String> getCustomFieldNameAndValueAsMap(HttpServletRequest request) {
		Map<String, String> customFieldsAndValues = new HashMap<String, String>();
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			if ((paramName.startsWith("cf.")) && (request.getParameter(paramName) != null)) {
				customFieldsAndValues.put(paramName.replace("cf.", ""), request.getParameter(paramName));

			}
		}
		return customFieldsAndValues;
	}

	public Set<StandardFo> getContentStandards(Set<Code> taxonomySet, String contentGooruOid) {
		Set<StandardFo> standards = new HashSet<StandardFo>();
		if (taxonomySet != null) {
			for (Code code : taxonomySet) {
				if (code.getRootNodeId() != null && UserGroupSupport.getTaxonomyPreference() != null && UserGroupSupport.getTaxonomyPreference().contains(code.getRootNodeId().toString())) {
					StandardFo standardFo = new StandardFo();
					if (code.getLabel() != null && !code.getLabel().equals("")) {
						standardFo.setDescription(code.getLabel());
					}
					if (code.getCommonCoreDotNotation() != null && !code.getCommonCoreDotNotation().equals("")) {
						standardFo.setCode(code.getCommonCoreDotNotation().replace(".--", " "));
					} else if (code.getdisplayCode() != null && !code.getdisplayCode().equals("")) {
						standardFo.setCode(code.getdisplayCode().replace(".--", " "));
					}
					standards.add(standardFo);
				}
			}
		}
		return standards;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public BaseRepository getBaseRepository() {
		return baseRepository;
	}

	public void setBaseRepository(BaseRepository baseRepository) {
		this.baseRepository = baseRepository;
	}

	public AssessmentRepository getAssessmentRepository() {
		return assessmentRepository;
	}

	public void setAssessmentRepository(AssessmentRepository assessmentRepository) {
		this.assessmentRepository = assessmentRepository;
	}

	public void deleteCollectionFromCache(String collectionId, String prefix) {
		// Remove the collection from cache
		final String cacheKey = prefix + "-" + collectionId;
		getRedisService().deleteKey(cacheKey);
	}

	public TaxonomyStoredProcedure getProcedureExecutor() {
		return procedureExecutor;
	}

	public void setProcedureExecutor(TaxonomyStoredProcedure procedureExecutor) {
		this.procedureExecutor = procedureExecutor;
	}

	public OperationAuthorizer getOperationAuthorizer() {
		return operationAuthorizer;
	}

	public void setOperationAuthorizer(OperationAuthorizer operationAuthorizer) {
		this.operationAuthorizer = operationAuthorizer;
	}

	public  void clearResourceCache(String gooruOid) { 
		List<Collection> collections = this.getCollectionService().getResourceMoreInfo(gooruOid);
		for (Collection collection : collections) { 
			this.redisService.bulkKeyDelete("v2-organize-data-" + collection.getUser().getPartyUid() + "*");
		}
	}
	
	public void deleteFromCache(String key) { 
		this.redisService.bulkKeyDelete(key);
	}

	public RedisService getRedisService() {
		return redisService;
	}

	public CollectionService getCollectionService() {
		return collectionService;
	}
	
	
}
