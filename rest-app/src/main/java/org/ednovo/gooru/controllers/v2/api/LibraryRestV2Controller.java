/////////////////////////////////////////////////////////////
//LibraryRestV2Controller.java
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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.featured.FeaturedService;
import org.ednovo.gooru.domain.service.redis.RedisService;
import org.ednovo.gooru.domain.service.search.SearchResults;
import org.ednovo.gooru.domain.service.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/v2/library")
public class LibraryRestV2Controller extends BaseController implements ConstantProperties {

    @Autowired
    private TaxonomyService taxonomyService;

    @Autowired
    private FeaturedService featuredService;

    @Autowired
    private RedisService redisService;

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/{type}", method = RequestMethod.GET)
    public ModelAndView getLibrary(@PathVariable(value = TYPE) String type, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache, @RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName, HttpServletRequest request,
            HttpServletResponse response) {
        Map<Object, Object> library = null;
        final String cacheKey = V2_LIBRARY_DATA + type + "-" + libraryName;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            library = this.getFeaturedService().getLibrary(type, libraryName);
            String includes[] = (String[]) ArrayUtils.addAll(LIBRARY_RESOURCE_INCLUDE_FIELDS, LIBRARY_COLLECTION_INCLUDE_FIELDS);
            includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
            includes = (String[]) ArrayUtils.addAll(includes, LIBRARY_CODE_INCLUDES);
            data = serialize(library, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, includes);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }
    
    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/{type}/item", method = RequestMethod.GET)
    public ModelAndView getLibraryItem(@PathVariable(value = TYPE) String type, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache, @RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName, HttpServletRequest request,
            HttpServletResponse response) {
        final String cacheKey =  V2_LIBRARY_DATA_ITEM + type + "-" + libraryName;
        List<Map<String, Object>> library = null;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            library = this.getFeaturedService().getLibraryItem(type, libraryName);
            String includes[] = (String[]) ArrayUtils.addAll(LIBRARY_RESOURCE_INCLUDE_FIELDS, LIBRARY_COLLECTION_INCLUDE_FIELDS);
            includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
            includes = (String[]) ArrayUtils.addAll(includes, LIBRARY_CODE_INCLUDES);
            data = serialize(library, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, includes);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }
    
    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/{type}/item/{itemType}/{id}", method = RequestMethod.GET)
    public ModelAndView getLibraryItems(@PathVariable(value = TYPE) String type, @PathVariable(value = ITEM_TYPE) String itemType, @PathVariable(value = ID) String id, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache, @RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName, 
            @RequestParam(value = ROOT_NODE_ID, required = false, defaultValue = "20000") String rootNodeId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "5") Integer limit, HttpServletRequest request,
            HttpServletResponse response) {
        final String cacheKey =  V2_LIBRARY_DATA_ITEM + type + "-" + libraryName + "-" + itemType + "-" + id  + "-" + limit + "-" + offset;
        List<Map<String, Object>> library = null;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            library = this.getFeaturedService().getLibraryItems(itemType, type, id, libraryName, type.equalsIgnoreCase(STANDARD) ? null : rootNodeId, limit, offset);
            String includes[] = (String[]) ArrayUtils.addAll(LIBRARY_RESOURCE_INCLUDE_FIELDS, LIBRARY_COLLECTION_INCLUDE_FIELDS);
            includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
            includes = (String[]) ArrayUtils.addAll(includes, LIBRARY_CODE_INCLUDES);
            data = serialize(library, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, includes);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/{type}/{topicId}", method = RequestMethod.GET)
    public ModelAndView getLibraryTopic(@PathVariable(value = TYPE) String type, @PathVariable(value = TOPIC_ID) String topicId, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache,
            @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "5") Integer limit, @RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName,
            @RequestParam(value = ROOT_NODE_ID, required = false, defaultValue = "20000") String rootNode, HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> library = null;
        final String cacheKey = V2_LIBRARY_DATA + type + "-" + topicId + limit + offset + "-" + libraryName + "-" + rootNode;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            library = this.getFeaturedService().getLibraryTopic(topicId, limit, offset, type, libraryName, rootNode);
            String includes[] = (String[]) ArrayUtils.addAll(LIBRARY_RESOURCE_INCLUDE_FIELDS, LIBRARY_COLLECTION_INCLUDE_FIELDS);
            includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
            includes = (String[]) ArrayUtils.addAll(includes, LIBRARY_CODE_INCLUDES);
            data = serialize(library, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, includes);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/{type}/unit/{id}", method = RequestMethod.GET)
    public ModelAndView getLibraryUnit(@PathVariable(value = TYPE) String type, @PathVariable(value = ID) String id, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache,
            @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, @RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName,
            @RequestParam(value = ROOT_NODE_ID, required = false, defaultValue = "20000") String rootNode, HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> library = null;
        final String cacheKey = V2_LIBRARY_DATA + type + "-" + id + limit + offset + "-" + libraryName + "-" + rootNode;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            library = this.getFeaturedService().getLibraryUnit(id, type, offset, limit, libraryName, rootNode);
            String includes[] = (String[]) ArrayUtils.addAll(LIBRARY_RESOURCE_INCLUDE_FIELDS, LIBRARY_COLLECTION_INCLUDE_FIELDS);
            includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
            includes = (String[]) ArrayUtils.addAll(includes, LIBRARY_CODE_INCLUDES);
            data = serialize(library, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, includes);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/{type}/collection/{id}", method = RequestMethod.GET)
    public ModelAndView getLibraryCollection(@PathVariable(value = TYPE) String type, @PathVariable(value = ID) Integer id, HttpServletRequest request, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit,@RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache, HttpServletResponse response) {
        final String cacheKey = V2_LIBRARY_REALTED_COLLECTION_DATA + type + "-" + id + limit + offset + "-" + libraryName;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            data = serialize(this.getFeaturedService().getLibraryCollection(id, type, offset, limit, libraryName), RESPONSE_FORMAT_JSON);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/collection", method = RequestMethod.GET)
    public ModelAndView getLibraryCollections(HttpServletRequest request, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, @RequestParam(value = THEME_CODE, required = false) String themeCode,
            @RequestParam(value = THEME_TYPE, required = false) String themeType, @RequestParam(value = FLT_SUBJECT, required = false) String subjectId, @RequestParam(value = FLT_COURSE, required = false) String courseId, @RequestParam(value = FLT_UNIT, required = false) String unitId,
            @RequestParam(value = FLT_LESSON, required = false) String lessonId, @RequestParam(value = FLT_TOPIC, required = false) String topicId, @RequestParam(value = GOORU_OID, required = false) String gooruOid, @RequestParam(value = CODE_ID, required = false) String codeId, HttpServletResponse response) {
        final String cacheKey = V2_LIBRARY_REALTED_COLLECTIONS_DATA_FEATURED;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            SearchResults<Map<String, Object>> results = this.getFeaturedService().getLibraryCollections(limit, offset, themeCode, themeType, subjectId, courseId, unitId, lessonId, topicId, gooruOid, codeId);
            String includes[] = (String[]) ArrayUtils.addAll(LIBRARY_FEATURED_COLLECTIONS_INCLUDE_FIELDS, LIBRARY_FEATURED_COLLECTIONS_USER_INCLUDE_FIELDS);
            data = serialize(results, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, includes);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/user/contributors", method = RequestMethod.GET)
    public ModelAndView getLibraryContributor(@RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache) {
        List<Map<Object, Object>> contributors = null;
        final String cacheKey = V2_LIBRARY_DATA_CONTRIBUTOR + libraryName;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            contributors = this.getFeaturedService().getLibraryContributor(libraryName);
            data = serialize(contributors, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, LIBRARY_CONTRIBUTOR_INCLUDES);
            getRedisService().putValue(cacheKey, data);
        }

        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/{id}/collection/popular", method = RequestMethod.GET)
    public ModelAndView getLibraryPopularCollection(@PathVariable(value = ID) String id, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit, @RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName, HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> library = null;
        final String cacheKey = V2_LIBRARY_POPULAR_DATA + id + limit + offset + "-" + libraryName;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            library = this.getFeaturedService().getPopularLibrary(id, offset, limit, libraryName);
            String includes[] = (String[]) ArrayUtils.addAll(LIBRARY_RESOURCE_INCLUDE_FIELDS, LIBRARY_COLLECTION_INCLUDE_FIELDS);
            includes = (String[]) ArrayUtils.addAll(includes, COLLECTION_ITEM_INCLUDE_FILEDS);
            data = serialize(library, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, includes);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "/resource", method = RequestMethod.GET)
    public ModelAndView getLibraryResource(HttpServletRequest request, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") Integer offset, @RequestParam(value = TYPE, required = false) String type,
            @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") Integer limit,@RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY) String libraryName, @RequestParam(value = CLEAR_CACHE, required = false, defaultValue = FALSE) boolean clearCache, HttpServletResponse response) {
        final String cacheKey = V2_LIBRARY_REALTED_RESOURCES_DATA_FEATURED;
        String data = null;
        if (!clearCache) {
            data = getRedisService().getValue(cacheKey);
        }
        if (data == null) {
            SearchResults<Map<String, Object>> results = this.getFeaturedService().getLibraryResource(type, offset, limit, libraryName);
            data = serialize(results, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, false, true, LIB_RESOURCE_FIELDS);
            getRedisService().putValue(cacheKey, data);
        }
        return toModelAndView(data);
    }

    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_READ })
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ModelAndView getLibrary(@RequestParam(value = LIBRARY_NAME, required = false, defaultValue = LIBRARY_NAMES) String libraryName, HttpServletRequest request, HttpServletResponse response) {
        return toModelAndView(this.getFeaturedService().getLibrary(libraryName), JSON);
    }

    
    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_ADD })
    @RequestMapping(value = "/{libraryId}", method = RequestMethod.POST)
    public ModelAndView assocaiateCollectionLibrary(@PathVariable(value = LIBRARY_ID) String libraryId, @RequestParam(value = CODE_ID) String codeId, @RequestParam(value = GOORU_OID) String gooruOid, HttpServletRequest request, HttpServletResponse response) {
        return toModelAndView(this.getFeaturedService().assocaiateCollectionLibrary(libraryId, codeId, gooruOid), JSON);
    }

  
    @AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_LIBRARY_DELETE })
    @RequestMapping(value = "/{libraryId}", method = RequestMethod.DELETE)
    public void deleteAssocCollectionLibrary(@PathVariable(value = LIBRARY_ID) String libraryId, @RequestParam(value = CODE_ID) String codeId, @RequestParam(value = GOORU_OID) String gooruOid, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        this.getFeaturedService().deleteLibraryCollectionAssoc(libraryId, codeId, gooruOid);
    }

    public TaxonomyService getTaxonomyService() {
        return taxonomyService;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    public FeaturedService getFeaturedService() {
        return featuredService;
    }

}