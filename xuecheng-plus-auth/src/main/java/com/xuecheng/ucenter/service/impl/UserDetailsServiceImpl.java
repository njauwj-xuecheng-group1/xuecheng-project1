package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
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
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/18 23:12
 * @explain: 核对用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {


    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private XcMenuMapper xcMenuMapper;

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
        //查询用户权限
        List<String> authorities = xcMenuMapper.selectPermissionByUserId(xcUserExt.getId());
        xcUserExt.setPermissions(authorities);
        //new String[0] 是指定数组的初始大小为 0。这个参数的作用是告诉 Java 编译器，我们需要一个 String 类型的数组，但是具体的大小还不确定，因此可以将数组的长度设为 0。
        // Java 编译器会根据 List 的大小动态分配数组的大小，并将 List 中的元素复制到数组中。
        String[] strings = authorities.toArray(new String[0]);
        return User.withUsername(userJson).password(password).authorities(strings).build();
    }
}
