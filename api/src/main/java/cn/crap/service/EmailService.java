package cn.crap.service;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import cn.crap.beans.Config;
import cn.crap.dto.MailBean;
import cn.crap.inter.service.ICacheService;
import cn.crap.inter.service.IEmailService;
import cn.crap.utils.Aes;
import cn.crap.utils.Const;

@Service
public class EmailService implements IEmailService {
	@Autowired
	private JavaMailSenderImpl mailSenderService;
	@Autowired
	private ICacheService cacheService;
	@Autowired
	private Config config;
	
	@Override
	public void sendMail(MailBean mailBean) throws UnsupportedEncodingException, MessagingException{
		String fromName = cacheService.getSetting(Const.SETTING_TITLE).getValue();
		MimeMessage mimeMessage = mailSenderService.createMimeMessage();
		MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
		messageHelper.setFrom(mailSenderService.getUsername(), fromName); 
		messageHelper.setSubject(mailBean.getSubject());  
		messageHelper.setTo(mailBean.getToEmail());  
		messageHelper.setText(mailBean.getContext(), true);// html: true  
		mailSenderService.send(mimeMessage);
	}
	
	@Override
	public void sendRegisterMain(String eamil, String id) throws UnsupportedEncodingException, MessagingException{
		String code =  Aes.encrypt(id);
		String domain = config.getDomain() + "/back/validateEmail.do?i=" + code;
		MailBean mailBean = new MailBean();
		mailBean.setContext( getMtml(eamil, "注册邮箱验证", "<a href=\""+domain+"\">"+domain+"</a>"));
		mailBean.setToEmail(eamil);
		mailBean.setSubject("注册邮箱验证");
		sendMail(mailBean);
		cacheService.setStr(code, Const.REGISTER, 10 * 60);
	}
	
	private String getMtml(String eamil, String title, String content){
		StringBuffer sb = new StringBuffer();
		sb.append("<div style=\"position:relative;width:400px;margin:0 auto; background:#f7f7f7;color:#999999; font-size:14px;line-height:36px;\">");
		sb.append("<div style=\"height:60px; border-bottom:2px solid #6f5499;padding:10px;\" >");
		sb.append("<div style=\"float:left;margin-left:10px; line-height:60px;font-size:18px;font-weight:bold;color:#555;width:360px;height:60px;overflow:hidden;text-align:left;\">");
		sb.append( title );
		sb.append("</div></div><div style=\"padding:20px;min-height:260px;white-space: pre-wrap;word-wrap: break-word;\">");
		sb.append(content);
		sb.append("</div><div style=\"padding:20px;text-align:right;margin-top:30px;\">");
		sb.append("<a style=\"color:#6f5499;\" href=\"http://api.crap.cn?sj="
				+ System.currentTimeMillis() + "\">本网站由CrapApi提供技术与支持</a>");
		sb.append("<br></div></div>");
		return sb.toString();
	}
}
