package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

import java.util.Map;

/**
 * @author: wj
 * @create_time: 2023/5/20 20:30
 * @explain:
 */
public interface WxAuthService {




    Map<String, String> getAccessToken(String code);

    Map<String, String> getUserInfoByAccessToken(String accessToken, String openid);

    XcUser addUserToDb(Map<String, String> userMap);

    XcUser wxAuth(String code);
}
