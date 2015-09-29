/////////////////////////////////////////////////////////////
// TaxonomyServiceImpl.java
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
package org.ednovo.gooru.domain.service.taxonomy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.ednovo.gooru.application.util.TaxonomyUtil;
import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.CodeOrganizationAssoc;
import org.ednovo.gooru.core.api.model.CodeType;
import org.ednovo.gooru.core.api.model.Organization;
import org.ednovo.gooru.core.api.model.TaxonomyDTO;
import org.ednovo.gooru.core.application.util.formatter.CodeFo;
import org.ednovo.gooru.core.application.util.formatter.FilterSubjectFo;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.exception.BadRequestException;
import org.ednovo.gooru.domain.cassandra.service.TaxonomyCassandraService;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.infrastructure.messenger.IndexHandler;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.gooru.infrastructure.persistence.hibernate.UserRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.party.OrganizationRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyRespository;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("taxonomyService")
public class TaxonomyServiceImpl implements TaxonomyService, ParameterProperties, ConstantProperties {

	private Logger logger = Logger.getLogger(TaxonomyServiceImpl.class);

	private static final String ELEMETRY = "K,1,2,3,4";

	private static final String MIDDLE = "5,6,7,8";

	private static final String HIGH = "9,10,11,12";

	private static final String TWENTY_FIRST_CENTURY_SKILLS_MODEL = "21_CS_M";

	private static final String TWENTY_FIRST_CENTURY_SKILLS_KEY = "21_CS_K";

	private static final String KEY = "key";

	private static final String MODEL = "model";
	@Autowired
	private TaxonomyRespository taxonomyRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private IndexProcessor indexProcessor;

	@Autowired
	private TaxonomyCassandraService taxonomyCassandraService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RedisService redisService;

	@Autowired
	private IndexHandler indexHandler;

	@Override
	public Code findCodeByTaxCode(String taxonomyCode) {
		return taxonomyRepository.findCodeByTaxCode(taxonomyCode);
	}

	@Override
	public List<Code> findRootTaxonomies(Short depth) {
		return taxonomyRepository.findRootTaxonomies(depth, null);
	}

	@Override
	public int findMaxDepthInTaxonomy(Code code) {
		return taxonomyRepository.findMaxDepthInTaxonomy(code, null);
	}

	@Override
	public CodeType findTaxonomyTypeBydepth(Code code, Short depth) {
		return taxonomyRepository.findTaxonomyTypeBydepth(code, depth);
	}

	@Override
	public List<CodeType> findTaxonomyLevels(Code root) {
		return taxonomyRepository.findTaxonomyLevels(root);
	}

	@Override
	public List<CodeType> findAllTaxonomyLevels() {
		return taxonomyRepository.findAllTaxonomyLevels();
	}

	@Override
	public List<Code> findChildTaxonomyCode(Integer codeId) {
		return taxonomyRepository.findChildTaxonomyCode(codeId);
	}

	@Override
	public List<Code> findChildTaxonomyCodeByOrder(Integer codeId, String order) {
		return taxonomyRepository.findChildTaxonomyCodeByOrder(codeId, order);
	}

	@Override
	public List<Code> findCodeByType(Integer taxonomyLevel) {
		return taxonomyRepository.findCodeByType(taxonomyLevel);
	}

	@Override
	public List<Code> findParentTaxonomyCodes(Integer codeId, List<Code> codeList) {
		return taxonomyRepository.findParentTaxonomyCodes(codeId, codeList);
	}

	@Override
	public List<Code> findSiblingTaxonomy(Code code) {
		return taxonomyRepository.findSiblingTaxonomy(code);
	}

	@Override
	public List<Code> findTaxonomyMappings(List<Code> codeList) {
		return taxonomyRepository.findTaxonomyMappings(codeList, false);
	}

	@Override
	public String findRootLevelTaxonomy(Code code) {
		return taxonomyRepository.findRootLevelTaxonomy(code);
	}

	@Override
	public void updateOrder(Code code) {
		taxonomyRepository.updateOrder(code);
	}

	@Override
	public String makeTree(Code rootCode) {
		return taxonomyRepository.makeTree(rootCode);
	}

	@Override
	public void writeToDisk(Code cde) throws Exception {
		taxonomyRepository.writeToDisk(cde);
	}

