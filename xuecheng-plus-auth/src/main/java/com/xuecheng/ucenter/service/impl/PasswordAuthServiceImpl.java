package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wj
 * @create_time: 2023/5/19 17:50
 * @explain: 密码登入验证
 */
@Service("passwordAuthService")
public class PasswordAuthServiceImpl implements AuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;


    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        String inputPassword = authParamsDto.getPassword();
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getUsername, username);
        //账号是否存在验证
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            throw new RuntimeException("账号不存在");
        }
        //密码是否正确验证
        String actualPassword = xcUser.getPassword();
        boolean matches = passwordEncoder.matches(inputPassword, actualPassword);
        if (!matches) {
            throw new RuntimeException("密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
