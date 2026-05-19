package cn.keking.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import cn.keking.model.ReturnResponse;
import cn.keking.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author yudian-it
 * @date 2017/12/1
 */
@RestController
public class FileController {
    @Value("${file.dir}")
    String fileDir;
    @Autowired
    FileUtils fileUtils;
    String demoDir = "demo";
    String demoPath = demoDir + File.separator;
    private static final List<String> ALLOWED_UPLOAD_SUFFIXES = Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "csv"
    );

    @RequestMapping(value = "fileUpload", method = RequestMethod.POST)
    public String fileUpload(@RequestParam("file") MultipartFile file,
                             HttpServletRequest request) throws JsonProcessingException {
        String fileName = file.getOriginalFilename();
        if (!isSafeUploadName(fileName)) {
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(1, "Unsupported or unsafe file name", null));
        }
        // 判断该文件类型是否有上传过，如果上传过则提示不允许再次上传
        if (existsTypeFile(fileName)) {
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(1, "每一种类型只可以上传一个文件，请先删除原有文件再次上传", null));
        }
        Path uploadDir = Paths.get(fileDir, demoDir).toAbsolutePath().normalize();
        Path target = uploadDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(1, "Unsupported or unsafe file name", null));
        }
        try(InputStream in = file.getInputStream()){
            Files.createDirectories(uploadDir);
            Files.copy(in, target);
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(0, "SUCCESS", null));
        } catch (IOException e) {
            e.printStackTrace();
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(1, "FAILURE", null));
        }
    }

    @RequestMapping(value = "deleteFile", method = RequestMethod.GET)
    public String deleteFile(String fileName) throws JsonProcessingException {
        if (!isSafeUploadName(fileName)) {
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(1, "Unsupported or unsafe file name", null));
        }
        Path uploadDir = Paths.get(fileDir, demoDir).toAbsolutePath().normalize();
        Path target = uploadDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(1, "Unsupported or unsafe file name", null));
        }
        File file = target.toFile();
        if (file.exists()) {
            file.delete();
        }
        return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(0, "SUCCESS", null));
    }

    @RequestMapping(value = "listFiles", method = RequestMethod.GET)
    public String getFiles() throws JsonProcessingException {
        List<Map<String, String>> list = Lists.newArrayList();
        File file = new File(fileDir + demoPath);
        if (file.exists()) {
            Arrays.stream(file.listFiles()).forEach(file1 -> list.add(ImmutableMap.of("fileName", demoDir + "/" + file1.getName())));
        }
        return new ObjectMapper().writeValueAsString(list);
    }

    private String getFileName(String name) {
        String suffix = name.substring(name.lastIndexOf("."));
        String nameNoSuffix = name.substring(0, name.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return uuid + "-" + nameNoSuffix + suffix;
    }

    /**
     * 是否存在该类型的文件
     * @return
     * @param fileName
     */
    private boolean existsTypeFile(String fileName) {
        boolean result = false;
        String suffix = fileUtils.getSuffixFromFileName(fileName);
        File file = new File(fileDir + demoPath);
        if (file.exists()) {
            for(File file1 : file.listFiles()){
                String existsFileSuffix = fileUtils.getSuffixFromFileName(file1.getName());
                if (suffix.equals(existsFileSuffix)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private boolean isSafeUploadName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            return false;
        }
        String suffix = fileUtils.getSuffixFromFileName(fileName);
        return suffix != null && ALLOWED_UPLOAD_SUFFIXES.contains(suffix.toLowerCase(Locale.ROOT));
    }
}