	@Override
	public String findTaxonomyTree(String taxonomyCode, String format) throws Exception {
		try{
		return taxonomyRepository.findTaxonomyTree(taxonomyCode, format);
		}
		catch(DocumentException e){
			throw new BadRequestException(FILE_NOT_FOUND, "400");
		}
	}

	@Override
	public void updateTaxonomyAssociation(Code taxonomy, List<Code> codes) {
		taxonomyRepository.updateTaxonomyAssociation(taxonomy, codes);
	}

	@Override
	public void deleteTaxonomyMapping(Code code, List<Code> codes) {
		taxonomyRepository.deleteTaxonomyMapping(code, codes);
	}

	@Override
	public Code findByLabel(String label) {
		return taxonomyRepository.findByLabel(label);
	}

	@Override
	public Code findByParent(String label, Integer parentId) {
		return taxonomyRepository.findByParent(label, parentId);
	}

	@Override
	public List<Code> findChildTaxonomyCodeByDepth(Integer codeId, Integer depth) {
		return taxonomyRepository.findChildTaxonomyCodeByDepth(codeId, depth);
	}

	@Override
	public List<Code> findAll() {
		return taxonomyRepository.findAll();
	}

	@Override
	public List<Code> findAllByRoot(Integer codeId) {
		return taxonomyRepository.findAllByRoot(codeId);
	}

	@Override
	public List<Code> getCodesOfConent(Long contentId) {
		return taxonomyRepository.getCodesOfConent(contentId);
	}

	@Override
	public Code findCodeByCodeId(Integer codeId) {
		return taxonomyRepository.findCodeByCodeId(codeId);
	}

	@Override
	public Code findCodeByCodeUId(String codeUId) {
		return taxonomyRepository.findCodeByCodeUId(codeUId);
	}

	@Override
	public TaxonomyDTO createTaxonomy(String parentCode, String label, String code, String order, String rootNodeId, String codeRoot, String displayCode) throws Exception {

		Code parentTaxonomy = (Code) taxonomyRepository.get(Code.class, new Integer(parentCode));

		List<Code> siblings = this.findChildTaxonomyCodeByOrder(parentTaxonomy.getCodeId(), order);
		if (siblings != null && siblings.size() != 0) {
			for (Code sibling : siblings) {
				sibling.setDisplayOrder(sibling.getDisplayOrder() + 1);
				this.updateOrder(sibling);
			}

		}

		Code rootNode = this.findCodeByTaxCode(codeRoot);

		int maxdepth = this.findMaxDepthInTaxonomy(rootNode);

		if (Short.valueOf((short) (parentTaxonomy.getDepth() + 1)) > maxdepth) {
			throw new Exception("Can Not add at this level");
		}

		CodeType newcodeType = this.findTaxonomyTypeBydepth(rootNode, Short.valueOf((short) (parentTaxonomy.getDepth() + 1)));

		Code newCode = new Code();
		newCode.setCode(code);
		newCode.setCodeType(newcodeType);
		newCode.setDepth(Short.valueOf((short) (parentTaxonomy.getDepth() + 1)));
		newCode.setDescription("");
		newCode.setDisplayOrder(new Integer(order));
		newCode.setLabel(label);
		newCode.setParentId(parentTaxonomy.getCodeId());
		newCode.setRootNodeId(Integer.valueOf(rootNodeId));
		newCode.setdisplayCode(displayCode);
		taxonomyRepository.save(newCode);

		this.writeToDisk(rootNode);

		TaxonomyDTO taxdto = new TaxonomyDTO();
		List<Code> codes = new ArrayList<Code>();
		codes.add(newCode);

		taxdto.setCode(codes);
		indexHandler.setReIndexRequest(newCode.getCodeId() + "", IndexProcessor.INDEX, TAXONOMY, null, false, false);

		return taxdto;
	}

	@Override
	public void writeTaxonomyToDisk() {
		@SuppressWarnings("unchecked")
		List<Organization> organizations = organizationRepository.getAll(Organization.class);
		if (organizations != null) {
			for (Organization organization : organizations) {
				Integer codeId = TaxonomyUtil.getTaxonomyRootId(organization.getPartyUid());
				if (codeId != null) {
					Code code = (Code) taxonomyRepository.findCode(codeId, organization.getPartyUid());
					if (code != null) {
						try {
							this.writeToDisk(code);
						} catch (Exception e) {
							logger.error("Error while creating taxonomy", e);
						}
					}
				}
			}
		}
	}

