package interfaceApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.file.fileHelper;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.security.codec;
import common.java.session.session;
import common.java.string.StringHelper;
import common.java.time.timeHelper;
import net.coobird.thumbnailator.Thumbnails;
import sun.misc.BASE64Decoder;

public class Theme {
	private GrapeTreeDBModel gDbModel;
	private JSONObject userInfo;
	private String userid;
	private JSONArray eleArray = new JSONArray();
	private JSONObject _obj = new JSONObject();
	private String pkString;

	public Theme() {
		
		gDbModel = new GrapeTreeDBModel();
		//数据模型绑定
		GrapeDBDescriptionModel  gdb = new GrapeDBDescriptionModel ();
		gdb.importDescription( appsProxy.tableConfig("Theme"));
		gDbModel.descriptionModel(gdb);

		//权限模型绑定
		GrapePermissionsModel gperm = new GrapePermissionsModel();
		gperm.importDescription(appsProxy.tableConfig("Theme"));
		gDbModel.permissionsModel(gperm);
		
		pkString = gDbModel.getPk();

		//用户信息
        userInfo = (new session()).getDatas();
        //用户的id
        if (userInfo != null && userInfo.size() > 0) {
			userid = (String) userInfo.getPkValue(pkString);
		}
        
        //开启检查模式
        gDbModel.enableCheck();
	}

	/**
	 * 获取屏幕id
	 * 
	 * @param tid 主题id
	 * @return
	 */
	protected List<String> getScreenid(String tid) {
		List<String> list = new ArrayList<String>();
		Mode mode = new Mode();
		JSONObject ModeInfo = Find(tid);
		String mid = "";
		if (ModeInfo != null && ModeInfo.size() > 0) {
			mid = ModeInfo.getString("mid");
		}
		if (!mid.equals("")) {
			mid = mode.getSid(mid);
			list.add(mid);
		}
		return list;
	}

