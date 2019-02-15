package cn.keking.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.zhuozhengsoft.pageoffice.FileSaver;
import com.zhuozhengsoft.pageoffice.OpenModeType;
import com.zhuozhengsoft.pageoffice.PageOfficeCtrl;

import cn.keking.model.ReturnResponse;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.FileOperateUtil;
import cn.keking.utils.HttpUtil;

@Controller
public class OnlineEditController {

	@Value("${posyspath}")
	private String poSysPath;
	@Value("${popassword}")
	private String poPassWord;
	@Value("${file.dir}")
	String fileDir;

	@Autowired
	DownloadUtils downloadUtils;

	/**
	 * 在线编辑WORD文档 回调地址(callBack)不能携带HTTP 回调时会把所有传递参数回传回去
	 * 
	 * @param url
	 *            文件下载地址URL
	 * @param request
	 * @param map
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/onlineEdit", method = RequestMethod.GET)
	public ModelAndView showWord(String url, HttpServletRequest request, Map<String, Object> map)
			throws UnsupportedEncodingException {
		PageOfficeCtrl poCtrl = new PageOfficeCtrl(request);
		poCtrl.setServerPage("/poserver.zz");// 设置服务页面
		poCtrl.addCustomToolButton("保存", "Save", 1);// 添加自定义保存按钮
		// poCtrl.addCustomToolButton("盖章","AddSeal",2);//添加自定义盖章按钮
		poCtrl.addCustomToolButton("打印", "ShowPrintDlg()", 6);
		poCtrl.addCustomToolButton("全屏切换", "SwitchFullScreen()", 4);
		poCtrl.addCustomToolButton("关闭", "close", 21);
		Map<String, String[]> parameterMap = request.getParameterMap();
		String urlParam = "?";
		for (String param : parameterMap.keySet()) {
			if (param.equals("url")) {
				continue;
			}
			urlParam += param + "=" + parameterMap.get(param)[0] + "&";
		}

		poCtrl.setSaveFilePage("/save" + urlParam);// 设置处理文件保存的请求方法

		// 打开word
		// poCtrl.webOpen("d:\\test.doc",OpenModeType.docAdmin,"张三");
		String[] strArray = url.split("\\.");
		int suffixIndex = strArray.length - 1;
		String type = strArray[suffixIndex];
		url = URLDecoder.decode(url, "utf-8");
		ReturnResponse<String> response = downloadUtils.downLoad(url, type, null);
		if (type.indexOf("doc") != -1) {
			poCtrl.webOpen(response.getContent().replace("/", "\\"), OpenModeType.docAdmin, "administrator");
		} else if (type.indexOf("xls") != -1) {
			poCtrl.webOpen(response.getContent().replace("/", "\\"), OpenModeType.xlsNormalEdit, "administrator");
		} else if (type.indexOf("ppt") != -1) {
			poCtrl.webOpen(response.getContent().replace("/", "\\"), OpenModeType.pptNormalEdit, "administrator");
		} else {
			poCtrl.webOpen(response.getContent().replace("/", "\\"), OpenModeType.mppNormalEdit, "administrator");
		}

		map.put("pageoffice", poCtrl.getHtmlCode("PageOfficeCtrl1"));
		ModelAndView mv = new ModelAndView("Word");
		return mv;
	}

	/**
	 * 点击保存按钮都事件
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/save")
	public void saveFile(HttpServletRequest request, HttpServletResponse response) {
		FileSaver fs = new FileSaver(request, response);
		fs.saveToFile(fileDir + fs.getFileName());
		fs.close();
		// word保存后的回调
		Map<String, String[]> parameterMap = request.getParameterMap();
		String callBack = parameterMap.get("callBack")[0];
		Map<String, String> paramMap = new HashMap<>();
		for (String param : parameterMap.keySet()) {
			paramMap.put(param, parameterMap.get(param)[0]);
		}
		paramMap.remove("callBack");
		paramMap.put("downloadPath", getRequestPrefix(request) + "/download?filename=" + fileDir + fs.getFileName());
		HttpUtil.doPost("http://" + callBack, paramMap);
	}

	/**
	 * 文件下载接口
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/download")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		init(request);
		try {
			String downloadfFileName = request.getParameter("filename");
			String fileName = downloadfFileName.substring(downloadfFileName.indexOf("_") + 1);
			String userAgent = request.getHeader("User-Agent");
			byte[] bytes = userAgent.contains("MSIE") ? fileName.getBytes() : fileName.getBytes("UTF-8");
			fileName = new String(bytes, "ISO-8859-1");
			fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
			response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"", fileName));
			try {
				response.addHeader("Content-Length", "" + new File(downloadfFileName).length());
			} catch (Exception e) {
				response.addHeader("Content-Length",
						"" + new File(new String(downloadfFileName.getBytes("iso-8859-1"), "utf-8")));
			}
			FileOperateUtil.downloadAbsPath(downloadfFileName, response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void init(HttpServletRequest request) {
		if (FileOperateUtil.FILEDIR == null) {
			FileOperateUtil.FILEDIR = request.getSession().getServletContext().getRealPath("/");
		}
	}

	/**
	 * 添加PageOffice的服务器端授权程序Servlet（必须）
	 * 
	 * @return
	 */
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		com.zhuozhengsoft.pageoffice.poserver.Server poserver = new com.zhuozhengsoft.pageoffice.poserver.Server();
		File licDir = new File(poSysPath);
		if (!licDir.exists()) {
			licDir.mkdirs();
		}
		poserver.setSysPath(poSysPath);// 设置PageOffice注册成功后,license.lic文件存放的目录
		ServletRegistrationBean srb = new ServletRegistrationBean(poserver);
		srb.addUrlMappings("/poserver.zz");
		srb.addUrlMappings("/posetup.exe");
		srb.addUrlMappings("/pageoffice.js");
		srb.addUrlMappings("/jquery.min.js");
		srb.addUrlMappings("/pobstyle.css");
		srb.addUrlMappings("/sealsetup.exe");
		return srb;//
	}

	/**
	 * 获取url请求前缀
	 * 
	 * @explain http://localhost:8080/test
	 * @param request
	 *            request对象
	 * @return
	 */
	public static String getRequestPrefix(HttpServletRequest request) {
		// 网络协议
		String networkProtocol = request.getScheme();
		// 网络ip
		String ip = request.getServerName();
		// 端口号
		int port = request.getServerPort();
		// 项目发布名称
		String webApp = request.getContextPath();
		String urlPrefix = networkProtocol + "://" + ip + ":" + port + webApp;
		return urlPrefix;
	}

}