	@Override
	public TaxonomyDTO updateTaxonomy(String codeId, String label, String order, String code) throws Exception {

		Code node = (Code) taxonomyRepository.get(Code.class, new Integer(codeId));
		if (code != null) {
			node.setCode(code);
		}

		if (label != null) {
			node.setLabel(label);
		}

		node.setDisplayOrder(new Integer(order));

		List<Code> siblings = this.findChildTaxonomyCodeByOrder(node.getParentId(), order);
		if ( siblings != null && siblings.size() != 0) {
			for (Code sibling : siblings) {
				sibling.setDisplayOrder(sibling.getDisplayOrder() + 1);
				this.updateOrder(sibling);
			}
		}

		taxonomyRepository.save(node);

		Code rootNode = (Code) taxonomyRepository.get(Code.class, node.getCodeType().getCodeId());

		this.writeToDisk(rootNode);

		TaxonomyDTO taxdto = new TaxonomyDTO();
		List<Code> codes = new ArrayList<Code>();
		codes.add(node);

		taxdto.setCode(codes);
		indexHandler.setReIndexRequest(node.getCodeId() + "", IndexProcessor.INDEX, TAXONOMY, null, false, false);

		return taxdto;
	}

	@Override
	public void deleteTaxonomy(String code) throws Exception {

		Code node = (Code) taxonomyRepository.get(Code.class, new Integer(code));

		Code rootNode = (Code) taxonomyRepository.get(Code.class, node.getCodeType().getCodeId());

		taxonomyRepository.remove(Code.class, node.getCodeId());

		this.writeToDisk(rootNode);
		indexHandler.setReIndexRequest(node.getCodeId() + "", IndexProcessor.DELETE, TAXONOMY, null, false, false);
	}

	@Override
	public TaxonomyDTO createTaxonomyRoot(String code, String label, String isCodeAutoGenerated, Integer rootNodeId) throws Exception {

		CodeType codeType = new CodeType();
		codeType.setDepth(Short.valueOf("0"));
		codeType.setLabel(label);
		codeType.setIsAutogeneratedCode(Integer.valueOf(isCodeAutoGenerated));

		taxonomyRepository.save(codeType);

		Code newCode = new Code();
		newCode.setCode(code);
		newCode.setCodeType(codeType);
		newCode.setDepth(Short.valueOf("0"));
		newCode.setDescription("");
		newCode.setDisplayOrder(0);
		newCode.setLabel(label);
		newCode.setParentId(null);
		newCode.setRootNodeId(rootNodeId);
		newCode.setdisplayCode(null);

		taxonomyRepository.save(newCode);

		CodeType updateCodetype = (CodeType) taxonomyRepository.get(CodeType.class, codeType.getTypeId());
		codeType.setCodeId(newCode.getCodeId());

		taxonomyRepository.save(updateCodetype);

		this.writeToDisk(newCode); // Generating XML

		TaxonomyDTO taxdto = new TaxonomyDTO();
		List<Code> codes = new ArrayList<Code>();
		codes.add(newCode);

		taxdto.setCode(codes);
		indexHandler.setReIndexRequest(newCode.getCodeId() + "", IndexProcessor.INDEX, TAXONOMY, null, false, false);
		return taxdto;
	}

	@Override
	public TaxonomyDTO addLevel(String taxonomyCode, String label, String isCodeAutogenerated) {

		Code code = (Code) taxonomyRepository.get(Code.class, new Integer(taxonomyCode));

		int depth = this.findMaxDepthInTaxonomy(code);

		CodeType codeType = new CodeType();
		codeType.setDepth(Short.valueOf((short) (depth + 1)));
		codeType.setLabel(label);
		codeType.setCodeId(code.getCodeId());
		codeType.setIsAutogeneratedCode(Integer.valueOf(isCodeAutogenerated));

		taxonomyRepository.save(codeType);

		codeType.setCode(code);
		indexHandler.setReIndexRequest(code.getCodeId() + "", IndexProcessor.INDEX, TAXONOMY, null, false, false);

		TaxonomyDTO taxDto = new TaxonomyDTO();
		List<CodeType> codes = new ArrayList<CodeType>();
		codes.add(codeType);

		taxDto.setCodetypes(codes);

		return taxDto;
	}