	/**
	 * 新增主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ScreenInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public String AddTheme(String ThemeInfo) {
		
		Object info = "";
		if(StringHelper.InvaildString(ThemeInfo)){
			return rMsg.netMSG(2, "参数为空");
		}
		//解码
		ThemeInfo = codec.DecodeFastJSON(ThemeInfo);
		
		JSONObject obj = JSONObject.toJSON(ThemeInfo);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		//判断内容信息
		String content = obj.getString("content");
		if(!StringHelper.InvaildString(content)){
			//在进行解码
			content = codec.DecodeFastJSON(content);
			obj.remove("content");
			obj.put("content", content);
		}
		
		obj.put("userid", userid);
		info = gDbModel.data(obj).autoComplete().insertOnce();
		obj = Find(info.toString());
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(3, "该主题不存在");
		}
		return resultJSONInfo(obj);
	}

	/**
	 * 修改主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param id
	 * @param ScreenInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public String UpdateTheme(String id, String ThemeInfo) {
		String thumbnail = "";
		int code = 99;
		if(StringHelper.InvaildString(ThemeInfo)){
			return rMsg.netMSG(3, "参数信息为空");
		}
		//解码
		ThemeInfo = codec.DecodeFastJSON(ThemeInfo);
		
		JSONObject obj = JSONObject.toJSON(ThemeInfo);
		if (StringHelper.InvaildString(id)) {
			return rMsg.netMSG(3, "无效主题id");
		}
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (obj != null && obj.size() > 0) {
			if (obj.containsKey("thumbnail")) {
				thumbnail = obj.getString("thumbnail");
				thumbnail = CreateImage(thumbnail);
				obj.put("thumbnail", thumbnail);
			}
			gDbModel.eq(pkString, id);
			code = (gDbModel.dataEx(obj).updateEx()) ? 0 : 99;
			if (code == 0) {
				String content = getTheme(id);
				broadManage.broadEvent(id, 0, content);
			}
		}
		return FindTheme(id);
	}

	/**
	 * 修改主题，主题缩略图通过网页url获取截图  //TODO 缩列图测试
	 * 
	 * @param id
	 * @param ThemeInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String UpdateThemeURL(String id, String ThemeInfo) {
		String thumbnail = "";
		int code = 99;
		if (StringHelper.InvaildString(id)) {
			return rMsg.netMSG(3, "无效主题id");
		}
		if(StringHelper.InvaildString(ThemeInfo)){
			return rMsg.netMSG(3, "参数信息为空");
		}
		//解码
		ThemeInfo = codec.DecodeFastJSON(ThemeInfo);
		
		JSONObject obj = JSONObject.toJSON(ThemeInfo);
		JSONObject object = new JSONObject();
		
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (obj != null && obj.size() > 0) {
			gDbModel.eq(pkString, id);
			code = (gDbModel.dataEx(obj).updateEx()) ? 0 : 99;
			if (code == 0) {
				// 生成缩略图
				gDbModel.clear();
				if (obj.containsKey("thumbnail")) {
					thumbnail = obj.getString("thumbnail");
					thumbnail = getWebpageThumbnail(thumbnail);
					object.put("thumbnail", thumbnail);
				} else {
					object.put("thumbnail", "");
				}
				if (object != null && object.size() > 0) {
					code = (gDbModel.eq(pkString, id).data(object).updateEx()) ? 0 : 99;
				}
			}
		}
		return FindTheme(id);
	}

	private String getFlePath(String key) {
		String value = "";
		JSONObject object = JSONObject.toJSON(appsProxy.configValue().getString("other"));
		if (object != null && object.size() > 0) {
			value = object.getString(key);
		}
		return value;
	}

	/**
	 * 获取网页缩略图
	 * 
	 * @param url
	 * @return
	 */
	private String getWebpageThumbnail(String url) {
		String thumbnail = "";
//		String host = getFlePath("APIHost");
//		String appid = getFlePath("appid");
		int width = Integer.parseInt(getFlePath("width"));
		int height = Integer.parseInt(getFlePath("height"));
		if (url != null && !url.equals("") && !url.equals("null")) {
			if (width > 0 && height > 0) {
				// String urls = host + "/" + appid +
				// "/GrapeCutImage/ImageOpeation/getNetImage/" + url + "/int:" +
				// width
				// + "/int:" + height;
				// System.out.println(urls);
				// thumbnail = request.Get(urls);
				thumbnail = (String) appsProxy.proxyCall("/GrapeCutImage/ImageOpeation/getNetImage/" + url + "/int:" + width + "/int:" + height);
				thumbnail = generateImage(thumbnail);
			}
		}
		return thumbnail;
	}

