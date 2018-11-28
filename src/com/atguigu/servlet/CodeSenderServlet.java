package com.atguigu.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.StringUtils;

import com.atguigu.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;


//获取手机验证码的处理
/*
 * ①输入手机号，点击发送后随机生成6位数字码，2分钟有效
 * 			思路：   key-value
 * 					key: 手机号
 * 					value: 字符串
 * 				 设置2分钟有效：  setex  key value secouds
 * 					
 * 
   ②输入验证码，点击验证，返回成功或失败
   			思路： 
   			 	 验证：get key
   					
   
   ③每个手机号每天最多只能生成三次验证码
   			思路： 根据手机号，生成一个key，专门针对此手机号生成验证码的次数进行计数
   					计数： key ： 手机号
   						value: 字符串类型，每次生成就 执行 incr key
   						
   						setex key value 一天
   				在生成验证码之前，提前判断用户发送的次数，是否符合条件！
   				
   	核心： 根据业务，确定value类型，以及使用哪些API！

 */
public class CodeSenderServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
   
    public CodeSenderServlet() {
        
    }

    
    
	@SuppressWarnings("resource")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 获取前台输入的手机号
		String phone_num = request.getParameter("phone_no");
		
		if(phone_num == null || "".equals(phone_num)) {
			System.out.println("手机号码非法");
			return;
		}
		
		String count_key = phone_num + VerifyCodeConfig.COUNT_SUFFIX;
		System.out.println(count_key);
		Jedis jedis = new Jedis(VerifyCodeConfig.HOST, VerifyCodeConfig.PORT);
		String count_str = jedis.get(count_key);
		if(count_str == null) {
			jedis.set(count_key, "1");
		}else {
			int count = Integer.parseInt(count_str);
			if( count >= 3) {
				System.out.println(phone_num + "今日次数超越上限");
				response.getWriter().print("limit");
				jedis.close();
				return;
			}else {
				jedis.incr(count_key);
			}
		}
		
		String code = genCode(VerifyCodeConfig.CODE_LEN);
		String phone_key = VerifyCodeConfig.PHONE_PREFIX + phone_num + VerifyCodeConfig.PHONE_SUFFIX;
		jedis.setex(phone_key, VerifyCodeConfig.CODE_TIMEOUT, code);
		jedis.close();
		System.out.println(phone_key+"===>"+code);
		System.out.println("尊敬的"+phone_num+",你的六位验证码为"+code);
		response.getWriter().print(true);
		
		
	} 
	
	
	//生成6位验证码
	private  String genCode(int len){
		 String code="";
		 for (int i = 0; i < len; i++) {
		     int rand=  new Random().nextInt(10);
		     code+=rand;
		 }
		 
		return code;
	}
	
	
 
}