	@Override
	public TaxonomyDTO updateLevel(String leveldepth, String taxonomyCode, String label) {

		Code code = (Code) taxonomyRepository.get(Code.class, new Integer(taxonomyCode));

		CodeType codeType = this.findTaxonomyTypeBydepth(code, Short.valueOf(leveldepth));
		codeType.setLabel(label);
		codeType.setCodeId(code.getCodeId());
		codeType.setDepth(Short.valueOf(leveldepth));

		taxonomyRepository.save(codeType);

		codeType.setCode(code);
		indexHandler.setReIndexRequest(code.getCodeId() + "", IndexProcessor.INDEX, TAXONOMY, null, false, false);

		TaxonomyDTO taxDto = new TaxonomyDTO();
		List<CodeType> codes = new ArrayList<CodeType>();
		codes.add(codeType);

		taxDto.setCodetypes(codes);

		return taxDto;
	}

	@Override
	public void deleteLevel(String leveldepth, String taxonomyCode) {

		Code code = (Code) taxonomyRepository.get(Code.class, new Integer(taxonomyCode));

		CodeType codeType = this.findTaxonomyTypeBydepth(code, Short.valueOf(leveldepth));

		taxonomyRepository.remove(CodeType.class, codeType.getTypeId());
	}

	@Override
	public TaxonomyDTO getTaxonomyLevels(String taxonomyCode) {

		Code node = (Code) this.taxonomyRepository.get(Code.class, new Integer(taxonomyCode));// (taxonomyCode);

		List<CodeType> taxonomyLevels = this.taxonomyRepository.findTaxonomyLevels(node);

		for (CodeType codeType : taxonomyLevels)
			codeType.setCode(node);

		TaxonomyDTO taxonomydto = new TaxonomyDTO();
		taxonomydto.setCodetypes(taxonomyLevels);
		return taxonomydto;
	}

	@Override
	public Code findCodeByTaxonomyCodeId(Integer taxonomyCodeId) {
		return (Code) taxonomyRepository.get(Code.class, taxonomyCodeId);
	}

	@Override
	public TaxonomyDTO updateAssocation(String taxonomyCodeId, String curriculumIds, Code code) {

		String[] CurriculumId = curriculumIds.split("\\s*,\\s*");
		List<Code> curriculums = new ArrayList<Code>();

		TaxonomyDTO taxDto = new TaxonomyDTO();

		if (CurriculumId != null) {
			for (String id : CurriculumId) {
				Code curriculum = (Code) taxonomyRepository.findCodeByTaxCode(id);
				if (curriculum != null && curriculum.getCodeType().getCodeId() != 1) {
					curriculums.add(curriculum);
				}
				indexHandler.setReIndexRequest(curriculum.getCodeId() + "", IndexProcessor.INDEX, TAXONOMY, null, false, false);
			}

			this.updateTaxonomyAssociation(code, curriculums);
		}

		List<Code> codes = new ArrayList<Code>();
		codes.add(code);
		List<Code> mappings = this.findTaxonomyMappings(codes);
		taxDto.setCode(mappings);
		return taxDto;
	}

	@Override
	public TaxonomyDTO deleteMapping(String[] CurriculumId, Code code) {

		List<Code> curriculums = new ArrayList<Code>();

		if (CurriculumId != null) {
			for (String id : CurriculumId) {
				Code curriculum = (Code) taxonomyRepository.get(Code.class, new Integer(id));
				if (curriculum != null && curriculum.getCodeType().getCodeId() != 1) {
					curriculums.add(curriculum);
				}
				indexHandler.setReIndexRequest(curriculum.getCodeId() + "", IndexProcessor.INDEX, TAXONOMY, null, false, false);
			}

			this.deleteTaxonomyMapping(code, curriculums);
		}

		List<Code> codes = new ArrayList<Code>();
		codes.add(code);

		List<Code> mappings = this.findTaxonomyMappings(codes);

		TaxonomyDTO taxDto = new TaxonomyDTO();
		taxDto.setCode(mappings);
		return taxDto;
	}

	@Override
	public Code saveUploadTaxonomyImage(Code code) {
		taxonomyRepository.save(code);
		return code;
	}

