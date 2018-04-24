package interfaceApplication;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.security.codec;
import common.java.string.StringHelper;

public class Block {
	private GrapeTreeDBModel gDbModel;
	private JSONObject _obj = new JSONObject();
	private String pkString;
	

	public Block() {
		
		gDbModel = new GrapeTreeDBModel();
		//数据模型
		GrapeDBDescriptionModel  gdbField = new GrapeDBDescriptionModel ();
        gdbField.importDescription(appsProxy.tableConfig("Block"));
        gDbModel.descriptionModel(gdbField);
        
        //权限模型
        GrapePermissionsModel gperm = new GrapePermissionsModel();
		gperm.importDescription(appsProxy.tableConfig("Block"));
		gDbModel.permissionsModel(gperm);
		
		pkString = gDbModel.getPk();
		
        //开启检查模式
        gDbModel.enableCheck();
	}

	/**
	 * 新增区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Screen.java
	 * 
	 * @param ScreenInfo
	 * @return
	 *
	 */
	public String AddBlock(String BlockInfo) {
		Object info = "";
		if(StringHelper.InvaildString(BlockInfo)){
			return rMsg.netMSG(1, "无参数信息");
		}
		BlockInfo = codec.DecodeFastJSON(BlockInfo);
		
		JSONObject object = JSONObject.toJSON(BlockInfo);
		if (object == null || object.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		info = gDbModel.dataEx(object).autoComplete().insertOnce();
		//TODO 这边是不是查看是否查看是否添加了一条数据，如果没有添加成功，是否要进行判断,等测试的时候，注意看
		object = gDbModel.eq(pkString, info).find();
		return resultJSONInfo(object);
	}

	/**
	 * 批量新增区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param BlockInfo
	 * @return
	 *
	 */
	public String AddAllBlock(String BlockInfo) {
		String info = "";
		List<Object> list = new ArrayList<Object>();
		if(StringHelper.InvaildString(BlockInfo)){
			return rMsg.netMSG(2, "无参数信息");
		}
		BlockInfo = codec.DecodeFastJSON(BlockInfo);
		
		JSONArray Condarray = JSONArray.toJSONArray(BlockInfo);
		JSONObject obj;
		if (Condarray == null || Condarray.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (Condarray != null && Condarray.size() > 0) {
			for (Object object : Condarray) {
				obj = (JSONObject) object;
				info = gDbModel.data(obj).insertOnce().toString();
				list.add(info);
			}
		}
		info = StringHelper.join(list);
		JSONArray array = FindBlock(info);
		return resultArray(array);
	}

	/**
	 * 修改区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param id
	 * @param BlockInfo
	 * @return
	 *
	 */
	public String UpdateBlock(String id, String BlockInfo) {
		int code = 99;
		if (StringHelper.InvaildString(id)) {
			return rMsg.netMSG(3, "无效区域id");
		}
		if(StringHelper.InvaildString(BlockInfo)){
			return rMsg.netMSG(3, "无效区域id");
		}
		BlockInfo = codec.DecodeFastJSON(BlockInfo);
		
		JSONObject obj = JSONObject.toJSON(BlockInfo);
		if (obj == null || obj.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		
		if (obj != null && obj.size() > 0) {
			gDbModel.eq(pkString, id);
			code = (gDbModel.dataEx(obj).updateEx()) ? 0 : 99;
		}
		return resultMessage(code, "修改成功");
	}

	//TODO 
	public String UpdateAllBlock(JSONArray blockArray) {
		JSONObject obj;
		String id;
		if (blockArray != null && blockArray.size() > 0) {
			for (Object object : blockArray) {
				obj = (JSONObject) object;
				id = obj.getString(pkString);
				obj.remove(pkString);
				gDbModel.eq(pkString, id).data(obj).updateEx();
			}
		}
		return resultMessage(0, "修改成功");
	}

	/**
	 * 删除区域信息
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String DeleteBlock(String ids) {
		long code = 0;
		if (StringHelper.InvaildString(ids)) {
			return rMsg.netMSG(3, "无效区域id");
		}
		String[] value = ids.split(",");
		gDbModel.or();
		for (String id : value) {
			gDbModel.eq(pkString, id);
		}
		code = gDbModel.deleteAll();
		return resultMessage(code > 0 ? 0 : 99, "删除成功");
	}

	/**
	 * 查询区域详细信息,包含批量查询
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param bid
	 * @return
	 *
	 */
	private JSONArray FindBlock(String bids) {
		JSONArray array = null;
		gDbModel.or();
		if (!StringHelper.InvaildString(bids)) {
			String[] value = bids.split(",");
			for (String bid : value) {
				gDbModel.eq(pkString, bid);
			}
			array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").select();
		}
		return array;
	}

	/**
	 * 批量查询区域信息，封装成固定格式输出，{"message":{"records":[]},"errorcode":0}
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids
	 * @return
	 *
	 */
	public String ShowBlock(String ids) {
		return resultArray(FindBlock(ids));
	}

	/**
	 * 获取区域信息，封装成{bid:area,bid:area}
	 * 
	 * @project GrapeScreen
	 * @package interfaceApplication
	 * @file Block.java
	 * 
	 * @param ids  --_ids
	 * @return
	 *
	 */
	@SuppressWarnings("unchecked")
	public JSONObject GetBlockInfo(String ids) {
		JSONObject tempObj, obj = new JSONObject();
		String id;
		int l = 0;
		JSONArray array = FindBlock(ids);//可查询区域详细信息
		if (array != null && array.size() > 0) {
			l = array.size();
			for (int i = 0; i < l; i++) {
				tempObj = (JSONObject) array.get(i);
				if (tempObj != null && tempObj.size() > 0) {
					id = (String) tempObj.getPkValue(pkString);
					tempObj.remove("mid");
					tempObj.put("areaid", id);
					obj.put(id, tempObj);
				}
			}
		}
		return obj;
	}

	/**
	 * 添加区域信息到模式信息数据中
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
	public JSONArray Block2Mode(String bid, JSONArray array) {
		JSONObject BlockInfo = GetBlockInfo(bid);
		JSONObject object;
		if (BlockInfo != null && BlockInfo.size() > 0) {
			int l = array.size();
			for (int i = 0; i < l; i++) {
				object = (JSONObject) array.get(i);
				array.set(i, FillBlock(object, BlockInfo));
			}
		}
		return array;
	}

	/**
	 * 填充模式数据中的区域信息
	 * 
	 * @param ModeInfo
	 * @param BlockInfo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject FillBlock(JSONObject ModeInfo, JSONObject BlockInfo) {
		JSONObject tempObj = new JSONObject();
		JSONArray tempArray = new JSONArray();
		String bid;
		String[] value;
		if (ModeInfo != null && ModeInfo.size() != 0 && BlockInfo != null && BlockInfo.size() != 0) {
			bid = ModeInfo.getString("bid");
			value = bid.split(",");
			for (String str : value) {
				if (str != null && !str.equals("")) {
					tempObj = (JSONObject) BlockInfo.get(str);
					if ((tempObj != null) && (tempObj.size() != 0)) {
						tempArray.add(tempObj);
					}
				}
			}
			ModeInfo.remove("bid");
			ModeInfo.put("area", (tempArray != null) && (tempArray.size() > 0) ? tempArray : new JSONArray());
		}
		return ModeInfo;
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

}
