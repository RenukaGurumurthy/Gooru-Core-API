package org.ednovo.gooru.domain.service;

import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.ClassCollectionSettings;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserClass;

public interface ClassService {

	ActionResponseDTO<UserClass> createClass(UserClass userClass, User user);

	void updateClass(String classUId, UserClass userClass, User user);

	UserClass getClassById(String classUid);

	Map<String, Object> getClass(String classUid, User user);

	Map<String, Object> getClasses(String gooruUid, Boolean emptyCourse, int limit, int offset);

	Map<String, Object> getStudyClasses(String gooruUid, int limit, int offset);

	void deleteUserFromClass(String classUid, String userUid, User user);

	Map<String, Object> getMember(String classUid, int limit, int offset);

	List<Map<String, Object>> getClassesByCourse(String courseGooruOid, int limit, int offset);

	void deleteClass(String classUId, User user);

	void joinClass(String classUid, User apiCaller);

	List<Map<String, Object>> getClassCollectionSettings(String classUid, String unitId, int limit, int offset);

	void updateClassSettings(String classUid, List<ClassCollectionSettings> classCollectionSettings);

	Map<String, Object> getClassCollections(String lessonId, int limit, int offset);

	boolean hasTeachAndStudy(String gooruUid);
	
	void updateMemberCount(String classUid);
	
	List<Map<String, Object>> getClassContent(String classUid, String gooruOid);

}