	@Override
	public List<Code> findParentTaxonomy(Integer codeId, boolean reverse) {
		return taxonomyRepository.findParentTaxonomy(codeId, reverse);
	}

	@Override
	public List<CodeFo> getCourseBySubject(Integer codeId, Integer maxLessonLimit) throws JSONException {
		List<CodeFo> taxonomyCodeList = new ArrayList<CodeFo>();
		List<Code> subjectCodeList = taxonomyRepository.findChildTaxonomyCodeByDepth(codeId, 1);
		for (Code subjectCode : subjectCodeList) {
			List<Code> courseCodeList = taxonomyRepository.findChildTaxonomyCodeByDepth(subjectCode.getCodeId(), 2);
			CodeFo subjectCodeObj = new CodeFo();
			subjectCodeObj.setCodeId(subjectCode.getCodeId());
			subjectCodeObj.setLabel(subjectCode.getLabel());
			subjectCodeObj.setParentId(subjectCode.getParentId());
			List<CodeFo> subjectCourseList = new ArrayList<CodeFo>();
			for (Code courseCode : courseCodeList) {
				CodeFo courseCodeObj = new CodeFo();
				courseCodeObj.setCodeId(courseCode.getCodeId());
				courseCodeObj.setLabel(courseCode.getLabel());
				courseCodeObj.setParentId(courseCode.getParentId());
				courseCodeObj.setGrade(courseCode.getGrade());
				courseCodeObj.setOrganization(courseCode.getOrganization());
				courseCodeObj.setS3UploadFlag(courseCode.getS3UploadFlag());
				courseCodeObj.setdisplayCode(courseCode.getdisplayCode());
				List<CodeFo> courseLessonList = new ArrayList<CodeFo>();
				Code unitCode = taxonomyRepository.findFirstChildTaxonomyCodeByDepth(courseCode.getCodeId(), 3);
				courseCodeObj.setFirstUnitId(unitCode != null ? unitCode.getCodeId() : null);
				courseCodeObj.setNode(courseLessonList);
				courseCodeObj.setCommonCoreDotNotation(courseCode.getCommonCoreDotNotation());
				subjectCourseList.add(courseCodeObj);
			}
			subjectCodeObj.setNode(subjectCourseList);
			taxonomyCodeList.add(subjectCodeObj);
		}
		return taxonomyCodeList;
	}

	@Override
	public List<Code> getCurriculum() {
		List<Code> curriculumCodeList = taxonomyRepository.getCurriculumCodeByDepth(0);
		return curriculumCodeList;
	}
	
	@Override
	public List<Map<String, Object>> getCurriculum(List<Integer> codeIds) {
		List<Map<String, Object>> curriculumCodeList = taxonomyRepository.getCurriculum(codeIds);
		return curriculumCodeList;
	}

	@Override
	public FilterSubjectFo getFilterSubject(Integer codeId, Integer maxLessonLimit) {
		FilterSubjectFo filterSubjectFo = new FilterSubjectFo();
		List<String> subjects = new ArrayList<String>();
		List<String> categories = new ArrayList<String>();
		List<String> gradeLevels = new ArrayList<String>();
		List<Code> subjectCodeList = taxonomyRepository.findChildTaxonomyCodeByDepth(codeId, 1);
		for (Code subjectCode : subjectCodeList) {
			subjects.add(subjectCode.getLabel());
		}
		filterSubjectFo.setSubject(subjects);
		categories.add(_ALL);
		categories.add(VIDEOS);
		categories.add(WEB_SITES);
		categories.add(INTERACTIVES);
		categories.add(SLIDES);
		categories.add(TEXT_BOOKS);
		categories.add(EXAMS);
		categories.add(LESSONS);
		categories.add(HAND_OUTS);
		filterSubjectFo.setCategory(categories);
		gradeLevels.add(MIDDLE_SCHOOL);
		gradeLevels.add(HIGH_SCHOOL);
		filterSubjectFo.setGradeLevel(gradeLevels);
		return filterSubjectFo;
	}

	@Override
	public Code findTaxonomyCodeById(Integer taxonomyCodeId) {
		Code code = this.getTaxonomyRepository().findTaxonomyCodeById(taxonomyCodeId);
		if (code == null) {
			throw new BadRequestException("Invalid CodeId!!!");
		}
		return code;
	}

