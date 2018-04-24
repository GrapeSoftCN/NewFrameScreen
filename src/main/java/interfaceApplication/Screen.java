package interfaceApplication;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.security.codec;
import common.java.session.session;
import common.java.string.StringHelper;

/**
 * 
 * [大屏] <br> 
 *  
 * @author [南京喜成]<br>
 * @version 1.0.0<br>
 * @CreateDate 2018年4月2日 <br>
 * @since v1.0.0<br>
 * @see interfaceApplication <br>
 */
public class Screen {
	private GrapeTreeDBModel gDbModel;
	private JSONObject userInfo;
	private String userid;
	private JSONObject _obj = new JSONObject();
	private String pkString; 

	public Screen() {
		
		gDbModel = new GrapeTreeDBModel();
		//数据模型绑定
		GrapeDBDescriptionModel gdbField = new GrapeDBDescriptionModel();
		System.out.println(appsProxy.tableConfig("Screen"));
        gdbField.importDescription(appsProxy.tableConfig("Screen"));
        gDbModel.descriptionModel(gdbField);
        
        //权限模型绑定
  		GrapePermissionsModel gperm = new GrapePermissionsModel();
  		gperm.importDescription(appsProxy.tableConfig("Screen"));
  		gDbModel.permissionsModel(gperm);
        
  		pkString = gDbModel.getPk();
        //用户信息
        userInfo = (new session()).getDatas();
        //用户的id
        if (userInfo != null && userInfo.size() != 0) {
			userid = (String) userInfo.getPkValue(pkString);
		}
        
        //开启检查模式,权限检查
        gDbModel.enableCheck();
        
	
	}

	/**
	 * 
	 * [这是设置主题的信息] <br> 
	 *  
	 * @author [南京喜成]<br>
	 * @param screenid 大屏的_id
	 * @param themeId  大屏的currenttid
	 * @return <br>
	 */
	public String SetTheme(String screenid, String themeId) {
		Theme theme = new Theme();
		int code = 99;
		String themes = "{\"currenttid\":\"" + themeId + "\"}";
		code = gDbModel.eq(pkString , screenid).data(themes).updateEx() ? 0 : 99;
		if (code == 0) {
			String content = theme.getThemeById(themeId);
			broadManage.broadEvent(themeId, 0, content);
			return resultMessage(code, "设置当前主题成功");
		}
		
		return resultMessage(code, "设置当前主题失败");
	}

	/**
	 * 新增大屏信息 
	 * 
	 * @param ScreenInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String AddScreen(String ScreenInfo) {
		Object info = null;
		if(StringHelper.InvaildString(ScreenInfo)){
			return rMsg.netMSG(2, "参数信息为空");
		}
		//解析参数信息
		ScreenInfo = codec.DecodeFastJSON(ScreenInfo);
		JSONObject obj = JSONObject.toJSON(ScreenInfo);
		Object put = obj.put("userid", userid);
		System.out.println(put);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		} 
		info = gDbModel.data(obj).autoComplete().insertOnce();
		obj = gDbModel.eq(pkString, info.toString()).find();
		return resultJSONInfo(obj);
	}

	/**
	 * 修改大屏信息
	 * 
	 * @param id
	 * @param ScreenInfo
	 * @return
	 */
	public String UpdateScreen(String id, String ScreenInfo) {
		int code = 99;
		if(StringHelper.InvaildString(ScreenInfo)){
			return rMsg.netMSG(2, "参数信息为空");
		}
		//解析参数信息
		ScreenInfo = codec.DecodeFastJSON(ScreenInfo);
		
		JSONObject obj = JSONObject.toJSON(ScreenInfo);
		if (id == null || id.equals("") || id.equals("null")) {
			return rMsg.netMSG(3, "无效屏幕id");
		}
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}

