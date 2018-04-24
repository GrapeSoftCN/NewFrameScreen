package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.security.codec;
import common.java.string.StringHelper;

public class Element {
	private GrapeTreeDBModel gDbModel;
//	private JSONObject userInfo = new JSONObject();
//	private String userid ;
	private JSONObject _obj = new JSONObject();
	private String pkString;

	public Element() {
		
		gDbModel = new GrapeTreeDBModel();
		//数据模型
		GrapeDBDescriptionModel gdbField = new GrapeDBDescriptionModel();
        gdbField.importDescription(appsProxy.tableConfig("Element"));
        gDbModel.descriptionModel(gdbField);

        //权限模型
        GrapePermissionsModel gperm = new GrapePermissionsModel();
		gperm.importDescription(appsProxy.tableConfig("Element"));
		gDbModel.permissionsModel(gperm);
		
		pkString = gDbModel.getPk();
		//用户信息
//        userInfo = (new session()).getDatas();
        //用户的id
//        if (userInfo != null && userInfo.size() != 0) {
//			userid = (String) userInfo.getPkValue(pkString);
//		}
        
        //开启检查模式
        gDbModel.enableCheck();
	}

	/**
	 * 新增元素信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ScreenInfo
	 * @return
	 *
	 */
//	@SuppressWarnings("unchecked")
	public String AddElement(String ElementInfo) {
		Object info = "";
//		String content;
		if(StringHelper.InvaildString(ElementInfo)){
			return resultmsg(1);
		}
		ElementInfo = codec.DecodeFastJSON(ElementInfo);
		
		JSONObject obj = JSONObject.toJSON(ElementInfo);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
//		if (obj != null && obj.size() > 0) {
//			if (!obj.containsKey("content")) {
//				return rMsg.netMSG(3, "元素内容为空");
//			}
//			content = obj.getString("content");
//			content = codec.DecodeHtmlTag(content);
//			content = codec.decodebase64(content);
//			obj.put("content", content);
//		}
		info = gDbModel.data(obj).autoComplete().insertOnce();
		if (info == null) {
			return resultmsg(1);
		}
		obj = Find(info.toString());
		return resultJSONInfo(obj);
	}