	@SuppressWarnings({ "unused" })
	private String generateImages(String thumbnail) {
		String path = "";
		String ext = "jpg";
//		if (thumbnail != null && !thumbnail.equals("") && !thumbnail.equals("null")) {
		if (!StringHelper.InvaildString(thumbnail) || !thumbnail.equals("null".trim())) {
			if (thumbnail.contains("data:image/")) {
				ext = thumbnail.substring(thumbnail.indexOf("data:image/") + 11, thumbnail.indexOf(";base64,"));
				thumbnail = thumbnail.substring(thumbnail.lastIndexOf(",") + 1);
			}
			path = getFilepath();
			String Date = timeHelper.stampToDate(timeHelper.nowMillis()).split(" ")[0];
			path = path + "\\" + Date + "\\" + timeHelper.nowSecond() + "." + ext;
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				byte[] bs = decoder.decodeBuffer(thumbnail);
				for (int i = 0; i < bs.length; i++) {
					if (bs[i] < 0) {
						bs[i] += 256;
					}
				}
				OutputStream out = new FileOutputStream(path);
				out.write(bs);
				out.flush();
				out.close();
			} catch (Exception e) {
				nlogger.logout(e);
				path = "";
			}
		}
		return getImgUrl(path);
	}

	private String generateImage(String thumbnail) {
		String path = "";
		String ext = "jpg";
		thumbnail = codec.DecodeHtmlTag(thumbnail);
		if (thumbnail != null) {
			if (thumbnail.contains("data:image/")) {
				ext = thumbnail.substring(thumbnail.indexOf("data:image/") + 11, thumbnail.indexOf(";base64,"));
				thumbnail = thumbnail.substring(thumbnail.lastIndexOf(",") + 1);
			}
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				path = getFlePath("filepath");
				String Date = timeHelper.stampToDate(timeHelper.nowMillis()).split(" ")[0];
				String dir = path + "\\" + Date;
				if (!new File(dir).exists()) {
					new File(dir).mkdir();
				}
				path = dir + "\\" + timeHelper.nowMillis() + "." + ext;
				byte[] bytes = decoder.decodeBuffer(thumbnail);
				if (fileHelper.createFile(path)) {
					OutputStream out = new FileOutputStream(path);
					out.write(bytes);
					out.flush();
					out.close();
				}
			} catch (Exception e) {
				nlogger.logout(e);
				path = "";
			}
		}
		return getImgUrl(path);
	}

	/**
	 * 按比例获取缩略图
	 * 
	 * @param image
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getThumbnail(String image) {
		String outpath = "";
		outpath = image.substring(0, image.lastIndexOf("."));
		try {
			Thumbnails.of(image).scale(Double.parseDouble(getThumbnailConfig("ImageScale")))
					.outputFormat(getThumbnailConfig("ImgType"))
					.outputQuality(Float.parseFloat(getThumbnailConfig("ImgQuality"))).toFile("outpath");
		} catch (Exception e) {
			nlogger.logout(e);
			outpath = "";
		}
		return (!outpath.equals("")) ? getImgUrl(outpath) : "";
	}

	/**
	 * 获取大屏宽度和高度
	 * 
	 * @param url
	 * @return {"width":0,"height":0}
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private JSONObject getScreenWidth(String url) {
		JSONObject tempObj = null, object = new JSONObject();
		int width = 400, height = 200;
		if (url != null && !url.equals("") && !url.equals("null")) {
			String[] temp = url.split("#");
			temp = (temp != null && temp.length >= 2) ? temp[1].split("/") : null;
//			sid = (temp != null && temp.length >= 2) ? temp[0] : null;
//			if (sid != null && !sid.equals("") && !sid.equals("null")) {
				// 获取大屏的宽和高
//				tempObj = new Screen().Find(sid);
//			}
		}
		if (tempObj != null && tempObj.size() > 0) {
			width = Integer.parseInt(tempObj.getString("width"));
			height = Integer.parseInt(tempObj.getString("height"));
		}
		object.put("width", width);
		object.put("height", height);
		return object;
	}

	/**
	 * 删除主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteTheme(String ids) {
		long tipcode = 99;
		int l = 0;
		if (StringHelper.InvaildString(ids) || ids.equals("null".trim())) {
//			if (ids == null || ids.equals("") || ids.equals("null")) {//为啥判断两遍
				return rMsg.netMSG(3, "无效主题id");
//			}
		}
		if (ids != null && !ids.equals("")) {
			String[] value = ids.split(",");
			l = value.length;
			gDbModel.or();
			for (String id : value) {
				gDbModel.eq(pkString, id);
			}
			tipcode = gDbModel.deleteAll();
		}
		return resultMessage(tipcode == l ? 0 : 99, "删除成功");
	}

	/**
	 * 分页显示主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param idx
	 * @param PageSize
	 * @param condString
	 * @return
	 *
	 */
	public String PageTheme(int idx, int PageSize, String condString) {
		JSONArray array = null;
		long total = 0, totalSize = 0;
		JSONArray condArray = JSONArray.toJSONArray(condString);
		if (idx <= 0 || PageSize <= 0) {
			return rMsg.netMSG(3, "当前页码小于0或者每页最大量小于0");
		}
		if (condArray != null && condArray.size() > 0) {
			gDbModel.where(condArray);
		} else {
			return pageShow(new JSONArray(), total, totalSize, idx, PageSize);
		}
//		if (sid != null && !sid.equals("")) {
			gDbModel.eq("userid", userid);
			array = gDbModel.dirty().mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode")
					.page(idx, PageSize);
			total = gDbModel.dirty().count();
			totalSize = gDbModel.pageMax(PageSize);
//		}
		return pageShow(getElement(array), total, totalSize, idx, PageSize);
	}

	/**
	 * 显示所有的主题信息，非管理员用户只能查看与自己相关的大屏信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param idx
	 * @param PageSize
	 * @param condString
	 * @return
	 *
	 */
	public String ShowTheme() {
		JSONArray array = null;
//		if (sid != null && !sid.equals("")) {
//			if (model.isAdmin() == false) {
				gDbModel.eq("userid", userid);
//			}
			array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").select();
//		}
		return resultArray(getElement(array));
	}

	/**
	 * 显示详细信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param info
	 * @return
	 *
	 */
	public String FindTheme(String info) {
		return resultJSONInfo(Find(info));
	}

	/**
	 * 
	 * [通过主题的id来获取元素的信息] <br> 
	 *  
	 * @author [南京喜成]<br>
	 * @param info 这是主题的id
	 * @return <br>
	 */
	public JSONObject Find(String info) {
		Element element = new Element();
		String eid = "";
		JSONObject ElementInfo = new JSONObject();
//		if (info == null || info.equals("") || info.equals("null")) {
		if (StringHelper.InvaildString(info) || info.equals("null".trim())) {
			return new JSONObject();
		}
		JSONObject obj = gDbModel.eq(pkString, info)
				.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,timediff").limit(1).find();
		// 获取元素id
		if (obj != null && obj.size() > 0) {
			eid = obj.getString("content");//元素里面的内容，是一个数组
//			if (eid != null && !eid.equals("") && eid.length() > 0) {
			if (!StringHelper.InvaildString(eid) && eid.length() > 0) {
				ElementInfo = element.GetElementInfo(eid);
				obj = element.FillElement(obj, ElementInfo);
			}
		}
		return obj;
	}

	/**
	 * 获取指定临时主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @return {}
	 *
	 */
	public String getTempTheme(String temptid) {
		JSONObject object = null;
		object = gDbModel.eq(pkString, temptid).eq("type", 0).limit(1).find();
		object = getInfo(object);
		return object.toString();
	}

	/**
	 * 获取指定永久主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @return {}
	 *
	 */
	public String getTheme(String temptid) {
		JSONObject object = null;
		try {
			object = gDbModel.eq(pkString, temptid).limit(1).find();
			object = getInfo(object);
		} catch (Exception e) {
			nlogger.logout(e);
			object = null;
		}
		return (object != null) ? object.toString() : "";
	}

	public String getThemeById(String temptid) {
		JSONObject object = null;
//		if (temptid != null && !temptid.equals("")) {
		if (!StringHelper.InvaildString(temptid)) {
			try {
				object = gDbModel.eq(pkString, temptid).limit(1).find();
				object = getInfos(object);
			} catch (Exception e) {
				nlogger.logout(e);
				object = null;
			}
		}
		return (object != null) ? object.toString() : "";
	}

	/**
	 * 获取所有永久主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @return
	 *
	 */
	public String getAllTheme() {
		JSONArray array = gDbModel.eq("type", 1).select();
		return SelectModeEle(array).toString();
	}

	/**
	 * 获取所有临时主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @return
	 *
	 */
	public String getAllTempTheme() {
		JSONArray array = gDbModel.eq("type", 0).select();
		return SelectModeEle(array).toString();
	}

	/**
	 * 新增主题信息，若元素，区域不存在，则同时新增元素，区域信息
	 * 
	 * @param Info
	 * @return
	 */
	public String InsertTheme(String Info) {
		Object info = "";
		JSONObject ThemeInfo = new JSONObject();
		// JSONArray blockArray = new JSONArray();
		// JSONArray elementArray = new JSONArray();
		JSONObject object = JSONObject.toJSON(Info);
		if (object != null && object.size() > 0) {
			eleArray = JSONArray.toJSONArray(object.getString("element"));
			// 获取区域信息
			getBlockInfo(object);
			// 获取元素信息
			getElementInfo();
			// 获取主题信息
			ThemeInfo = getThemeInfo(object);
			// 新增主题信息
			// ThemeInfo = model.AddMap(getInitData(),
			// ThemeInfo.toJSONString());
			if (ThemeInfo == null || ThemeInfo.size() <= 0) {
				return rMsg.netMSG(2, "参数异常");
			}
			info = gDbModel.data(ThemeInfo).autoComplete().insertOnce();
		}
		return getTheme(info.toString());
	}

	protected String getTidByeid(String eid) {
		String tid = "";
		JSONObject object = gDbModel.like("content", eid).find();
		if (object != null && object.size() > 0) {
			tid = (String) object.getPkValue(pkString);
		}
		return tid;
	}

	/**
	 * 修改主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param tid
	 * @param themeInfo
	 * @return
	 *
	 */
	public String EditTheme(String tid, String Info) {
		String ScreenInfo = "";
		int code = 0;
		Block block = new Block();
		Element element = new Element();
		JSONObject ThemeInfo = new JSONObject();
		JSONArray blockArray = new JSONArray();
		JSONArray elementArray = new JSONArray();
		JSONObject object = JSONObject.toJSON(Info);
//		if (tid == null || tid.equals("") || tid.equals("null")) {
		if (StringHelper.InvaildString(tid) || tid.equals("null".trim())) {
			return rMsg.netMSG(3, "无效主题id");
		}
		if (object == null || object.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (object != null && object.size() > 0) {
			eleArray = JSONArray.toJSONArray(object.getString("element"));
			blockArray = getBlockInfo(object);
			elementArray = getElementInfo();
			ThemeInfo = getThemeInfo(object);
			// 修改区域信息
			block.UpdateAllBlock(blockArray);
			// 修改元素信息
			element.UpdateAllElement(elementArray);
			// 修改主题信息
			code = gDbModel.eq(pkString, tid).data(ThemeInfo).updateEx() ? 0 : 99;
			if (code == 0) {
				broadManage.broadEvent(tid, 0, ScreenInfo);
			}
		}
		ScreenInfo = getTheme(tid);
		return ScreenInfo;
	}

	/**
	 * 解析json，得到主题信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param object
	 * @param eleInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getThemeInfo(JSONObject object) {
		JSONObject ThemeInfo = new JSONObject();
		JSONObject tempobj;
		String type, eid = "", tempid;
		ThemeInfo.put("name", object.getString("name"));
		ThemeInfo.put("mid", object.getString("mid"));
		ThemeInfo.put("userid", object.getString("userid"));
		String thumb = object.getString("thumbnail");
		ThemeInfo.put("thumbnail", CreateImage(thumb));
		type = object.getString("type");
		if (type.contains("$numberLong")) {
			type = JSONObject.toJSON(type).getString("$numberLong");
		}
		ThemeInfo.put("type", Long.parseLong(type));
		for (Object object2 : eleArray) {
			tempobj = (JSONObject) object2;
			tempid = tempobj.getString(pkString);
			if (!tempid.equals("")) {
				eid += tempid + ",";
			}
		}
		if (eid != null && eid.length() > 0) {
			ThemeInfo.put("content", StringHelper.fixString(eid, ','));
		}
		return ThemeInfo;
	}

	/**
	 * base64图片文件写入磁盘，获取文件url
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param thumbnail
	 * @return
	 *
	 */
	public String CreateImage(String thumbnail) {
		String path = CreateImages(thumbnail);
		return getImgUrl(path);
	}

	/**
	 * base64图片写到磁盘
	 * 
	 * @param thumbnail
	 * @return
	 */
	public String CreateImages(String thumbnail) {
		String path = "";
		String ext = "jpg";
		if (thumbnail != null && !thumbnail.equals("") && !thumbnail.equals("null")) {
			thumbnail = codec.DecodeHtmlTag(thumbnail);
			if (thumbnail != null) { // 图像数据为空
				if (thumbnail.contains("data:image/")) {
					ext = thumbnail.substring(thumbnail.indexOf("data:image/") + 11, thumbnail.indexOf(";base64,"));
					thumbnail = thumbnail.substring(thumbnail.lastIndexOf(",") + 1);
				}
				BASE64Decoder decoder = new BASE64Decoder();
				try {
					path = getFilepath();
					String Date = timeHelper.stampToDate(timeHelper.nowMillis()).split(" ")[0];
					path = path + "\\" + Date + "\\" + timeHelper.nowSecond() + "." + ext;
					byte[] bytes = decoder.decodeBuffer(thumbnail);
					if (fileHelper.createFile(path)) {
						OutputStream out = new FileOutputStream(path);
						out.write(bytes);
						out.flush();
						out.close();
					}
				} catch (Exception e) {
					nlogger.logout(e);
					path = "";
				}
			}
		}
		return path;
	}

	private String getImgUrl(String imageURL) {
		int i = 0;
		if (imageURL != null && !imageURL.equals("") && !imageURL.equals("null")) {
			if (imageURL.contains("File//upload")) {
				i = imageURL.toLowerCase().indexOf("file//upload");
				imageURL = "\\" + imageURL.substring(i);
			}
			if (imageURL.contains("File\\upload")) {
				i = imageURL.toLowerCase().indexOf("file\\upload");
				imageURL = "\\" + imageURL.substring(i);
			}
			if (imageURL.contains("File/upload")) {
				i = imageURL.toLowerCase().indexOf("file/upload");
				imageURL = "\\" + imageURL.substring(i);
			}
			imageURL = getFileUrl() + imageURL;
		}
		return imageURL;
	}

	/**
	 * 解析json，得到元素信息，并修改
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param object
	 * @param eleInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getElementInfo() {
		JSONArray array = new JSONArray();
		JSONArray ElementArray = new JSONArray();
		JSONObject tempobj;
		String eid, _id, content;
		if (eleArray != null && eleArray.size() != 0) {
			int l = eleArray.size();
			for (int i = 0; i < l; i++) {
				JSONObject ElementInfo = new JSONObject();
				tempobj = (JSONObject) eleArray.get(i);
				_id = tempobj.getString(pkString);
				if (_id.equals("")) {
					eid = AddElement(tempobj);
					tempobj.put(pkString, eid);
					content = tempobj.getString("content");
					if (!content.equals("")) {
						content = codec.DecodeHtmlTag(content);
						content = codec.decodebase64(content);
					}
					tempobj.put("content", content);
				} else {
					eid = tempobj.getString(pkString);
				}
				ElementInfo.put("bid", tempobj.getString("areaid"));
				ElementInfo.put("content", tempobj.getString("content"));
				ElementInfo.put(pkString, eid);
				ElementArray.add(ElementInfo);
				array.add(tempobj);
			}
			eleArray = array;
		}
		return ElementArray;
	}

	/**
	 * 解析json，得到元素信息，并修改
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param object
	 * @param eleInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	private JSONArray getBlockInfo(JSONObject object) {
		String _id, areaid;
		JSONArray array = new JSONArray();
		JSONObject BlockInfo;
		JSONArray Array = new JSONArray();
		JSONObject tempobj;
		if (eleArray != null && eleArray.size() > 0) {
			int l = eleArray.size();
			for (int i = 0; i < l; i++) {
				BlockInfo = new JSONObject();
				tempobj = (JSONObject) eleArray.get(i);
				_id = tempobj.getString("areaid");
				if (_id.equals("")) {
					areaid = AddBlock(tempobj, object);
					tempobj.put("areaid", areaid);
					tempobj.put("bid", areaid);
					_id = areaid;
				}
				BlockInfo.put("mid", object.getString("mid"));
				BlockInfo.put("area", tempobj.getString("area"));
				BlockInfo.put(pkString, _id);
				Array.add(BlockInfo);
				array.add(tempobj);
			}
			eleArray = array;
		}
		return Array;
	}

	@SuppressWarnings("unchecked")
	private String AddElement(JSONObject tempobj) {
		String id = "";
		if (tempobj != null && tempobj.size() > 0) {
			Element element = new Element();
			JSONObject temp = new JSONObject();
			temp.put("bid", tempobj.getString("areaid"));
			temp.put("content", tempobj.getString("content"));
			String info = element.AddElement(temp.toJSONString());
			info = JSONObject.toJSON(info).getString("message");
			if (info.contains("records")) {
				info = JSONObject.toJSON(info).getString("records");
				temp = JSONObject.toJSON(info);
				if (temp != null && temp.size() != 0) {
					id = (String) temp.getPkValue(pkString);
				}
			}
		}
		return id;
	}

	@SuppressWarnings("unchecked")
	private String AddBlock(JSONObject tempobj, JSONObject object) {
		Block block = new Block();
		JSONObject temp = new JSONObject();
		String id = "";
		temp.put("mid", object.getString("mid"));
		temp.put("area", tempobj.getString("area"));
		String info = block.AddBlock(temp.toJSONString());
		info = JSONObject.toJSON(info).getString("message");
		if (info.contains("records")) {
			info = JSONObject.toJSON(info).getString("records");
			temp = JSONObject.toJSON(info);
			if (temp != null && temp.size() > 0) {
				id = (String) temp.getPkValue(pkString);
			}
		}
		return id;
	}

	/**
	 * 获取主题 - 元素信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Mode.java
	 * 
	 * @param array
	 * @return
	 *
	 */
	private JSONArray getElement(JSONArray array) {
		Element element = new Element();
		JSONObject object;
		String tempId, eid = "";
		if (array != null && array.size() > 0) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				object = (JSONObject) array.get(i);
				tempId = object.getString("content");
				if (StringHelper.InvaildString(tempId)) {
					eid += tempId + ",";
				}
			}
			if (eid.length() > 0) {
				eid = StringHelper.fixString(eid, ',');
			}
			if (eid.length() > 0) {
				array = element.Element2Themem(eid, array);
			}
		}
		return array;
	}

	/**
	 * 批量查询模式及元素等信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param array
	 * @return
	 *
	 */
	private JSONArray SelectModeEle(JSONArray array) {
		JSONObject object;
		String mid = "", eid = "", tempmid, tempeid;
		if (array != null && array.size() > 0) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				object = (JSONObject) array.get(i);
				tempmid = object.getString("mid");
				tempeid = object.getString("content");
				if (!tempmid.equals("")) {
					mid += tempmid + ",";
				}
				if (!tempeid.equals("")) {
					eid += tempeid + ",";
				}
			}
		}
		if (mid != null && mid.length() > 0) {
			mid = StringHelper.fixString(mid, ',');
		}
		if (eid != null && eid.length() > 0) {
			eid = StringHelper.fixString(eid, ',');
		}
		return FillData(array, mid, eid);
	}

	/**
	 * 填充数据
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param array
	 * @param mid
	 * @param eid
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	private JSONArray FillData(JSONArray array, String mid, String eid) {
		Mode mode = new Mode();
		Element element = new Element();
		JSONObject ModeObj = null, ElementObj = null;
		JSONObject object;
		ModeObj = mode.getModeInfo(mid);
		// 获取元素信息及元素对应的区域信息
		ElementObj = element.GetElementInfo(eid);
		if (array != null && array.size() > 0) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				object = (JSONObject) array.get(i);
				object = FillModeEle(ModeObj, ElementObj, object);
				array.set(i, object);
			}
		}
		return array;
	}

	/**
	 * 获取模式数据，元素数据
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param object
	 * @return
	 *
	 */
	public JSONObject getInfo(JSONObject object) {
		Mode mode = new Mode();
		Element element = new Element();
		JSONObject ModeObj = null, ElementObj = null;
		String mid = "", eid = "";
		if (object != null) {
			// 获取模式及大屏信息
			mid = object.getString("mid");
			ModeObj = mode.getModeInfo(mid);
			// 获取元素信息及元素对应的区域信息
			eid = object.getString("content");
			ElementObj = element.GetElementInfo(eid);
		}
		object = FillModeEle(ModeObj, ElementObj, object);
		return object;
	}

	private JSONObject getInfos(JSONObject object) {
		Mode mode = new Mode();
		Element element = new Element();
		JSONObject ModeObj = null, ElementObj = null;
		String mid = "", eid = "";
		if (object != null) {
			// 获取模式及大屏信息
			mid = object.getString("mid");
			ModeObj = mode.getModeInfo(mid);
			// 获取元素信息及元素对应的区域信息
			eid = object.getString("content");
			ElementObj = element.GetElementInfos(eid);
		}
		object = FillModeEle(ModeObj, ElementObj, object);
		return object;
	}

	/**
	 * 填充模式及元素数据
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file ThemeTemp.java
	 * 
	 * @param ModeObj
	 * @param EleObj
	 * @param themeObj
	 * @param eid
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	private JSONObject FillModeEle(JSONObject ModeObj, JSONObject EleObj, JSONObject themeObj) {
		String eid, mid;
		String modeName = "", ScreenName = "", sid = "";
		JSONArray tempArray = new JSONArray();
		JSONObject tempobj;
		if (themeObj != null && themeObj.size() > 0) {
			mid = themeObj.getString("mid");
			eid = themeObj.getString("content");
			if (ModeObj != null && ModeObj.size() > 0) {
				tempobj = (JSONObject) ModeObj.get(mid);
				if (tempobj != null && tempobj.size() > 0) {
					modeName = tempobj.getString("modeName");
					ScreenName = tempobj.getString("ScreenName");
					sid = tempobj.getString("sid");
				}
			}
			if (EleObj != null && EleObj.size() > 0) {
				String[] value = eid.split(",");
				for (String str : value) {
					JSONObject tempObj = (JSONObject) EleObj.get(str);
					if ((tempObj != null) && (tempObj.size() != 0)) {
						tempObj.put("content", JSONArray.toJSONArray(tempObj.getString("content")));
						tempArray.add(tempObj);
					}
				}
			}
			themeObj.remove("content");
			themeObj.put("element", tempArray);
			themeObj.put("modeName", modeName);
			themeObj.put("ScreenName", ScreenName);
			themeObj.put("sid", sid);
		}
		return themeObj;
	}
	
	/*
	 *model 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public String resultJSONInfo(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}
	
	public String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填字段为空";
			break;
		default:
			msg = "其他操作异常";
			break;
		}
		return rMsg.netMSG(num, msg);
	}
	
	/**
	 * 分页数据输出
	 * 
	 * @param array
	 *            当前页数据
	 * @param total
	 *            总数据量
	 * @param totalSize
	 *            总页数
	 * @param idx
	 *            当前页
	 * @param pageSize
	 *            每页数据量
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String pageShow(JSONArray array, long total, long totalSize, int idx, int pageSize) {
		array = (array != null && array.size() > 0) ? array : new JSONArray();
		JSONObject object = new JSONObject();
		object.put("currentPage", idx);
		object.put("pageSize", pageSize);
		object.put("total", total);
		object.put("totalSize", totalSize);
		object.put("data", array);
		return resultJSONInfo(object);
	}
	
	@SuppressWarnings("unchecked")
	public String resultArray(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
		_obj.put("records", array);
		return resultMessage(0, _obj.toString());
	}
	
	public String getFilepath() {
		return getConfig("imgurl");
	}
	
	/**
	 * 读取配置文件
	 * 
	 * @project File
	 * @package model
	 * @file GetFileUrl.java
	 * 
	 * @param key
	 *            配置文件中 key值
	 * @return
	 *
	 */
	private String getConfig(String key) {
		String value = "";
		try {
			Properties pro = new Properties();
			pro.load(new FileInputStream("URLConfig.properties"));
			value = pro.getProperty(key);
		} catch (Exception e) {
			value = "";
		}
		return value;
	}
	
	public String getThumbnailConfig(String key) {
		String value = "";
		value = getConfig(key);
		return value;
	}
	
	public String getFileUrl() {
		String url = getConfig("file").split("/")[0];
		return "http://" + url;
	}
	
}