	@Override
	public Map<String, List<Code>> findCodeByParentCodeId(String parentCode, boolean groupByCode, String creatorUid, String fetchType, String organizationCode) {
		List<CodeOrganizationAssoc> codes = this.getTaxonomyRepository().findCodeByParentCodeId(parentCode, creatorUid, null, null, fetchType, organizationCode, null, null);
		Map<String, List<Code>> codeBygroup = new HashMap<String, List<Code>>();
		List<Code> codeList = new ArrayList<Code>();
		if (groupByCode && codes.size() > 0) {
			List<Code> elementList = new ArrayList<Code>();
			List<Code> middleList = new ArrayList<Code>();
			List<Code> highList = new ArrayList<Code>();
			List<Code> otherList = new ArrayList<Code>();
			for (CodeOrganizationAssoc codeOrganizationAssoc : codes) {
				if (codeOrganizationAssoc.getCode().getGrade() != null && ELEMETRY.contains(codeOrganizationAssoc.getCode().getGrade().toString())) {
					elementList.add(codeOrganizationAssoc.getCode());
				} else if (codeOrganizationAssoc.getCode().getGrade() != null && MIDDLE.contains(codeOrganizationAssoc.getCode().getGrade().toString())) {
					middleList.add(codeOrganizationAssoc.getCode());
				} else if (codeOrganizationAssoc.getCode().getGrade() != null && HIGH.contains(codeOrganizationAssoc.getCode().getGrade().toString())) {
					highList.add(codeOrganizationAssoc.getCode());
				} else {
					otherList.add(codeOrganizationAssoc.getCode());
				}
				codeBygroup.put(ELEMENTARY_SCHOOL, elementList);
				codeBygroup.put(MIDDLE__SCHOOL, middleList);
				codeBygroup.put(HIGH__SCHOOL, highList);
				codeBygroup.put(_OTHER, otherList);
				codeList.add(codeOrganizationAssoc.getCode());
			}
			return codeBygroup;
		}
		codeBygroup.put(TAXONOMY_CODES, codeList);
		return codeBygroup;
	}

	@Override
	public List<Map<String, Object>> getStandards(String code, Integer depth) {
		List<Map<String, Object>> standards = null;
		if (depth == 0) {
			standards = levelZero(code);
		} else if (depth == 1) {
			standards = levelOne(code);
		} else if (depth == 2) {
			standards = levelTwo(code);
		} else if (depth == 3) {
			standards = levelThree(code);
		}
		return standards;
	}

	private List<Map<String, Object>> levelZero(String code) {
		List<Code> curriculums = this.getTaxonomyRepository().findCodeStartWith(code, Short.valueOf("0"));
		List<Map<String, Object>> levelOneMapCodes = new ArrayList<Map<String, Object>>();
		int levelOneCount = 0;
		for (Code curriculum : curriculums) {
			List<Code> levelOneCodes = this.getTaxonomyRepository().findChildTaxonomy(String.valueOf(curriculum.getCodeId()), 1);
			for (Code levelOneCode : levelOneCodes) {
				List<Map<String, Object>> levelTwoMapCodes = null;
				if (levelOneCount == 0) {
					levelTwoMapCodes = levelOne(String.valueOf(levelOneCode.getCodeId()));
				}
				levelOneMapCodes.add(getCode(levelOneCode, levelTwoMapCodes, NODE));
				levelOneCount++;
			}
		}
		return levelOneMapCodes;
	}

	private List<Map<String, Object>> levelOne(String codeId) {
		List<Code> levelTwoCodes = this.getTaxonomyRepository().findChildTaxonomy(codeId, 2);
		List<Map<String, Object>> levelTwoMapCodes = new ArrayList<Map<String, Object>>();
		int levelCount = 0;
		for (Code levelTwoCode : levelTwoCodes) {
			List<Map<String, Object>> levelThreeMapCodes = null;
			if (levelCount == 0) {
				levelThreeMapCodes = levelTwo(String.valueOf(levelTwoCode.getCodeId()));
			}
			levelCount++;
			levelTwoMapCodes.add(getCode(levelTwoCode, levelThreeMapCodes, NODE));
		}
		return levelTwoMapCodes;

	}

