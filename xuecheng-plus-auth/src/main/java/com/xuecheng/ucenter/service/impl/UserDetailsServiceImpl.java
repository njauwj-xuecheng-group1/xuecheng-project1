package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wj
 * @create_time: 2023/5/18 23:12
 * @explain: 核对用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {


    @Resource
    private ApplicationContext applicationContext;

    /**
     * 核对用户身份颁发令牌
     *
     * @param s :用户认证请求参数
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        String authType = authParamsDto.getAuthType();
        //策略模式
        String authServiceName = authType + "AuthService";
        AuthService authService = applicationContext.getBean(authServiceName, AuthService.class);
        //进行登入认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        return getUserDetails(xcUserExt);
    }

    private UserDetails getUserDetails(XcUserExt xcUserExt) {
        String password = xcUserExt.getPassword();
        xcUserExt.setPassword(null);
        String userJson = JSON.toJSONString(xcUserExt);
        return User.withUsername(userJson).password(password).authorities("test").build();
    }
}