		if (obj != null && obj.size() > 0) {
			gDbModel.eq(pkString, id);
			code = (gDbModel.dataEx(obj).updateEx() ? 0 : 99);
		}
		if(code == 0){
			return resultMessage(code, "修改大屏信息成功");
		}
		return resultMessage(code, "修改大屏信息失败");
	}

	/**
	 * 删除大屏信息，支持批量删除 使用批量删除功能，则id之间使用","隔开
	 * 
	 * 删除大屏，同时删除该大屏所包含的模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteScreen(String ids) {
		long tipcode = 99;
		int l = 0;
		if (StringHelper.InvaildString(ids)) {
			return rMsg.netMSG(3, "无效屏幕id");
		}
		if (ids != null && !ids.equals("")) {
			String[] value = ids.split(",");
			l = value.length;
			gDbModel.or();
			for (String id : value) {
				gDbModel.eq(pkString, id);
			}
			tipcode = gDbModel.deleteAllEx();
		}
		return resultMessage(tipcode == l ? 0 : 99, "删除成功");
	}

	/**
	 * 分页显示大屏信息,当查询条件为null时，默认分页查询
	 * 
	 * 系统管理员用户，可以查询所有大屏数据
	 * 
	 * 非系统管理员用户，只能查询与自己相关的大屏信息
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
	public String PageScreen(int idx, int PageSize, String condString) {
		JSONArray array = null;
		long total = 0, totalSize = 0;
		if (idx <= 0 || PageSize <= 0) {
			return rMsg.netMSG(3, "当前页码小于0或者每页最大量小于0");
		}
		if (!StringHelper.InvaildString(condString)) {
			JSONArray condArray = JSONArray.toJSONArray(condString);
			if (condArray != null && condArray.size() > 0) {
				gDbModel.where(condArray);
			} else {
				return pageShow(null, total, totalSize, idx, PageSize);
			}
		}
		array = gDbModel.dirty().mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,userid")
				.page(idx, PageSize);
		total = gDbModel.dirty().count();
		totalSize = gDbModel.pageMax(PageSize);
		return pageShow(array, total, totalSize, idx, PageSize);
	}

	/**
	 * 显示所有的大屏信息，非管理员用户只能查看与自己相关的大屏信息 1
	 * 
	 * @return {"message":{"records":[{}]},"errorcode":0}
	 */
	public String ShowScreen() {
		JSONArray array = null;
		array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,userid").select();
		return resultArray(array);
	}

	/**
	 * 前台大屏显示
	 * 
	 * @param screenid
	 * @param mid
	 * @param tid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ShowScreenFront(String screenid, String mid, String tid) {
		Object area = "";
		JSONObject ScreenInfo = null;
		if(StringHelper.InvaildString(screenid)){
			return resultJSONInfo(null);
		}
		ScreenInfo = Find(screenid);//当前屏幕的信息
		JSONObject ModeInfo = Ele2Mode(mid, tid);
		area = (ModeInfo != null) && (ModeInfo.size() > 0) ? ModeInfo.get("area") : "";
		if ((ScreenInfo != null) && (ScreenInfo.size() > 0)) {
			ScreenInfo.put("area", area);
		}
		return resultJSONInfo(ScreenInfo);
	}

	/**
	 * 
	 * 
	 * @param val
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ShowFronts(String screenid) {
		Object area = "";
		String themeid = "";
		String modeid = "";
		Theme theme = new Theme();
		JSONObject ScreenInfo = null;
		JSONObject ModeInfo = null;
		JSONObject ThemeInfo = null;
		if(StringHelper.InvaildString(screenid)){
			return resultJSONInfo(null);
		}
		String[] value = screenid.split("\\*");
		ScreenInfo = Find(value[0]);
		if (ScreenInfo != null && ScreenInfo.size() > 0) {
			themeid = ScreenInfo.getString("currenttid");
		}
		if (!themeid.equals("")) {
			ThemeInfo = theme.Find(themeid);
			if (ThemeInfo != null && ThemeInfo.size() > 0) {
				modeid = ThemeInfo.getString("mid");
			}
		}
		ModeInfo = Ele2Mode(modeid, themeid);
		area = (ModeInfo != null) && (ModeInfo.size() > 0) ? ModeInfo.get("area") : "";
		if ((ScreenInfo != null) && (ScreenInfo.size() > 0)) {
			ScreenInfo.put("area", area);
		}
		return resultJSONInfo(ScreenInfo);
	}

	/**
	 * 
	 * 
	 * @param val
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ShowFront(String screenid) {
		Object area = "";
		String themeid = "";
		String modeid = "";
		Theme theme = new Theme();
		JSONObject ScreenInfo = null;
		JSONObject ModeInfo = null;
		JSONObject ThemeInfo = null;
		if(StringHelper.InvaildString(screenid)){
			return resultJSONInfo(null);
		}
		String[] value = screenid.split("\\*");
		ScreenInfo = Find(value[0]);
		if (value != null && value.length >= 2) {
			themeid = value[1];
		} else {
			if (ScreenInfo != null && ScreenInfo.size() > 0) {
				themeid = ScreenInfo.getString("currenttid");
			}
		}
		if (!themeid.equals("")) {
			ThemeInfo = theme.Find(themeid);
			if (ThemeInfo != null && ThemeInfo.size() > 0) {
				modeid = ThemeInfo.getString("mid");
			}
		}
		ModeInfo = Ele2Mode(modeid, themeid);
		area = (ModeInfo != null) && (ModeInfo.size() > 0) ? ModeInfo.get("area") : "";
		if ((ScreenInfo != null) && (ScreenInfo.size() > 0)) {
			ScreenInfo.put("area", area);
		}
		return resultJSONInfo(ScreenInfo);
	}

	@SuppressWarnings("unchecked")
	private JSONObject Ele2Mode(String mid, String tid) {
		JSONObject modeInfo = getMode(mid);//获取mode的信息
		JSONObject ThemeInfo = getTheme(tid);//获取主题的信息
		JSONArray ele = new JSONArray();
		JSONArray area = new JSONArray();

		if ((modeInfo != null) && (modeInfo.size() > 0)) {
			area = JSONArray.toJSONArray(modeInfo.getString("area"));
		}
		if ((ThemeInfo != null) && (ThemeInfo.size() > 0)) {
			ele = JSONArray.toJSONArray(ThemeInfo.getString("element"));
		}
		if ((ele != null) && (ele.size() > 0) && (area != null) && (area.size() > 0)) {
			int l = area.size();
			for (int i = 0; i < l; i++) {
				JSONObject areaObj = (JSONObject) area.get(i);
				String areaid = areaObj.getString("areaid");
				JSONArray contentArray = new JSONArray();
				for (Object object : ele) {
					JSONObject eleObj = (JSONObject) object;
					String bid = eleObj.getString("bid");
					if (areaid.equals(bid)) {
						contentArray = JSONArray.toJSONArray(eleObj.getString("content"));
					}
					areaObj.put("element", contentArray);
					areaObj.put("timediff", Integer.parseInt(eleObj.getString("timediff")));
				}

				area.set(i, areaObj);
				modeInfo.put("area", area);
			}
		}
		return modeInfo;
	}

	/**
	 * 
	 * [获取mode的信息] <br> 
	 *  
	 * @author [南京喜成]<br>
	 * @param mid
	 * @return <br>
	 */
	private JSONObject getMode(String mid) {
		Mode mode = new Mode();
		JSONObject ModeInfo = new JSONObject();
		if (!StringHelper.InvaildString(mid)) {
			ModeInfo = mode.Find(mid);//mode的_id
		}
		return ModeInfo;
	}

	private JSONObject getTheme(String tid) {
		Theme theme = new Theme();
		JSONObject ThemeInfo = new JSONObject();
		if (!StringHelper.InvaildString(tid)) {
			ThemeInfo = theme.Find(tid);
		}
		return ThemeInfo;
	}

	/**
	 * 显示大屏详细信息
	 * 
	 * @param info
	 * @return
	 */
	public String FindScreen(String info) {
		return resultJSONInfo(Find(info));
	}

	protected JSONObject Find(Object info) {
		gDbModel.eq(pkString, info);
		JSONObject object = gDbModel.limit(1).find();
		return object;
	}

	@SuppressWarnings("unchecked")
	protected JSONObject getScreenInfo(String sids) {
		String sid, sname;
		JSONObject object, obj = new JSONObject();
		JSONArray array = null;
		if (!StringHelper.InvaildString(sids)) {
			String[] value = sids.split(",");
			for (String id : value) {
				gDbModel.eq(pkString, id);
			}
			array = gDbModel.field(pkString + ",name").select();
		}
		if (array != null && array.size() != 0) {
			for (Object object2 : array) {
				object = (JSONObject) object2;
				sid = (String) object.getPkValue(pkString);
				sname = object.getString("name");
				obj.put(sid, sname);
			}
		}
		return obj;
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
}
