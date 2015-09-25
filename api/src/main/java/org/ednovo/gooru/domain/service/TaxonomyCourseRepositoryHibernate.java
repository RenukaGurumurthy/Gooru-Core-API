/////////////////////////////////////////////////////////////
// TaxonomyCourseRepositoryHibernate.java
// gooru-api
// Created by Gooru on 2015
// Copyright (c) 2015 Gooru. All rights reserved.
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
package org.ednovo.gooru.domain.service;

import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.api.model.ContentTaxonomyCourseAssoc;
import org.ednovo.gooru.core.api.model.TaxonomyCourse;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class TaxonomyCourseRepositoryHibernate extends BaseRepositoryHibernate implements TaxonomyCourseRepository, ParameterProperties, ConstantProperties {

	private static final String GET_COURSE = "FROM TaxonomyCourse c  WHERE c.courseId=:courseId";

	private static final String GET_COURSE_CODE = "FROM TaxonomyCourse c  WHERE c.courseCode=:courseCode";

	private static final String GET_COURSES = "FROM TaxonomyCourse order by displaySequence";

	private static final String GET_MAX = "SELECT COALESCE(MAX(displaySequence),0) FROM TaxonomyCourse";

	private static final String GET_DOMAINS = "select d.domain_id as domainId,d.name, d.image_path as imagePath, s.subdomain_id as subdomainId, s.description, c.subject_id as subjectId, c.course_id as courseId  from domain d join subdomain s on s.domain_id=d.domain_id inner join course c on s.course_id=c.course_id where c.course_id=:courseId";

	private static final String GET_TAXONOMY_COURSES = "FROM TaxonomyCourse c  WHERE c.courseId  in (:courseId)";
	
	private static final String GET_CONTENT_COURSE = "FROM ContentTaxonomyCourseAssoc c  WHERE c.content.contentId=:contentId";

	@Override
	public TaxonomyCourse getCourse(Integer courseId) {
		Query query = getSession().createQuery(GET_COURSE).setParameter(COURSE_ID, courseId);
		return (TaxonomyCourse) (query.list().size() > 0 ? query.list().get(0) : null);
	}

	@Override
	public TaxonomyCourse getCourseCode(String courseCode) {
		Query query = getSession().createQuery(GET_COURSE_CODE).setParameter(COURSE_CODE, courseCode);
		return (TaxonomyCourse) (query.list().size() > 0 ? query.list().get(0) : null);
	}

	@Override
	public List<TaxonomyCourse> getCourses(Integer limit, Integer offset) {
		Query query = getSession().createQuery(GET_COURSES);
		if (limit == null) {
			limit = 0;
		}
		query.setMaxResults(limit > MAX_LIMIT ? MAX_LIMIT : limit);
		query.setFirstResult(offset);
		return list(query);
	}

	@Override
	public Integer getMaxSequence() {
		Query query = getSession().createQuery(GET_MAX);
		return (Integer) query.list().get(0);
	}

	@Override
	public List<Map<String, Object>> getDomains(Integer courseId, int limit, int offset) {
		Query query = getSession().createSQLQuery(GET_DOMAINS).setParameter(COURSE_ID, courseId);
		query.setMaxResults(limit > MAX_LIMIT ? MAX_LIMIT : limit);
		query.setFirstResult(offset);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public List<TaxonomyCourse> getTaxonomyCourses(List<Integer> courseIds) {
		Query query = getSession().createQuery(GET_TAXONOMY_COURSES);
		query.setParameterList(COURSE_ID, courseIds);
		return list(query);
	}
	
	@Override
	public List<ContentTaxonomyCourseAssoc> getContentTaxonomyCourseAssoc(Long contentId) {
		Query query = getSession().createQuery(GET_CONTENT_COURSE);
		query.setParameter(CONTENT_ID, contentId);
		return list(query);
	}
}
