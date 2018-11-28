package com.atguigu.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;

//验证手机验证码
public class CodeVerifyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public CodeVerifyServlet() {

	}

	@SuppressWarnings("resource")
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String phone_num = request.getParameter("phone_no");
		String code_front = request.getParameter("verify_code");
		if(phone_num == null || "".equals(phone_num) || code_front == null || "".equals(code_front)) {
			return;
		}
		
		String phone_key = VerifyCodeConfig.PHONE_PREFIX + phone_num + VerifyCodeConfig.PHONE_SUFFIX;
		Jedis jedis = new Jedis(VerifyCodeConfig.HOST, VerifyCodeConfig.PORT);
		String code_query = jedis.get(phone_key);
		jedis.close();
		if(code_query.equals(code_front)) {
			response.getWriter().print(true);
		}
	}

}