	/**
	 * 修改元素信息
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
	public String UpdateElement(String id, String ElementInfo) {
		Theme theme = new Theme();
		String cont;
		int code = 99;
		if(StringHelper.InvaildString(id)){
			return resultMessage(code, "没有选中需要修改的元素");
		}
		String tid = theme.getTidByeid(id);
		JSONObject object = new JSONObject();
		if(StringHelper.InvaildString(ElementInfo)){
			return resultmsg(1);
		}
		ElementInfo = codec.DecodeFastJSON(ElementInfo);
		
		JSONObject obj = JSONObject.toJSON(ElementInfo);
		if (id == null || id.equals("") || id.equals("null")) {
			return rMsg.netMSG(4, "无效元素id");
		}
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (obj != null && obj.size() > 0) {
			if (obj.containsKey("content")) {
				cont = obj.getString("content");
				cont = codec.DecodeHtmlTag(cont);
				obj.put("content", cont);
			}
			gDbModel.eq(pkString, id);
			code = (gDbModel.dataEx(obj).updateEx() ) ? 0 : 99;//修改了
			if (code == 0) {
				if (!tid.equals("")) {
					JSONObject content = Find(id);
					object.put("target", id);
					object.put("content", content);
					broadManage.broadEvent(tid, 1, object.toString());
				}
			}
		}
		return resultMessage(code, "修改成功");
	}

	@SuppressWarnings("unchecked")
	protected String UpdateAllElement(JSONArray eleArray) {
		String content;
		JSONObject obj;
		String id;
		if (eleArray != null && eleArray.size() != 0) {
			for (Object object : eleArray) {
				obj = (JSONObject) object;
				if (obj.containsKey("content")) {
					content = obj.getString("content");
					content = codec.DecodeHtmlTag(content);
					content = codec.decodebase64(content);
					obj.put("content", content);
				}
				id = obj.getString(pkString);
				obj.remove(pkString);
				gDbModel.eq(pkString, id).data(obj).updateEx();//修
			}
		}
		return resultMessage(0, "修改成功");
	}

	/**
	 * 删除元素信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteElement(String ids) {
		long tipcode = 0;
		if (StringHelper.InvaildString(ids)) {
			return rMsg.netMSG(4, "无效元素id");
		}
		String[] value = ids.split(",");
//		int l = value.length;
		gDbModel.or();
		for (String id : value) {
			gDbModel.eq(pkString, id);
		}
		tipcode = gDbModel.deleteAll();
		return resultMessage(tipcode > 0 ? 0 : 99, "删除成功");
	}

	/**
	 * 分页显示大屏信息
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
	public String PageElement(int idx, int PageSize, String condString) {
		JSONArray array = null;
		long total = 0, totalSize = 0;
		JSONArray condArray = null;
		if(!StringHelper.InvaildString(condString)){
			condArray = JSONArray.toJSONArray(condString);
		}
		if (idx > 0 && PageSize > 0) {
			if (condArray != null && condArray.size() > 0) {
				gDbModel.where(condArray);
			}
//			gDbModel.eq("userid", userid);//这个条件是干嘛的
			array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").dirty()
					.page(idx, PageSize);
			total = gDbModel.dirty().count();
			totalSize = gDbModel.pageMax(PageSize);
		}
		return pageShow(array, total, totalSize, idx, PageSize);
	}

	/**
	 * 显示所有的元素信息，非管理员用户只能查看与自己相关的大屏信息 
	 * TODO 有问题，这个数量级太大
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
	public String ShowScreen() {
		JSONArray array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").select();
		return resultArray(array);
	}

	/**
	 * 批量查询元素信息，封装成固定格式输出，{"message":{"records":[]},"errorcode":0}
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String ShowElement(String ids) {
		return resultArray(BatchElement(ids));
	}

	/**
	 * 获取元素信息，封装成{eid:{"content":""},eid:{"content":""}}
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public JSONObject GetElementInfo(String ids) {
		JSONObject tempObj, obj = new JSONObject();
		String id, temp="5";
		int l = 0;
		JSONArray array = BatchElement(ids);
		JSONArray contentobj = new JSONArray();
		array = getBlocks(array);
		JSONObject object = null;
		if (array != null && array.size() > 0) {
			l = array.size();
			for (int i = 0; i < l; i++) {
				object = new JSONObject();
				tempObj = (JSONObject) array.get(i);
				id = (String) tempObj.getPkValue(pkString);
				contentobj = JSONArray.toJSONArray(tempObj.get("content").toString());
				object.put("content", (contentobj != null && contentobj.size() != 0) ? contentobj : new JSONArray());
				object.put("area", tempObj.getString("area"));
				object.put("areaid", tempObj.getString("areaid"));
				object.put("bid", tempObj.getString("bid"));
				object.put(pkString, tempObj.getString(pkString));
				if (object.containsKey("timediff")) {
					temp = tempObj.getString("timediff");
					if (temp.contains("$numberLong")) {
						temp = JSONObject.toJSON(temp).getString("$numberLong");
					}
					if (!StringHelper.InvaildString(temp)) {
						temp = "5";
					}
				}
				object.put("timediff", Integer.parseInt(temp));
				obj.put(id, object);
			}
		}
		System.out.println(obj);
		return obj;
	}

	@SuppressWarnings("unchecked")
	public JSONObject GetElementInfos(String ids) {
		JSONObject tempObj, obj = new JSONObject();
		String id;
		int l = 0;
		JSONArray array = BatchElement(ids);
		array = getBlock(array);
		if (array != null && array.size() > 0) {
			l = array.size();
			for (int i = 0; i < l; i++) {
				tempObj = (JSONObject) array.get(i);
				id = (String) tempObj.getPkValue(pkString);
				obj.put(id, tempObj);
			}
		}
		System.out.println(obj);
		return obj;
	}

	/**
	 * 获取区域信息,添加至元素信息中
	 * 
	 * @param array
	 * @return
	 */
	private JSONArray getBlock(JSONArray array) {
		array = new Mode().getBlock(array);
		return array;
	}

	private JSONArray getBlocks(JSONArray array) {
		Block block = new Block();
		String bid = "";
		JSONObject BlockInfo = new JSONObject();
		bid = getBid(array);
		if (bid.length() > 0) {
			BlockInfo = block.GetBlockInfo(bid);
		}
		return Block2Element(array, BlockInfo);
	}

