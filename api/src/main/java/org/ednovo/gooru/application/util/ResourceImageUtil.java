/////////////////////////////////////////////////////////////
// ResourceImageUtil.java
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.Job;
import org.ednovo.gooru.core.api.model.Resource;
import org.ednovo.gooru.core.api.model.ResourceInfo;
import org.ednovo.gooru.core.api.model.ResourceType;
import org.ednovo.gooru.core.api.model.UserGroupSupport;
import org.ednovo.gooru.core.application.util.BaseUtil;
import org.ednovo.gooru.core.cassandra.model.ResourceMetadataCo;
import org.ednovo.gooru.core.constant.ConfigConstants;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.domain.service.job.JobService;
import org.ednovo.gooru.domain.service.resource.MediaService;
import org.ednovo.gooru.domain.service.setting.SettingService;
import org.ednovo.gooru.domain.service.storage.S3ResourceApiHandler;
import org.ednovo.gooru.infrastructure.messenger.IndexHandler;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.gooru.infrastructure.persistence.hibernate.resource.ResourceRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyRespository;
import org.ednovo.gooru.kafka.producer.KafkaProducer;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

@Component
public class ResourceImageUtil extends UserGroupSupport implements ParameterProperties {

	@Autowired
	private MediaService mediaService;

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private S3ResourceApiHandler s3ResourceApiHandler;

	@Autowired
	private SettingService settingService;

	@Autowired
	private TaxonomyRespository taxonomyRespository;

	@Autowired
	private IndexProcessor indexProcessor;

	@Autowired
	private JobService jobService;

	@Autowired
	private AsyncExecutor asyncExecutor;

	@Autowired
	private IndexHandler indexHandler;

