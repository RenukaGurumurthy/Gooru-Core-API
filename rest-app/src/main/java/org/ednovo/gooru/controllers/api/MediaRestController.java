package org.ednovo.gooru.controllers.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ednovo.gooru.application.util.GooruImageUtil;
import org.ednovo.gooru.application.util.ResourceProcessor;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.FileMeta;
import org.ednovo.gooru.core.api.model.UserGroupSupport;
import org.ednovo.gooru.core.application.util.RequestUtil;
import org.ednovo.gooru.core.application.util.ServerValidationUtils;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.exception.BadRequestException;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.resource.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = { "/media" })
public class MediaRestController extends BaseController implements ConstantProperties {

	private static final Logger logger = LoggerFactory.getLogger(MediaRestController.class);

	@Autowired
	private MediaService mediaService;

	@Autowired
	private GooruImageUtil gooruImageUtil;

	@Autowired
	private ResourceProcessor resourceProcessor;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_MEDIA_ADD })
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView uploadMedia(HttpServletRequest request, @RequestParam(value = UPLOAD_FILENAME) String fileName, @RequestParam(value = IMAGE_URL, required = false) String imageURL, @RequestParam(value = SESSIONTOKEN, required = false) String sessionToken,
			@RequestParam(value = RESIZE, required = false, defaultValue = FALSE) boolean resize, @RequestParam(value = WIDTH, required = false, defaultValue = SEVEN_HUNDRED) int width, @RequestParam(value = HEIGHT, required = false, defaultValue = FIVE_TWENTYFIVE) int height, HttpServletResponse response)
			throws Exception {
		request.setAttribute(PREDICATE, MEDIA_UPLOAD_IMG);
		response.setContentType(APPLICATION_JSON);
		Map<String, Object> formField = RequestUtil.getMultipartItems(request);
		if (formField == null) {
			throw new BadRequestException("Invalid Content Type " + request.getContentType());
		}
		String fileExtension = StringUtils.substringAfterLast(fileName, ".");
		if (fileExtension.isEmpty()) {
			fileExtension = PNG;
		}
		fileName = UUID.randomUUID().toString() + "." + fileExtension;

		FileMeta fileMeta = getMediaService().handleFileUpload(fileName, imageURL, formField, resize, width, height);

		List<FileMeta> files = new ArrayList<FileMeta>();
		files.add(fileMeta);

		return toModelAndView(files, FORMAT_JSON);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_MEDIA_ADD })
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@RequestMapping(method = RequestMethod.POST, value = "/{gooruImageId}")
	public ModelAndView uploadUpdatedMedia(HttpServletRequest request, @PathVariable(GOORU_CONTENT_ID) String gooruContentId, @RequestParam(value = UPLOAD_FILENAME) String fileName, @RequestParam(value = IMAGE_URL, required = false) String imageURL,
			@RequestParam(value = SESSIONTOKEN, required = false) String sessionToken, @RequestParam(value = RESIZE, required = false, defaultValue = FALSE) boolean resize, @RequestParam(value = WIDTH, required = false, defaultValue = SEVEN_HUNDRED) int width,
			@RequestParam(value = HEIGHT, required = false, defaultValue = FIVE_TWENTYFIVE) int height, HttpServletResponse response) throws Exception {

		Map<String, Object> formField = RequestUtil.getMultipartItems(request);

		getMediaService().handleFileUpload(fileName, imageURL, formField, resize, width, height);

		ModelAndView jsonmodel = new ModelAndView(REST_MODEL);
		jsonmodel.addObject(MODEL, Constants.UPLOADED_MEDIA_FOLDER + "/" + fileName);
		return jsonmodel;
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_MEDIA_UPDATE })
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@RequestMapping(method = RequestMethod.PUT, value = "/{id}/crop")
	public void cropImage(@PathVariable(value = ID) String gooruImageId, @RequestParam(value = XPOSITION) int xPosition, @RequestParam(value = YPOSITION) int yPosition, @RequestParam(value = WIDTH) int width, @RequestParam(value = HEIGHT) int height,
			@RequestParam(value = CROP_ENGINE, required = false, defaultValue = IMG_MAGICK) String cropEngine, HttpServletRequest request, HttpServletResponse response) {

		File classplanDir = new File(UserGroupSupport.getUserOrganizationNfsInternalPath() + Constants.UPLOADED_MEDIA_FOLDER);

		String fileName = gooruImageId;

		File file = new File(classplanDir.getPath() + "/" + fileName);

		if (fileName != null && file.exists()) {

			try {
				if (cropEngine.equalsIgnoreCase(IMG_MAGICK)) {
					getGooruImageUtil().cropImageUsingImageMagick(file.getPath(), width, height, xPosition, yPosition, file.getPath());
				} else {
					getGooruImageUtil().cropImageUsingImageMagick(file.getPath(), width, height, xPosition, yPosition, file.getPath());
				}
			} catch (Exception exception) {
				logger.error("Cannot crop Image : " + exception.getMessage());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

		} else {
			response.setStatus(404);
		}
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_MEDIA_UPDATE })
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@RequestMapping(method = RequestMethod.PUT, value = "/resource/thumbnail")
	public void updateResourceThumbnail(@RequestParam(value = RESOURCE_GOORU_OID) String resourceGooruOid, @RequestParam(value = THUMBNAIL) String thumbnail, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setAttribute(PREDICATE, "resource.update_thumbanil");
		// logger.error("Thumbnail" + thumbnail);
		resourceProcessor.updateResourceToS3WithNewSession(resourceGooruOid);
		if (thumbnail != null) {
			// FIX ME
			// resourceProcessor.updateResourceThumbnail(thumbnail,
			// resourceGooruOid);
		}
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_MEDIA_UPDATE })
	@RequestMapping(value="/crop", method=RequestMethod.PUT)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public ModelAndView cropImage(@RequestParam (value=IMAGE_URL) String imageUrl, @RequestParam(value = XPOSITION) int xPosition, @RequestParam(value = YPOSITION) int yPosition, @RequestParam(value = WIDTH) int width, @RequestParam(value = HEIGHT) int height ,HttpServletRequest request, HttpServletResponse response) throws Exception{
		String filePath = StringUtils.substringAfterLast(imageUrl, UserGroupSupport.getUserOrganizationCdnDirectPath());
		StringBuffer destPath = new StringBuffer(UserGroupSupport.getUserOrganizationNfsInternalPath());
		StringBuffer fileName = new StringBuffer(UUID.randomUUID().toString());
		StringBuffer mediaPath = new StringBuffer(Constants.UPLOADED_MEDIA_FOLDER);
		StringBuffer url = new StringBuffer(UserGroupSupport.getUserOrganizationNfsRealPath());
		String fileExtension = StringUtils.substringAfterLast(imageUrl, DOT);
		ServerValidationUtils.reject(!fileExtension.isEmpty(), GL0006, 400, FILE_EXTENSION);
		fileName.append(DOT).append(fileExtension);
		mediaPath.append('/').append(fileName);
		destPath.append(mediaPath);
		getGooruImageUtil().cropImageUsingImageMagick(filePath, width, height, xPosition, yPosition, destPath.toString());
		Map<String,String> json = new HashMap<String,String>();
		json.put(MEDIA_FILE_NAME, fileName.toString());
		json.put(URL, url.append(mediaPath).toString());
		return toModelAndView(json , FORMAT_JSON);
	}
	
	public GooruImageUtil getGooruImageUtil() {
		return gooruImageUtil;
	}

	public MediaService getMediaService() {
		return mediaService;
	}

	public void setMediaService(MediaService mediaService) {
		this.mediaService = mediaService;
	}

}
