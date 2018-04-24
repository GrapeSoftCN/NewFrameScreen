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

public class Mode {
	private GrapeTreeDBModel gDbModel;
	private JSONObject userInfo;
	private String userid ;
	private Block block = new Block();
	private JSONObject _obj = new JSONObject();
	private String pkString;

	public Mode() {
		
		gDbModel = new GrapeTreeDBModel();
		//数据模型
		GrapeDBDescriptionModel gdbField = new GrapeDBDescriptionModel();
        gdbField.importDescription(appsProxy.tableConfig("Mode"));
        gDbModel.descriptionModel(gdbField);
        
        //权限模型
        GrapePermissionsModel gperm = new GrapePermissionsModel();
		gperm.importDescription(appsProxy.tableConfig("Mode"));
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
	 * 获取大屏id
	 * @param mid
	 * @return
	 */
	public String getSid(String mid) {
		String sid = "";
		if(StringHelper.InvaildString(mid)){
			return null;
		}
		JSONObject object = Find(mid);
		if (object != null && object.size() != 0) {
			sid = object.getString("sid");
		}
		return sid;
	}

	/**
	 * 新增模式信息
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
	public String AddMode(String ModeInfo) {
		Object info;
		if(StringHelper.InvaildString(ModeInfo)){
			return rMsg.netMSG(2, "参数为空");
		}
		ModeInfo = codec.DecodeFastJSON(ModeInfo);
		JSONObject obj = JSONObject.toJSON(ModeInfo);
		obj.put("userid", userid);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		info = gDbModel.data(obj).autoComplete().insertOnce();
		obj = gDbModel.eq(pkString, (info.toString())).limit(1).find();
		return resultJSONInfo(obj);
	}

	/**
	 * 修改模式信息
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
	public String UpdateMode(String id, String ScreenInfo) {
		int code = 99;
		if (StringHelper.InvaildString(id)) {
			return rMsg.netMSG(3, "无效模式id");
		}
		if(StringHelper.InvaildString(ScreenInfo)){
			return rMsg.netMSG(2, "参数为空");
		}
		ScreenInfo = codec.DecodeFastJSON(ScreenInfo);
		
		JSONObject obj = JSONObject.toJSON(ScreenInfo);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (obj != null && obj.size() > 0) {
			gDbModel.eq(pkString, id);
			code = (gDbModel.dataEx(obj).updateEx()) ? 0 : 99;
		}
		return resultMessage(code, "修改成功");
	}

	/**
	 * 删除模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteMode(String ids) {
		String bid = "";
		JSONArray BlockInfo = new JSONArray();
		long tipcode = 99;
		if (StringHelper.InvaildString(ids)) {
			return rMsg.netMSG(3, "无效模式id");
		}
		String[] value = ids.split(",");
		gDbModel.or();
		for (String id : value) {
			gDbModel.eq(pkString, id);
		}
		BlockInfo = gDbModel.dirty().select();
		bid = getBid(BlockInfo);
		if (!StringHelper.InvaildString(bid)) {
			block.DeleteBlock(bid);
		}
		tipcode = gDbModel.deleteAll();
		return resultMessage(tipcode > 0 ? 0 : 99, "删除成功");
	}

	/**
	 * 分页显示模式信息
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
	public String PageMode(int idx, int PageSize, String condString) {
		JSONArray array = null;
		JSONArray condArray;
		long total = 0, totalSize = 0;
		if (idx <= 0 || PageSize <= 0) {
			return rMsg.netMSG(3, "当前页码小于0或者每页最大量小于0");
		}
		if (!StringHelper.InvaildString(condString)) {
			condArray = JSONArray.toJSONArray(condString);
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
		return pageShow(getBlock(array), total, totalSize, idx, PageSize);
	}

	/**
	 * 显示所有的模式信息，非管理员用户只能查看与自己相关的模式信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @return
	 *
	 */
	public String ShowMode() {
		JSONArray array = null;
//		userid = "5993c3829c93690f5ac4ca2d";
		gDbModel.eq("userid", userid);//判断当前用户的信息
		array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode,userid").select();
		return resultArray(getBlock(array));
	}

	/**
	 * 显示详细信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param info  -- _id
	 * @return
	 *
	 */
	public JSONObject Find(String info) {
		if(StringHelper.InvaildString(info)){
			return null;
		}
		Block block = new Block();
		String bid;
		JSONObject blockInfo;
		JSONObject obj = gDbModel.eq(pkString, info)
				.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").limit(1).find();
		if (obj != null && obj.size() > 0) {
			bid = obj.getString("bid");
			if (!StringHelper.InvaildString(bid)) {
				blockInfo = block.GetBlockInfo(bid);
				obj = block.FillBlock(obj, blockInfo);
			}
		}
		return obj;
	}

	public String FindMode(String info) {
		return resultJSONInfo(Find(info));
	}

	/**
	 * 获取模式 - 区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Mode.java
	 * 
	 * @param array
	 * @return
	 *
	 */
	public JSONArray getBlock(JSONArray array) {
		String bid = "";
		bid = getBid(array);
		if (bid.length() > 0) {
			array = block.Block2Mode(bid, array);
		}
		return array;
	}

	/**
	 * 获取模式所包含的区域id
	 * 
	 * @param array
	 * @return
	 */
	private String getBid(JSONArray array) {
		JSONObject object;
		String tempId, bid = "";
		if (array != null && array.size() > 0) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				object = (JSONObject) array.get(i);
				tempId = object.getString("bid");
				bid += tempId + ",";
			}
			if (bid.length() > 0) {
				bid = StringHelper.fixString(bid, ',');
			}
		}
		return bid;
	}

	/**
	 * 根据大屏id查询所有的模式id
	 * 
	 * @param sid
	 * @return
	 */
	public String getMidBySid(String sid) {
		String mid = "";
		JSONObject object;
		String tmpsid;
		JSONArray array;
		if (!StringHelper.InvaildString(sid)) {
			String[] value = sid.split(",");
			gDbModel.or();
			for (String id : value) {
				gDbModel.eq(pkString, id);
			}
			array = gDbModel.eq("sid", sid).select();
			for (Object obj : array) {
				object = (JSONObject) obj;
				tmpsid = (String) object.getPkValue(pkString);
				mid += tmpsid + ",";
			}
			mid = StringHelper.fixString(mid, ',');
		}
		return mid;
	}

	protected JSONObject getModeInfo(String mids) {
		JSONObject object, obj = new JSONObject();
		gDbModel.or();
		JSONArray array = null;
		if (mids != null && !mids.equals("")) {
			String[] value = mids.split(",");
			for (String id : value) {
				gDbModel.eq(pkString, id);
			}
			array = gDbModel.select();
		}
		if (array != null && array.size() != 0) {
			for (Object object2 : array) {
				object = (JSONObject) object2;
				obj = getInfo(obj, object);
			}
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getInfo(JSONObject obj, JSONObject object) {
		Screen screen = new Screen();
		String sid, mid, ScreenName = "", modeName = "";
		JSONObject tempObj = new JSONObject(), ScreenObj;
		if (object != null && object.size() != 0) {
			sid = object.getString("sid");
			mid = object.getString(pkString);
			// 获取大屏信息
			ScreenObj = screen.getScreenInfo(sid);
			if (ScreenObj != null && ScreenObj.size() != 0) {
				ScreenName = ScreenObj.getString(sid);
			}
			tempObj.put("modeName", modeName);
			tempObj.put("ScreenName", ScreenName);
			tempObj.put("sid", sid);
			obj.put(mid, tempObj);
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
		array = (array != null && array.size() != 0) ? array : new JSONArray();
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
