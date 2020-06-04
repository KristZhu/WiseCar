package com.example.wisecarapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class GenerateQRCode extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public GenerateQRCode() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String qrText = request.getParameter("qrText");
        ByteArrayOutputStream out = QRCode.from(qrText).to(ImageType.PNG).withSize(300, 300).stream();

        response.setContentType("image/png");
        response.setContentLength(out.size());

        OutputStream os = response.getOutputStream();
        os.write(out.toByteArray());

        os.flush();
        os.close();

    }

}