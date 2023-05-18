package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wj
 * @create_time: 2023/5/18 23:12
 * @explain:
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //查询用户是否存在
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            return null;
        }
        return User.withUsername(username).password(xcUser.getPassword()).authorities("test").build();
    }
}
