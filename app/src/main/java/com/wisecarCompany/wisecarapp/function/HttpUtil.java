package com.wisecarCompany.wisecarapp.function;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    // 分割符
    private static final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";


    /**
     * HttpUrlConnection　实现文件上传
     * @param params 普通参数
     * @param fileFormName 文件在表单中的键
     * @param uploadFile 上传的文件
     * @param newFileName 文件在表单中的值（服务端获取到的文件名）
     * @param urlStr url
     * @throws IOException
     */
    public static String uploadForm(Map<String, String> params, String fileFormName, File uploadFile, String newFileName,
                                  String urlStr) throws IOException {
        if(uploadFile == null){
            StringBuilder sb = new StringBuilder();
            /**
             * 普通的表单数据
             */
            if (params != null) {
                for (String key : params.keySet()) {
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + "\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key) + "\r\n");
                }
            }
            byte[] headerInfo = sb.toString().getBytes("UTF-8");
            byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            // 设置传输内容的格式，以及长度
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("Content-Length",
                    String.valueOf(headerInfo.length + endInfo.length));
            conn.setDoOutput(true);

            OutputStream out = conn.getOutputStream();
            // 写入头部 （包含了普通的参数，以及文件的标示等）
            out.write(headerInfo);
            // 写入尾部
            out.write(endInfo);
            out.close();

            if(conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
                InputStream inputStream = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder s = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    s.append(line);
                }
                return s.toString();
            }else{
                return null;
            }
        }else {
            if (newFileName == null || newFileName.trim().equals("")) {
                newFileName = uploadFile.getName();
            }
            StringBuilder sb = new StringBuilder();
            /**
             * 普通的表单数据
             */
            if (params != null) {
                for (String key : params.keySet()) {
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + "\r\n");
                    sb.append("\r\n");
                    sb.append(params.get(key) + "\r\n");
                }
            }

            /**
             * 上传文件的头
             */
            sb.append("--" + BOUNDARY + "\r\n");
            sb.append("Content-Disposition: form-data; name=\"" + fileFormName + "\"; filename=\"" + newFileName + "\""
                    + "\r\n");
            sb.append("Content-Type: image/png" + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
            sb.append("\r\n");

            byte[] headerInfo = sb.toString().getBytes("UTF-8");
            byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");


            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            // 设置传输内容的格式，以及长度
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            conn.setRequestProperty("Content-Length",
                    String.valueOf(headerInfo.length + uploadFile.length() + endInfo.length));
            conn.setDoOutput(true);

            OutputStream out = conn.getOutputStream();
            InputStream in = new FileInputStream(uploadFile);
            // 写入头部 （包含了普通的参数，以及文件的标示等）
            out.write(headerInfo);
            // 写入文件
            byte[] buf = new byte[10240];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            // 写入尾部
            out.write(endInfo);
            in.close();
            out.close();
            if(conn.getResponseCode() == 200 || conn.getResponseCode() == 201){
                InputStream inputStream = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder s = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    s.append(line);
                }
                return s.toString();
            }else{
                return null;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //需要上传的文件
        File file = new File("ss.png");
        // 普通参数
        HashMap<String , String> params = new HashMap<>();
        params.put("user", "admin");

        // conn上传
        uploadForm(params, "file", file, "ss.jpg", "http://localhost:8080/Web/UploadFile");

    }

}