	private Map<String, String> propertyMap;

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceImageUtil.class);

	private static final String THUMBNAIL = "thumbnail_small";

	private static final String VIMEO_VIDEO = "vimeo.com";

	private static final String YOUTUBE_VIDEO = "youtube.com";

	public static final String CONVERT_DOCUMENT_PDF = "convert.docToPdf";

	public static String V2_ORGANIZE_DATA = "v2-organize-data-";

	private static final String FOLDER_IN_BUCKET = "folderInBucket";

	private static final String GOORU_BUCKET = "gooruBucket";

	@Autowired
	private KafkaProducer kafkaProducer;

	public void sendMsgToGenerateThumbnails(Resource resource) {

		sendMsgToGenerateThumbnails(resource, resource.getThumbnail());
	}

	public void sendMsgToGenerateThumbnails(Resource resource, String fileName) {
		String repoPath = resource.getOrganization().getNfsStorageArea().getInternalPath() + resource.getFolder();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(SOURCE_FILE_PATH, repoPath + fileName);
		param.put(TARGET_FOLDER_PATH, repoPath);
		param.put(THUMBNAIL, resource.getThumbnail());
		param.put(DIMENSIONS, getDimensions(resource));
		param.put(RESOURCE_GOORU_OID, resource.getGooruOid());
		param.put(API_END_POINT, settingService.getConfigSetting(ConfigConstants.GOORU_API_ENDPOINT, 0, TaxonomyUtil.GOORU_ORG_UID));
		LOGGER.debug(fileName);
		LOGGER.debug(settingService.getConfigSetting(ConfigConstants.GOORU_CONVERSION_RESTPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "/conversion/image");
		this.getAsyncExecutor().executeRestAPI(param, settingService.getConfigSetting(ConfigConstants.GOORU_CONVERSION_RESTPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "/conversion/image", Method.POST.getName());
	}

	public void generateThumbnails(Resource resource, String fileName) {
		String repoPath = resource.getOrganization().getNfsStorageArea().getInternalPath() + resource.getFolder();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(SOURCE_FILE_PATH, repoPath + fileName);
		param.put(TARGET_FOLDER_PATH, repoPath);
		param.put(THUMBNAIL, resource.getThumbnail());
		param.put(DIMENSIONS, getDimensions(resource));
		param.put(RESOURCE_GOORU_OID, resource.getGooruOid());
		param.put(API_END_POINT, settingService.getConfigSetting(ConfigConstants.GOORU_API_ENDPOINT, 0, TaxonomyUtil.GOORU_ORG_UID));
		this.getAsyncExecutor().executeRestAPI(param, settingService.getConfigSetting(ConfigConstants.GOORU_CONVERSION_RESTPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "/conversion/image", Method.POST.getName());
	}

	public void convertDoctoPdf(Resource resource, String mediaFileName, String fileName) {
		Job job = getJobService().createJob(resource);
		resource.setSourceReference(String.valueOf(job.getJobId()));
		String sourcePath = UserGroupSupport.getUserOrganizationNfsInternalPath() + Constants.UPLOADED_MEDIA_FOLDER + "/" + mediaFileName;
		String targetPath = resource.getOrganization().getNfsStorageArea().getInternalPath() + resource.getFolder();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(SOURCE_FILE_PATH, sourcePath);
		param.put(TARGET_FOLDER_PATH, targetPath);
		param.put(FILENAME, fileName);
		param.put(JOB_UID, String.valueOf(job.getJobId()));
		param.put(STATUS, job.getStatus());
		param.put(EVENTNAME, CONVERT_DOCUMENT_PDF);
		param.put(SESSIONTOKEN, UserGroupSupport.getSessionToken());
		param.put(API_END_POINT, settingService.getConfigSetting(ConfigConstants.GOORU_API_ENDPOINT, 0, TaxonomyUtil.GOORU_ORG_UID));
		param.put(GOORU_BUCKET, settingService.getConfigSetting(ConfigConstants.RESOURCE_S3_BUCKET, 0, TaxonomyUtil.GOORU_ORG_UID));
		param.put(CALL_BACK_URL, settingService.getConfigSetting(ConfigConstants.GOORU_API_ENDPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "v2/resource/" + resource.getGooruOid() + "?sessionToken=" + UserGroupSupport.getSessionToken());
		param.put(FOLDER_IN_BUCKET, resource.getFolder());
		this.getAsyncExecutor().executeRestAPI(param, settingService.getConfigSetting(ConfigConstants.GOORU_CONVERSION_RESTPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "/conversion/document-to-pdf", Method.POST.getName());
		// kafkaProducer.push(new JSONSerializer().serialize(param)); // FIX ME
		// TO-DO
	}

	public void downloadResourceImage(String repoPath, Resource resource, String webSrc) {

		String resourceTypeName = resource.getResourceType().getName();
		if (resource.getThumbnail() == null
				&& (resourceTypeName.equals(ResourceType.Type.EXAM.getType()) || resourceTypeName.equals(ResourceType.Type.PRESENTATION.getType()) || resourceTypeName.equals(ResourceType.Type.HANDOUTS.getType()) || resourceTypeName.equals(ResourceType.Type.TEXTBOOK.getType()))) {
			resource.setThumbnail("slides/slides1.jpg");
		}

		String thumbnail = GooruImageUtil.getFileName(GooruImageUtil.downloadWebResourceToFile(webSrc, repoPath, resource.getGooruOid()));

		if (thumbnail != null) {
			resource.setThumbnail(thumbnail);
		}

		resourceRepository.save(resource);
	}

	public void downloadAndGenerateThumbnails(Resource resource, String webSrc) {
		String repoPath = resource.getOrganization().getNfsStorageArea().getInternalPath() + resource.getFolder();
		downloadResourceImage(repoPath, resource, webSrc);
		generateThumbnails(resource, resource.getThumbnail());
	}

	public void downloadAndSendMsgToGenerateThumbnails(Resource resource, String webSrc) {
		String repoPath = resource.getOrganization().getNfsStorageArea().getInternalPath() + resource.getFolder();
		Map<String, Object> param = new HashMap<String, Object>();
		if (resource.getResourceType() != null && resource.getResourceType().getName().equalsIgnoreCase(ResourceType.Type.HANDOUTS.getType()) && webSrc == null) {
			resource.setThumbnail("slides/slides1.jpg");
			resourceRepository.save(resource);
			param.put(RESOURCE_FILE_PATH, repoPath + resource.getUrl());
			param.put(RESOURCE_GOORU_OID, resource.getGooruOid());
			this.getAsyncExecutor().executeRestAPI(param, settingService.getConfigSetting(ConfigConstants.GOORU_CONVERSION_RESTPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "/conversion/pdf-to-image", Method.POST.getName());
		} else {
			downloadResourceImage(repoPath, resource, webSrc);
			param.put(SOURCE_FILE_PATH, repoPath + resource.getThumbnail());
			param.put(TARGET_FOLDER_PATH, repoPath);
			param.put(THUMBNAIL, resource.getThumbnail());
			param.put(DIMENSIONS, getDimensions(resource));
			param.put(RESOURCE_GOORU_OID, resource.getGooruOid());
			this.getAsyncExecutor().executeRestAPI(param, settingService.getConfigSetting(ConfigConstants.GOORU_CONVERSION_RESTPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "/conversion/image", Method.POST.getName());
		}
		getAsyncExecutor().deleteFromCache(V2_ORGANIZE_DATA + resource.getUser().getPartyUid() + "*");
	}

	public void moveFileAndSendMsgToGenerateThumbnails(Resource resource, String fileName, Boolean isUpdateSlideResourceThumbnail) throws IOException {
		String repoPath = UserGroupSupport.getUserOrganizationNfsInternalPath();
		final String mediaFolderPath = repoPath + Constants.UPLOADED_MEDIA_FOLDER;
		final String contentGooruOid = resource.getGooruOid();

		String resourceImageFile = mediaFolderPath + "/" + fileName;
		File mediaImage = new File(resourceImageFile);
		if (!mediaImage.exists() || !mediaImage.isFile()) {
			return;
		}
		if (mediaImage.exists() && resource != null) {

			// get resource internal path
			repoPath = resource.getOrganization().getNfsStorageArea().getInternalPath();

			File resourceFolder = new File(repoPath + "/" + resource.getFolder());
			if (!resourceFolder.exists()) {
				resourceFolder.mkdir();
			}
			File newImage = new File(resourceFolder, contentGooruOid + "_" + fileName);
			if (resource.getThumbnail() != null && !resource.getThumbnail().startsWith(contentGooruOid) && !resource.getThumbnail().contains(GOORU_DEFAULT)) {
				// Collection image exists, but doesn't start with new ID.
				// migrate to new pattern
				File existingImage = new File(resourceFolder, resource.getThumbnail());
				existingImage.delete();
			}
			if (newImage.exists()) {
				newImage.delete();
			}
			FileUtils.moveFile(mediaImage, newImage);

			fileName = newImage.getName();
			String resourceTypeName = resource.getResourceType().getName();
			if (resourceTypeName.equals(ResourceType.Type.PRESENTATION.getType()) || resourceTypeName.equals(ResourceType.Type.HANDOUTS.getType()) || resourceTypeName.equals(ResourceType.Type.TEXTBOOK.getType())) {
				if (isUpdateSlideResourceThumbnail) {
					resource.setThumbnail(fileName);
				} else {
					resource.setThumbnail("slides/slides1.jpg");
				}

			} else {
				resource.setThumbnail(fileName);
			}
			this.setDefaultThumbnailImageIfFileNotExist(resource);
			if (resource.getUrl() != null && !resource.getUrl().toLowerCase().startsWith("http://") && !resource.getUrl().toLowerCase().startsWith("https://") && !resourceTypeName.equalsIgnoreCase(ResourceType.Type.RESOURCE.getType())
					&& !resourceTypeName.equalsIgnoreCase(ResourceType.Type.VIDEO.getType())) {
				if (!isUpdateSlideResourceThumbnail) {
					resource.setUrl(fileName);
				}
			}
		}

		resourceRepository.save(resource);
		if (resource.getResourceType() != null) {
			String indexType = RESOURCE;
			if (resource.getResourceType().getName().equalsIgnoreCase(SCOLLECTION)) {
				indexType = SCOLLECTION;
			}
			try {
				indexHandler.setReIndexRequest(resource.getGooruOid(), IndexProcessor.INDEX, indexType, null, false, false);
			} catch (Exception e) {
				LOGGER.debug("failed to index {}", e);
			}
		}
		sendMsgToGenerateThumbnails(resource);
	}

	public String getDimensions(Resource resource) {
		String resourceTypeName = resource.getResourceType().getName();
		if (resourceTypeName.equalsIgnoreCase(ResourceType.Type.CLASSPLAN.getType()) || resourceTypeName.equalsIgnoreCase(ResourceType.Type.CLASSBOOK.getType()) || resourceTypeName.equalsIgnoreCase(ResourceType.Type.SCOLLECTION.getType())) {
			return COLLECTION_THUMBNAIL_SIZES;
		} else if (resourceTypeName.equalsIgnoreCase(ResourceType.Type.ASSESSMENT_QUIZ.getType()) || resourceTypeName.equalsIgnoreCase(ResourceType.Type.ASSESSMENT_EXAM.getType())) {
			return QUIZ_THUMBNAIL_SIZES;
		} else {
			return RESOURCE_THUMBNAIL_SIZES;
		}
	}

	public void setDefaultThumbnailImageIfFileNotExist(Resource resource) {
		if (resource != null) {
			boolean setDefault = false;
			if (resource.getThumbnail() == null) {
				setDefault = true;
			} else if (!resource.getThumbnail().equalsIgnoreCase(GOORU_DEFAULT)) {
				String repoPath = resource.getOrganization().getNfsStorageArea().getInternalPath();
				File resourceFolder = new File(repoPath + "/" + resource.getFolder() + "/" + resource.getThumbnail());
				if (!resourceFolder.exists()) {
					setDefault = true;
				}
			}

			if (setDefault) {
				String resourceTypeName = resource.getResourceType().getName();
				if (resourceTypeName.equalsIgnoreCase(ResourceType.Type.CLASSPLAN.getType()) || resourceTypeName.equalsIgnoreCase(ResourceType.Type.CLASSBOOK.getType()) || resourceTypeName.equalsIgnoreCase(ResourceType.Type.ASSESSMENT_QUIZ.getType())
						|| resourceTypeName.equalsIgnoreCase(ResourceType.Type.ASSESSMENT_EXAM.getType())) {
					String defaultThumbnailImages = this.getSettingService().getConfigSetting(ConfigConstants.DEFAULT_IMAGES, 0, resource.getOrganization().getPartyUid());
					if (defaultThumbnailImages != null) {
						List<String> defaultThumbnailImageList = Arrays.asList(defaultThumbnailImages.split("\\s*,\\s*"));
						Integer randomNumber = new Random().nextInt(defaultThumbnailImageList.size());
						resource.setThumbnail((randomNumber != null ? defaultThumbnailImageList.get(randomNumber > 0 ? randomNumber - 1 : randomNumber) : null));
						LOGGER.info("default thumbnail  content id: " + resource.getContentId());
					}
				}
				resourceRepository.save(resource);
			}
		}
	}

	public Map<String, Object> getResourceMetaData(String url, String resourceTitle, boolean fetchThumbnail) {
		Map<String, Object> metaData = new HashMap<String, Object>();
		ResourceMetadataCo resourceFeeds = null;
		if (url != null && url.contains(VIMEO_VIDEO)) {
			resourceFeeds = getMetaDataFromVimeoVideo(url);
		} else if (url != null && url.contains(YOUTUBE_VIDEO)) {
			resourceFeeds = getYoutubeResourceFeeds(url, null);
		}
		String description = "";
		String title = "";
		String videoDuration = "";
		Set<String> images = new LinkedHashSet<String>();
		if (resourceFeeds == null || resourceFeeds.getUrlStatus() == 404) {
			Document doc = null;
			try {
				if (url != null && (url.contains("http://") || url.contains("https://"))) {
					doc = Jsoup.connect(url).timeout(6000).get();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (doc != null) {
				title = doc.title();
				Elements meta = doc.getElementsByTag(META);
				if (meta != null) {
					for (Element element : meta) {
						if (element.attr(NAME) != null && element.attr(NAME).equalsIgnoreCase(DESCRIPTION)) {
							description = element.attr(CONTENT);
							break;
						}
					}
				}
				metaData.put(DESCRIPTION, description);
				if (fetchThumbnail) {
					Elements media = doc.select("[src]");
					if (media != null) {
						for (Element src : media) {
							if (src.tagName().equals(IMG)) {
								images.add(src.attr("abs:src"));
							}
							if (images.size() >= SUGGEST_IMAGE_MAX_SIZE) {
								break;
							}
						}
					}
				}
			}
		} else {
			title = resourceFeeds.getTitle();
			description = resourceFeeds.getDescription();
			videoDuration = resourceFeeds.getDuration().toString();
		}
		if (fetchThumbnail) {
			if (resourceFeeds != null && resourceFeeds.getThumbnail() != null) {
				images.add(resourceFeeds.getThumbnail());
			}
			metaData.put(IMAGES, images);
		}
		metaData.put(TITLE, title);
		metaData.put(DESCRIPTION, description);
		metaData.put(DURATION, videoDuration);
		return metaData;
	}

	public static String getYoutubeVideoId(String url) {
		String pattern = "youtu(?:\\.be|be\\.com)/(?:.*v(?:/|=)|(?:.*/)?)([a-zA-Z0-9-_]{11}+)";
		String videoId = null;
		Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = compiledPattern.matcher(url);
		if (matcher != null) {
			while (matcher.find()) {
				videoId = matcher.group(1);
			}
		}
		return videoId;
	}

	public void createThumbnailForCode(String codeId, String sourceFilePath, String destinationFilePath, String dimensions) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(SOURCE_FILE_PATH, sourceFilePath);
		param.put(TARGET_FOLDER_PATH, destinationFilePath);
		param.put(DIMENSIONS, dimensions);
		param.put(CODE_UID, codeId);
		Code code = this.taxonomyRespository.findCodeByCodeUId(codeId);
		param.put(API_END_POINT, settingService.getConfigSetting(ConfigConstants.GOORU_API_ENDPOINT, 0, TaxonomyUtil.GOORU_ORG_UID));
		s3ResourceApiHandler.resetS3UploadFlag(code);
		this.getAsyncExecutor().executeRestAPI(param, settingService.getConfigSetting(ConfigConstants.GOORU_CONVERSION_RESTPOINT, 0, TaxonomyUtil.GOORU_ORG_UID) + "/conversion/image", Method.POST.getName());
	}

	public static ResourceMetadataCo getYoutubeResourceFeeds(String url, ResourceMetadataCo resourceFeeds) {
		if (resourceFeeds == null) {
			resourceFeeds = new ResourceMetadataCo();
		}
		int status = 404;
		resourceFeeds.setUrlStatus(status);
		long start = System.currentTimeMillis();
		String videoId = getYoutubeVideoId(url);
		if (videoId != null) {
			String requestURL = "https://www.googleapis.com/youtube/v3/videos?id=" + videoId + "&key=" + ConfigProperties.getGoogleApiKey() + "&part=snippet,contentDetails,statistics,status";
			try {
				ClientResource clientResource = new ClientResource(requestURL);
				Representation representation = new ClientResource(requestURL).get();
				Map<String, Object> data = JsonDeserializer.deserialize(representation.getText(), new TypeReference<Map<String, Object>>() {
				});
				Map<String, Object> pageInfo = (Map<String, Object>) data.get(PAGE_INFO);
				Integer totalResults = (Integer) pageInfo.get(TOTAL_RESULTS);
				if (totalResults > 0) {
					status = 200;
				}
				if (status == 200) {
					LOGGER.info("youtube api response code: " + status);
					List<Map<String, Object>> items = (List<Map<String, Object>>) data.get(ITEMS);
					Map<String, Object> item = items.get(0);
					Map<String, Object> snippet = (Map<String, Object>) item.get(SNIPPET);
					resourceFeeds.setTitle((String) snippet.get(TITLE));
					resourceFeeds.setDescription((String) snippet.get(DESCRIPTION));
					if (item.get(STATISTICS) != null) {
						Map<String, Object> statistics = (Map<String, Object>) item.get(STATISTICS);
						resourceFeeds.setFavoriteCount(Long.parseLong((String) statistics.get(FAVORITE_COUNT)));
						resourceFeeds.setViewCount(Long.parseLong((String) statistics.get(VIEW_COUNT)));
					}

					if (item.get(CONTENT_DETAILS) != null) {
						Map<String, Object> contentDetails = (Map<String, Object>) item.get(CONTENT_DETAILS);
						PeriodFormatter formatter = ISOPeriodFormat.standard();
						Period period = formatter.parsePeriod((String) contentDetails.get(DURATION));
						Seconds seconds = period.toStandardSeconds();
						resourceFeeds.setDuration((long) seconds.getSeconds());
					}
					resourceFeeds.setUrlStatus(status);
					return resourceFeeds;
				}
			} catch (Exception ex) {
				LOGGER.error("getYoutubeResourceFeeds: " + ex);
				LOGGER.error("Total time for get youtube api data :" + (System.currentTimeMillis() - start));
			}
		}
		return resourceFeeds;
	}

	public void moveAttachment(Resource newResource, Resource resource) {
		try {
			File parentFolderFile = new File(UserGroupSupport.getUserOrganizationNfsInternalPath() + resource.getFolder());
			if (!parentFolderFile.exists()) {
				parentFolderFile.mkdirs();
			}
			String fileExtension = org.apache.commons.lang.StringUtils.substringAfterLast(newResource.getAttach().getMediaFilename(), ".");
			if (BaseUtil.supportedDocument().containsKey(fileExtension)) {
				this.convertDoctoPdf(resource, newResource.getAttach().getMediaFilename(), newResource.getAttach().getFilename());
			} else {
				File file = new File(UserGroupSupport.getUserOrganizationNfsInternalPath() + Constants.UPLOADED_MEDIA_FOLDER + "/" + newResource.getAttach().getMediaFilename());
				if (fileExtension.equalsIgnoreCase(PDF)) {
					PDDocument doc = PDDocument.load(file);
					ResourceInfo resourceInfo = this.resourceRepository.findResourceInfo(resource.getGooruOid());
					if (resourceInfo == null) {
						resourceInfo = new ResourceInfo();
					}
					resourceInfo.setResource(resource);
					resourceInfo.setNumOfPages(doc.getNumberOfPages());
					resourceInfo.setLastUpdated(resource.getLastModified());
					this.resourceRepository.save(resourceInfo);
					resource.setResourceInfo(resourceInfo);
				}

				file.renameTo(new File(UserGroupSupport.getUserOrganizationNfsInternalPath() + resource.getFolder() + "/" + newResource.getAttach().getFilename()));
				if (newResource.getThumbnail() == null) {
					this.downloadAndSendMsgToGenerateThumbnails(resource, null);
				}
				this.getAsyncExecutor().updateResourceFileInS3(resource.getFolder(), resource.getOrganization().getNfsStorageArea().getInternalPath(), resource.getGooruOid(), UserGroupSupport.getSessionToken());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ResourceMetadataCo getMetaDataFromVimeoVideo(String url) {
		ResourceMetadataCo resourceMetadataCo = null;
		try {
			String id = org.apache.commons.lang.StringUtils.substringAfterLast(url, "/");
			if (org.apache.commons.lang.StringUtils.isNumeric(id)) {
				ClientResource clientResource = new ClientResource("http://vimeo.com/api/v2/video/" + id + ".json");
				List<Map<String, String>> data = JsonDeserializer.deserialize(clientResource.get().getText(), new TypeReference<List<Map<String, String>>>() {
				});
				if (data != null && data.size() > 0) {
					Map<String, String> resourceFeed = data.get(0);
					if (resourceFeed != null) {
						resourceMetadataCo = new ResourceMetadataCo();
						resourceMetadataCo.setTitle(resourceFeed.get(TITLE));
						resourceMetadataCo.setDescription(resourceFeed.get(DESCRIPTION));
						resourceMetadataCo.setThumbnail(resourceFeed.get(THUMBNAIL));
						resourceMetadataCo.setDuration(Long.parseLong(resourceFeed.get(DURATION)));
						resourceMetadataCo.setUrlStatus(200);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to fetch the data from vimeo feeds");
		}
		return resourceMetadataCo;
	}

	public SettingService getSettingService() {
		return settingService;
	}

	public MediaService getMediaService() {
		return mediaService;
	}

	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}

	public AsyncExecutor getAsyncExecutor() {
		return asyncExecutor;
	}

	public JobService getJobService() {
		return jobService;
	}
}