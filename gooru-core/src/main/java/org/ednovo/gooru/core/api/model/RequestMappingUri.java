package org.ednovo.gooru.core.api.model;

public class RequestMappingUri {

	public static final String TAXONOMY_COURSE = "/v1/taxonomycourse";

	public static final String SUBJECT = "/v1/subject";

	public static final String SUBDOMAIN = "/v1/sub-domain";

	public static final String DOMAIN = "/v1/domain";

	public static final String V3_CLASS = "/v3/class";

	public static final String SEPARATOR = "/";

	public static final String META = "/v1/meta";

	public static final String COURSE = "/v1/course";

	public static final String UNIT = "/v1/course/{courseId}/unit";

	public static final String LESSON = "/v1/course/{courseId}/unit/{unitId}/lesson";

	public static final String ID = "/{id}";

	public static final String CLASS_MEMBER = "/{id}/member";

	public static final String LESSON_COLLECTION = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection";

	public static final String LESSON_COLLECTION_ID = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{id}";

	public static final String TARGET_LESSON = "/v1/targetCourse/{courseId}/targetUnit/{unitId}/targetLesson/{lessonId}/sourceCollection/{id}";

	public static final String CREATE_QUESTION = "/id}/question";

	public static final String CREATE_RESOURCE = "/{id}/resource";

	public static final String UPDATE_RESOURCE = "/collectionId}/resource/{id}";

	public static final String UPDATE_QUESTION = "/collectionId}/question/{id}";

	public static final String USER_COURSES = "/{id}/course";

	public static final String DELETE_USER_FROM_CLASS = "/{id}/member/{userUid}";

	public static final String LESSON_COLLECTION_ITEM = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{id}/item";

	public static final String LESSON_COLLECTION_QUESTION = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{id}/question";

	public static final String LESSON_COLLECTION_RESOURCE = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{id}/resource";

	public static final String LESSON_COLLECTION_RESOURCE_ID = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{collectionId}/resource/{id}";

	public static final String LESSON_COLLECTION_QUESTION_ID = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{collectionId}/question/{id}";

	public static final String COURSES_CLASS = "/{id}/classes";

	public static final String CLASS_TEACH = "/teach";

	public static final String CLASS_STUDY = "/study";

	public static final String LESSON_COLLECTION_ITEM_ID = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{collectionId}/item/{id}";

	public static final String ITEM_ID = "/v3/collection/{id}/item";

	public static final String TAXONOMY_COURSE_BY_SUBJECT = "/{id}/taxonomycourse";

	public static final String DOMAIN_BY_TAXONOMY_COURSE = "/{id}/domain";
	
	public static final String COLLECTION_QUESTION_ID = "/v3/collection/{collectionId}/question/{id}";

	public static final String COLLECTION_RESOURCE_ID = "/v3/collection/{collectionId}/resource/{id}";

	public static final String COLLECTION_ITEM_ID = "/v3/collection/{collectionId}/item/{id}";

	public static final String COLLECTION_QUESTION = "/v3/collection/{id}/question";

	public static final String COLLECTION_RESOURCE = "/v3/collection/{id}/resource";
	
	public static final String SUBDOMAIN_STANDARDS = "/{id}/standards";
		
	public static final String CLASS_UNIT_COLLECTION_SETTINGS = "/{id}/setting";
	
	public static final String V3_COLLECTION_ID = "/v3/collection/{id}";
	
	public static final String V3_COLLECTION = "/v3/collection";
	
	public static final String V3_SOURCE_COLLECTION_ID = "/v3/sourceCollection/{id}";
	
	public static final String CLASS_UNIT_LESSON_COLLECTION = "/{id}/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection";
	
	public static final String CLASS_UNIT = "/{id}/course/{courseId}/unit";
	
	public static final String CLASS_TEACH_STUDY = "/has-teach-study";
	
	public static final String V2_JOB = "/v2/job";
	
	public static final String V1_CROP = "/v1/crop";
	
	public static final String CLASS_UNITS = "/{id}/course/{courseId}/unit";

	public static final String CLASS_LESSONS = "/{id}/course/{courseId}/unit/{unitId}/lesson";
	
	public static final String CLASS_COLLECTIONS = "/{id}/course/{courseId}/unit/{unitId}/lesson/{lessonId}";
	
	public static final String V3_LIBRARY = "/v3/library";
	
	public static final String V3_LIBRARY_COURSE = "/{id}/course";
	
	public static final String V3_LIBRARY_COURSE_UNIT = "/{id}/course/{courseId}/unit";
	
	public static final String V3_LIBRARY_COURSE_LESSON = "/{id}/course/{courseId}/unit/{unitId}/lesson";
	
	public static final String V3_LIBRARY_COURSE_LESSON_COLLECTION = "/{id}/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection";

	public static final String V3_LIBRARY_SUBJECT = "/{id}/subject";
	
	public static final String V3_LIBRARY_SUBJECT_COURSE = "/{id}/subject/{subjectId}/course";
	
	public static final String V3_LIBRARY_SUBJECT_COURSE_UNIT = "/{id}/subject/{subjectId}/course/{courseId}/unit";
	
	public static final String V3_LIBRARY_SUBJECT_COURSE_UNIT_LESSON = "/{id}/subject/{subjectId}/course/{courseId}/unit/{unitId}/lesson";
	
	public static final String V3_LIBRARY_SUBJECT_COURSE_UNIT_LESSON_COLLECTION = "/{id}/subject/{subjectId}/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection";
	
	public static final String COURSE_COLLECTION_CLASSES = "/v1/course/{courseId}/unit/{unitId}/lesson/{lessonId}/collection/{id}/classes";
	
	public static final String CLASS_UNIT_COLLECTION_SETTING = "/setting";
}