	private String getBid(JSONArray array) {
		String bid = "";
		if ((array != null) && (array.size() != 0)) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				JSONObject object = (JSONObject) array.get(i);
				String tempId = object.getString("bid");
				bid = bid + tempId + ",";
			}
			if (bid.length() > 0) {
				bid = StringHelper.fixString(bid, ',');
			}
		}
		return bid;
	}

	@SuppressWarnings("unchecked")
	private JSONArray Block2Element(JSONArray array, JSONObject BlockInfo) {
		if ((BlockInfo != null) && (BlockInfo.size() != 0)) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				JSONObject object = (JSONObject) array.get(i);
				array.set(i, FillBlock(object, BlockInfo));
			}
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	private JSONObject FillBlock(JSONObject ModeInfo, JSONObject BlockInfo) {
		String id, area = "", areaid = "";
		if ((ModeInfo != null) && (ModeInfo.size() != 0)) {
			String bid = ModeInfo.getString("bid");
			String[] value = bid.split(",");
			for (String str : value) {
				JSONObject tempObj = (JSONObject) BlockInfo.get(str);
				if ((tempObj != null) && (tempObj.size() != 0)) {
					id = tempObj.getString(pkString);
					area = tempObj.getString("area");
					areaid = id;
				}
			}
			ModeInfo.put("area", area);
			ModeInfo.put("areaid", areaid);
			// ModeInfo.remove("bid");
			// ModeInfo.put("area", tempArray);
		}
		return ModeInfo;
	}
	//
	// private JSONArray getBlock(JSONArray array) {
	// Block block = new Block();
	// JSONObject object;
	// String tempId, bid = "";
	// if (array != null && array.size() != 0) {
	// int l = array.size();
	// for (int i = 0; i < l; i++) {
	// object = (JSONObject) array.get(i);
	// tempId = object.getString("bid");
	// bid += tempId + ",";
	// }
	// if (bid.length() > 0) {
	// bid = StringHelper.fixString(bid, ',');
	// }
	// if (bid.length() > 0) {
	// array = block.Block2Mode(bid, array);
	// }
	// }
	// return array;
	// }

	/**
	 * 批量查询元素信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	private JSONArray BatchElement(String ids) {
		gDbModel.or();
		JSONArray array = null;
		if (!StringHelper.InvaildString(ids)) {
			String[] value = ids.split(",");
			if (value.length > 0) {
				for (String id : value) {
					if (!StringHelper.InvaildString(id)) {
						if (ObjectId.isValid(id)) {
							gDbModel.eq(pkString, id);
						}
					}
				}
				if(!gDbModel.nullCondition()){
					array = gDbModel.field("_id,bid,content,timediff").select();
				}
				// array =
				// gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").select();
			}
		}
		return array;
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
	public JSONObject Find(String info) {
		JSONObject obj = gDbModel.eq(pkString, info).field(pkString + ",content,bid,timediff").limit(1).find();
		return obj;
	}

	/**
	 * 添加元素信息到主题信息数据中
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Mode.java
	 * 
	 * @param array
	 * @param ElementInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public JSONArray Element2Themem(String eid, JSONArray array) {
		JSONObject tempObj = new JSONObject();
		JSONObject ElementInfo;
		if (eid != null && !eid.equals("")) {
			ElementInfo = GetElementInfo(eid);
			if (ElementInfo != null && ElementInfo.size() != 0) {
				int l = array.size();
				for (int i = 0; i < l; i++) {
					tempObj = (JSONObject) array.get(i);
					tempObj = FillElement(tempObj, ElementInfo);
					array.set(i, tempObj);
				}
			}
		}
		return array;
	}

	/**
	 * 元素内容填充至主题中
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Theme.java
	 * 
	 * @param ThemeInfo
	 * @param ElementInfo
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public JSONObject FillElement(JSONObject ThemeInfo, JSONObject ElementInfo) {
		JSONObject tempObj;
		JSONArray tempArray = new JSONArray(), contentArray;
		String bid,temp = "5";
		String[] value;
		if (ThemeInfo != null && ThemeInfo.size() != 0) {
			bid = ThemeInfo.getString("content");
			value = bid.split(",");
			for (String str : value) {
				tempObj = (JSONObject) ElementInfo.get(str);
				if (tempObj != null && tempObj.size() != 0) {
					contentArray = JSONArray.toJSONArray(tempObj.getString("content"));
					tempObj.put("content", contentArray);
					temp = tempObj.getString("timediff");
					if (temp.contains("$numberLong")) {
						temp = JSONObject.toJSON(temp).getString("$numberLong");
					}
					tempObj.put("timediff", Integer.parseInt(temp));
					tempArray.add(tempObj);
				}
			}
			ThemeInfo.remove("content");
			ThemeInfo.put("element", tempArray);
		}
		return ThemeInfo;
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
	
	@SuppressWarnings("unchecked")
	public String resultArray(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
		_obj.put("records", array);
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
	
	public String resultmsg(int num) {
		return resultMessage(num, "");
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
}