	private List<Map<String, Object>> levelTwo(String codeId) {
		int levelCount = 0;
		List<Code> levelThreeCodes = this.getTaxonomyRepository().findChildTaxonomy(codeId, 3);
		List<Map<String, Object>> levelThreeMapCodes = new ArrayList<Map<String, Object>>();
		for (Code levelThreeCode : levelThreeCodes) {
			List<Map<String, Object>> levelFourMapCodes = null;
			if (levelCount == 0) {
				levelFourMapCodes = levelThree(String.valueOf(levelThreeCode.getCodeId()));
			}
			levelCount++;
			levelThreeMapCodes.add(getCode(levelThreeCode, levelFourMapCodes, NODE));
		}

		return levelThreeMapCodes;
	}

	private List<Map<String, Object>> levelThree(String codeId) {
		Code code = this.getTaxonomyRepository().findCodeByCodeId(Integer.valueOf(codeId));
		Code curriculum = this.getTaxonomyRepository().findCodeByCodeId(code.getRootNodeId());
		List<Map<String, Object>> levelFourMapCodes = new ArrayList<Map<String, Object>>();
		List<Code> levelFourCodes = this.getTaxonomyRepository().findChildTaxonomy(codeId, 4);
		for (Code levelFourCode : levelFourCodes) {
			List<Code> levelFiveCodes = this.getTaxonomyRepository().findChildTaxonomy(String.valueOf(levelFourCode.getCodeId()), 5);
			if (curriculum.getCode().equalsIgnoreCase("CA") || curriculum.getCode().equalsIgnoreCase("CASK5") || curriculum.getCode().equalsIgnoreCase("TEKS")) {
				levelFourMapCodes.add(getCode(levelFourCode, null, NODE));
			}
			for (Code levelFiveCode : levelFiveCodes) {
				List<Code> levelSixCodes = this.getTaxonomyRepository().findChildTaxonomy(String.valueOf(levelFiveCode.getCodeId()), 6);
				levelFourMapCodes.add(getCode(levelFiveCode, null, NODE));
				for (Code levelSixCode : levelSixCodes) {
					levelFourMapCodes.add(getCode(levelSixCode, null, NODE));
				}
			}
		}
		return levelFourMapCodes;
	}

	private Map<String, Object> getCode(Code code, List<Map<String, Object>> childern, String type) {
		final Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put(CODE, code.getCommonCoreDotNotation() == null ? code.getdisplayCode() : code.getCommonCoreDotNotation());
		codeMap.put(CODE_ID, code.getCodeId());
		codeMap.put(LABEL, code.getLabel());
		codeMap.put(PARENT_ID, code.getParentId());
		codeMap.put(type, childern);
		return codeMap;
	}

	private Map<String, Object> getCode(Object code, Object codeId, Object label, List<Map<String, Object>> childern) {
		final Map<String, Object> codeMap = new HashMap<String, Object>();
		codeMap.put(CODE, code);
		codeMap.put(LABEL, label);
		codeMap.put(CODE_ID, codeId);
		codeMap.put(NODE, childern);
		return codeMap;
	}

	@Override
	public Map<String, Object> getTaxonomySkills() {
		List<Code> model = this.getTaxonomyRepository().findCodeStartWith(TWENTY_FIRST_CENTURY_SKILLS_MODEL, Short.parseShort(ONE));
		List<Code> key = this.getTaxonomyRepository().findCodeStartWith(TWENTY_FIRST_CENTURY_SKILLS_KEY, Short.parseShort(TWO));
		Map<String, Object> skills = new HashMap<String, Object>();
		skills.put(MODEL, model);
		List<Map<String, Object>> keys = new ArrayList<Map<String, Object>>();
		for (Code code : key) {
			List<Map<String, Object>> node = new ArrayList<Map<String, Object>>();
			List<Object[]> result = this.getTaxonomyRepository().getTaxonomySkills(code.getCodeId());
			for (Object[] object : result) {
				node.add(getCode(object[3], object[0], object[2], null));
			}
			keys.add(getCode(code.getCode(), code.getCodeId(), code.getLabel(), node));
		}
		skills.put(KEY, keys);
		return skills;
	}

	public TaxonomyRespository getTaxonomyRepository() {
		return taxonomyRepository;
	}

	public TaxonomyCassandraService getTaxonomyCassandraService() {
		return taxonomyCassandraService;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public RedisService getRedisService() {
		return redisService;
	}

}
