package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author: wj
 * @create_time: 2023/5/19 17:52
 * @explain: 微信登入验证
 */
@Service("wxAuthService")
public class WxAuthServiceImpl implements AuthService, WxAuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private XcUserRoleMapper xcUserRoleMapper;

    @Resource
    private WxAuthService proxyObj;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            throw new RuntimeException("用户不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }

    /**
     * http请求方式: POST
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     * 根据授权码请求微信服务拿到令牌
     *
     * @param code
     * @return {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    @Override
    public Map<String, String> getAccessToken(String code) {
        String urlTemplate = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(urlTemplate, appid, secret, code);
        //调用微信服务拿到令牌
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        Map<String, String> result = JSON.parseObject(exchange.getBody(), Map.class);
        return result;
    }

    /**
     * http请求方式: GET
     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     * 使用令牌请求微信拿到用户信息
     *
     * @param accessToken
     * @param openid
     * @return {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * <p>
     * }
     */
    @Override
    public Map<String, String> getUserInfoByAccessToken(String accessToken, String openid) {
        String urlTemplate = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(urlTemplate, accessToken, openid);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //解决返回的数据乱码问题
        String body = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        Map<String, String> result = JSON.parseObject(body, Map.class);
        return result;
    }

    /**
     * 将微信用户信息保存至数据库
     *
     * @param userMap
     * @return
     */
    @Override
    @Transactional
    public XcUser addUserToDb(Map<String, String> userMap) {
        String unionid = userMap.get("unionid");
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getWxUnionid, unionid);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            //注册一个
            XcUser newUser = new XcUser();
            String nickname = userMap.get("nickname");
            String userId = UUID.randomUUID().toString();
            newUser.setId(userId);
            newUser.setUsername(unionid);
            newUser.setName(nickname);
            newUser.setPassword(unionid);
            newUser.setNickname(nickname);
            newUser.setName(nickname);
            newUser.setWxUnionid(userMap.get("unionid"));
            newUser.setUtype("101001");
            newUser.setStatus("1");
            newUser.setCreateTime(LocalDateTime.now());
            newUser.setUserpic(userMap.get("headimgurl"));
            xcUserMapper.insert(newUser);
            //保存用户角色表
            XcUserRole xcUserRole = new XcUserRole();
            xcUserRole.setId(UUID.randomUUID().toString());
            xcUserRole.setUserId(userId);
            xcUserRole.setRoleId("17");
            xcUserRole.setCreateTime(LocalDateTime.now());
            xcUserRoleMapper.insert(xcUserRole);
            return newUser;
        }
        return xcUser;
    }

    /**
     * @param code 授权码
     * @return
     */
    @Override
    public XcUser wxAuth(String code) {
        //请求微信申请令牌，
        Map<String, String> result = getAccessToken(code);
        // 拿到令牌查询用户信息，
        String accessToken = result.get("access_token");
        String openid = result.get("openid");
        Map<String, String> userMap = getUserInfoByAccessToken(accessToken, openid);
        // 将用户信息写入本项目数据库
        return proxyObj.addUserToDb(userMap);
    }
}