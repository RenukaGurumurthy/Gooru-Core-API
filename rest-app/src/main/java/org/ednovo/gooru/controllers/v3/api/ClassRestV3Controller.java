package org.ednovo.gooru.controllers.v3.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.ClassCollectionSettings;
import org.ednovo.gooru.core.api.model.RequestMappingUri;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserClass;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.ClassService;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;

@RequestMapping(value = { RequestMappingUri.V3_CLASS })
@Controller
public class ClassRestV3Controller extends BaseController implements ConstantProperties, ParameterProperties {

	@Autowired
	private ClassService classService;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ADD })
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView createClass(@RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		final ActionResponseDTO<UserClass> responseDTO = this.getClassService().createClass(buildClass(data), user);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_CREATED);
			responseDTO.getModel().setUri(generateUri(request.getRequestURI(), responseDTO.getModel().getPartyUid()));
		}
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_UPDATE })
	@RequestMapping(value = RequestMappingUri.ID, method = { RequestMethod.PUT })
	public void updateClass(@PathVariable(value = ID) final String classUId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getClassService().updateClass(classUId, this.buildClass(data), user);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.ID, method = RequestMethod.GET)
	public ModelAndView getClass(@PathVariable(value = ID) final String classUId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		return toModelAndViewWithIoFilter(this.getClassService().getClass(classUId, user), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, CLASS_INCLUDES);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_TEACH_STUDY, method = RequestMethod.GET)
	public ModelAndView hasClassTeachAndStudy(final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		return toModelAndView(this.getClassService().hasTeachAndStudy(user.getPartyUid()));
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getClasses(@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") final int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") final int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getClassService().getClasses(null, null, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, CLASS_INCLUDES);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_TEACH, method = RequestMethod.GET)
	public ModelAndView getTeachClasses(@RequestParam(value = CLASS_EMPTY_COURSE_FILTER, required = false, defaultValue = FALSE) boolean courseFilter, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		return toModelAndViewWithIoFilter(this.getClassService().getClasses(user.getPartyUid(), courseFilter, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, CLASS_INCLUDES);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_STUDY, method = RequestMethod.GET)
	public ModelAndView getStudyClasses(@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		return toModelAndViewWithIoFilter(this.getClassService().getStudyClasses(user.getPartyUid(), limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, CLASS_INCLUDES);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_DELETE })
	@RequestMapping(value = RequestMappingUri.ID, method = RequestMethod.DELETE)
	public void deleteClass(@PathVariable(value = ID) final String classUId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getClassService().deleteClass(classUId, user);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_ADD })
	@RequestMapping(value = RequestMappingUri.CLASS_MEMBER, method = RequestMethod.POST)
	public void joinClass(@PathVariable(value = ID) final String classUId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getClassService().joinClass(classUId, user);
		this.getClassService().updateMemberCount(classUId);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_MEMBER, method = RequestMethod.GET)
	public ModelAndView getClassMemberList(@PathVariable(ID) final String classUid, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request,
			final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getClassService().getMember(classUid, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE, true, CLASS_FIELDS);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_DELETE })
	@RequestMapping(value = RequestMappingUri.DELETE_USER_FROM_CLASS, method = RequestMethod.DELETE)
	public void removeFromClass(@PathVariable(value = ID) final String classUid, @PathVariable(value = USER_UID) final String userUid, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getClassService().deleteUserFromClass(classUid, userUid, user);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_UPDATE })
	@RequestMapping(value = RequestMappingUri.CLASS_UNIT_COLLECTION_SETTINGS, method = { RequestMethod.PUT })
	public void updateCollectionSettings(@PathVariable(ID) final String classUid, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) {
		this.getClassService().updateClassSettings(classUid, this.buildClassCollectionSettings(data));
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_UNITS, method = RequestMethod.GET)
	public ModelAndView getClassUnits(@PathVariable(ID) final String classUid, @PathVariable(COURSE_ID) final String courseId,
			final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndView(this.getClassService().getClassContent(classUid, courseId, COURSE), RESPONSE_FORMAT_JSON);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_LESSONS, method = RequestMethod.GET)
	public ModelAndView getClassLessons(@PathVariable(ID) final String classUid, @PathVariable(COURSE_ID) final String courseId , @PathVariable(UNIT_ID) final String unitId, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndView(this.getClassService().getClassContent(classUid, unitId, UNIT), RESPONSE_FORMAT_JSON);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_COLLECTIONS, method = RequestMethod.GET)
	public ModelAndView getClassCollections(@PathVariable(ID) final String classUid, @PathVariable(COURSE_ID) final String courseId , @PathVariable(UNIT_ID) final String unitId,  @PathVariable(LESSON_ID) final String lessonId, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndView(this.getClassService().getClassContent(classUid, lessonId, LESSON), RESPONSE_FORMAT_JSON);
	}
	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_CLASSPAGE_READ })
	@RequestMapping(value = RequestMappingUri.CLASS_UNIT_LESSON_COLLECTION, method = RequestMethod.GET)
	public ModelAndView getClassCollection(@PathVariable(ID) final String classUid, @PathVariable(COURSE_ID) final String courseId, @PathVariable(UNIT_ID) final String unitId, @PathVariable(LESSON_ID) final String lessonId,
			@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getClassService().getClassCollections(lessonId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE, true, "*");
	}
	
	private UserClass buildClass(final String data) {
		return JsonDeserializer.deserialize(data, UserClass.class);
	}

	private List<ClassCollectionSettings> buildClassCollectionSettings(final String data) {
		return JsonDeserializer.deserialize(data, new TypeReference<List<ClassCollectionSettings>>() {
		});
	}

	public ClassService getClassService() {
		return classService;
	}
}
